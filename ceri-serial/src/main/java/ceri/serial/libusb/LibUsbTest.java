package ceri.serial.libusb;

import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_IN;
import static ceri.serial.libusb.jna.LibUsb.libusb_endpoint_direction.LIBUSB_ENDPOINT_OUT;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_recipient.LIBUSB_RECIPIENT_DEVICE;
import static ceri.serial.libusb.jna.LibUsb.libusb_request_type.LIBUSB_REQUEST_TYPE_VENDOR;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sun.jna.ptr.ByteByReference;
import ceri.common.util.BasicUtil;
import ceri.log.util.LogUtil;
import ceri.serial.jna.JnaUtil;
import ceri.serial.libusb.jna.LibUsb;
import ceri.serial.libusb.jna.LibUsb.libusb_device_descriptor;
import ceri.serial.libusb.jna.LibUsbException;

public class LibUsbTest {
	private static final Logger logger = LogManager.getLogger();
	private static final byte FTDI_DEVICE_OUT_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_OUT);
	private static final byte FTDI_DEVICE_IN_REQTYPE = LibUsb.libusb_request_type(
		LIBUSB_RECIPIENT_DEVICE, LIBUSB_REQUEST_TYPE_VENDOR, LIBUSB_ENDPOINT_IN);
	private static final byte SIO_SET_BITMODE_REQUEST = 0x0b;
	private static final byte SIO_READ_PINS_REQUEST = 0x0c;

	public static void main(String[] args) throws IOException {
		System.out.printf("FTDI_DEVICE_OUT_REQTYPE=0x%02x%n", FTDI_DEVICE_OUT_REQTYPE);
		System.out.printf("FTDI_DEVICE_IN_REQTYPE=0x%02x%n", FTDI_DEVICE_IN_REQTYPE);
		//if (true) return;
		logger.info("Started");
		try (LibUsbContext ctx = LibUsbContext.init()) {
			try (LibUsbDeviceHandle handle = findFtdi(ctx)) {
				if (handle != null) process(handle);
			}
		}
	}

	private static void process(LibUsbDeviceHandle handle) throws IOException {
		int interfaceNumber = 0;
		int delayMs = 500;
		
		boolean kernelDriverActive = handle.kernelDriverActive(interfaceNumber);
		logger.info("kernel driver active: {}", kernelDriverActive);
		if (kernelDriverActive) {
			logger.info("detaching kernel driver");
			handle.detachKernelDriver(interfaceNumber);
		}
		logger.info("Claiming interface: {}", interfaceNumber);
		handle.claimInterface(interfaceNumber);
		setBaud(handle);
		bitBang(handle, true);
		setBaud(handle);
		for (int i = 0; i < 16; i++) {
			read(handle);
			BasicUtil.delay(delayMs);
			write(handle, i);
			BasicUtil.delay(delayMs);
		}
		bitBang(handle, false);
		logger.info("Releasing interface: {}", interfaceNumber);
		handle.releaseInterface(interfaceNumber);
		if (kernelDriverActive) {
			logger.info("re-attaching kernel driver");
			handle.attachKernelDriver(interfaceNumber);
		}
	}

	private static void write(LibUsbDeviceHandle handle, int value) throws LibUsbException {
		logger.info("writing: {}", value);
		handle.bulkTransfer(0x02,  new byte[] { (byte) value }, 500);
	}
	
	private static void setBaud(LibUsbDeviceHandle handle) throws LibUsbException {
		logger.info("setting 9600 baud");
		handle.controlTransfer(0x40, 0x03, 0x4138, 0x0000, 500);
	}
	
	private static void bitBang(LibUsbDeviceHandle handle, boolean enable) throws LibUsbException {
		logger.info("Bit-bang: {}", enable);
		int value = enable ? 0x01ff : 0x0000;
		handle.controlTransfer(0x40, 0x0b, value, 0x0001, 500);
	}

	private static int read(LibUsbDeviceHandle handle) throws LibUsbException {
		logger.info("Reading byte");
		byte[] data = handle.controlTransfer(0xc0, 0x0c, 0x0000, 0x0001, 1, 500);
		int value = JnaUtil.ubyte(data[0]);
		logger.info("Status: 0x{}", LogUtil.toHex(value));
		return value;
	}

	private static LibUsbDeviceHandle findFtdi(LibUsbContext ctx) throws LibUsbException {
		try (LibUsbDeviceList list = ctx.deviceList()) {
			logger.info("Devices found: {}", list.devices.size());
			for (LibUsbDevice device : list.devices) {
				libusb_device_descriptor desc = device.descriptor();
				if (desc.idVendor != (short) 0x0403) continue;
				if (desc.idProduct != (short) 0x6001) continue;
				logger.info("FTDI device found");
				return device.open();
			}
			logger.info("FTDI device not found");
			return null;
		}
	}

}
