package ceri.serial.libusb.jna;

import static ceri.common.math.MathUtil.ubyte;
import static ceri.serial.libusb.jna.LibUsb.libusb_bulk_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_claim_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_close;
import static ceri.serial.libusb.jna.LibUsb.libusb_control_transfer;
import static ceri.serial.libusb.jna.LibUsb.libusb_detach_kernel_driver;
import static ceri.serial.libusb.jna.LibUsb.libusb_exit;
import static ceri.serial.libusb.jna.LibUsb.libusb_init;
import static ceri.serial.libusb.jna.LibUsb.libusb_kernel_driver_active;
import static ceri.serial.libusb.jna.LibUsb.libusb_open;
import static ceri.serial.libusb.jna.LibUsb.libusb_release_interface;
import static ceri.serial.libusb.jna.LibUsb.libusb_unref_device;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.log.util.LogUtil;
import ceri.serial.libusb.jna.LibUsb.libusb_context;
import ceri.serial.libusb.jna.LibUsb.libusb_device;
import ceri.serial.libusb.jna.LibUsb.libusb_device_handle;

public class LibUsbFtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws LibUsbException {
		logger.info("Started");
		libusb_context ctx = libusb_init();
		libusb_device dev = null;
		libusb_device_handle handle = null;
		try {
			var finder = LibUsbFinder.from("0x403");
			dev = finder.findAndRef(ctx);
			handle = libusb_open(dev);
			process(handle);
		} finally {
			libusb_close(handle);
			libusb_unref_device(dev);
			libusb_exit(ctx);
		}
	}

	private static void process(libusb_device_handle handle) throws LibUsbException {
		int interfaceNumber = 0;
		int delayMs = 200;

		boolean kernelDriverActive = libusb_kernel_driver_active(handle, interfaceNumber);
		logger.info("kernel driver active: {}", kernelDriverActive);
		if (kernelDriverActive) {
			logger.info("detaching kernel driver");
			libusb_detach_kernel_driver(handle, interfaceNumber);
		}
		logger.info("Claiming interface: {}", interfaceNumber);
		libusb_claim_interface(handle, interfaceNumber);
		logger.info("setting 9600 baud");
		libusb_control_transfer(handle, 0x40, 0x03, 0x4138, 0, 500);
		logger.info("Bit-bang on");
		// libusb_control_transfer(handle, 0x40, 0x0b, 0x01ff, 1, 500);
		libusb_control_transfer(handle, 0x40, 0x0b, 0x010f, 1, 500);
		read(handle);
		ConcurrentUtil.delay(delayMs);
		ByteBuffer b =
			ByteBuffer.wrap(ArrayUtil.bytes(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
		for (int i = 0; i < 16; i++) {
			write(handle, b, i);
			ConcurrentUtil.delay(delayMs);
			read(handle);
			ConcurrentUtil.delay(delayMs);
		}
		logger.info("Bit-bang off");
		libusb_control_transfer(handle, 0x40, 0x0b, 0, 0x0001, 500);
		logger.info("Releasing interface: {}", interfaceNumber);
		libusb_release_interface(handle, interfaceNumber);
		if (kernelDriverActive) {
			logger.info("re-attaching kernel driver");
			libusb_detach_kernel_driver(handle, interfaceNumber);
		}
		logger.info("Done");
	}

	private static void read(libusb_device_handle handle) throws LibUsbException {
		logger.info("Reading 1 byte");
		int value = ubyte(libusb_control_transfer(handle, 0xc0, 0x0c, 0x0000, 1, 1, 500)[0]);
		logger.info("Status: 0x{}", LogUtil.toHex(value));
	}

	private static void write(libusb_device_handle handle, ByteBuffer b, int i)
		throws LibUsbException {
		logger.info("Writing: {}", b.get(i));
		b.position(i);
		logger.info("Position before = {}", b.position());
		int n = libusb_bulk_transfer(handle, 0x02, b, 1, 500);
		logger.info("Position after = {}", b.position());
		logger.info("Sent: {} bytes", n);
	}

}
