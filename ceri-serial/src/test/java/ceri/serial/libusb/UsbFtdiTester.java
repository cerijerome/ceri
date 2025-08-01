package ceri.serial.libusb;

import static ceri.common.math.MathUtil.ubyte;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.jna.LibFtdiUtil;
import ceri.serial.libusb.jna.LibUsbException;

public class UsbFtdiTester {
	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws LibUsbException {
		logger.info("Started");
		try (Usb ctx = Usb.of()) {
			try (UsbDeviceHandle handle = ctx.open(LibFtdiUtil.FINDER)) {
				process(handle);
			}
		}
	}

	private static void process(UsbDeviceHandle handle) throws LibUsbException {
		int interfaceNumber = 0;
		int delayMs = 200;

		boolean kernelDriverActive = handle.kernelDriverActive(interfaceNumber);
		logger.info("kernel driver active: {}", kernelDriverActive);
		if (kernelDriverActive) {
			logger.info("detaching kernel driver");
			handle.detachKernelDriver(interfaceNumber);
		}
		logger.info("Claiming interface: {}", interfaceNumber);
		handle.claimInterface(interfaceNumber);
		logger.info("setting 9600 baud");
		handle.controlTransfer(0x40, 0x03, 0x4138, 0, 500);
		logger.info("Bit-bang on");
		handle.controlTransfer(0x40, 0x0b, 0x01ff, 1, 500);
		read(handle);
		ConcurrentUtil.delay(delayMs);
		ByteBuffer b =
			ByteBuffer.wrap(ArrayUtil.bytes.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
		for (int i = 0; i < 16; i++) {
			write(handle, b, i);
			ConcurrentUtil.delay(delayMs);
			read(handle);
			ConcurrentUtil.delay(delayMs);
		}
		logger.info("Bit-bang off");
		handle.controlTransfer(0x40, 0x0b, 0x0000, 1, 500);
		logger.info("Releasing interface: {}", interfaceNumber);
		handle.releaseInterface(interfaceNumber);
		if (kernelDriverActive) {
			logger.info("re-attaching kernel driver");
			handle.detachKernelDriver(interfaceNumber);
		}
		logger.info("Done");
	}

	private static void read(UsbDeviceHandle handle) throws LibUsbException {
		logger.info("Reading 1 byte");
		int value = ubyte(handle.controlTransfer(0xc0, 0x0c, 0x0000, 1, 1, 500)[0]);
		logger.info("Status: 0x{}", LogUtil.toHex(value));
	}

	private static void write(UsbDeviceHandle handle, ByteBuffer b, int i) throws LibUsbException {
		logger.info("Writing: {}", b.get(i));
		b.position(i);
		logger.info("Position before = {}", b.position());
		int n = handle.bulkTransfer(0x02, b, 500);
		logger.info("Position after = {}", b.position());
		logger.info("Sent: {} bytes", n);
	}

}
