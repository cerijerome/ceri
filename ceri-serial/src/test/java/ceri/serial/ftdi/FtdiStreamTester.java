package ceri.serial.ftdi;

import static ceri.serial.ftdi.jna.LibFtdi.FTDI_VENDOR_ID;
import static ceri.serial.libusb.jna.LibUsbFinder.libusb_find_criteria;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.test.BinaryPrinter;
import ceri.common.util.BasicUtil;
import ceri.serial.ftdi.jna.LibFtdiStream.ftdi_progress_info;
import ceri.serial.libusb.jna.LibUsbException;
import ceri.serial.libusb.jna.LibUsbFinder.libusb_device_criteria;

public class FtdiStreamTester {
	private static final Logger logger = LogManager.getLogger();
	private static final BinaryPrinter printer = BinaryPrinter.builder()
		.columns(4).showBinary(false).build();
		
	public static void main(String[] args) throws LibUsbException {
		try (Ftdi ftdi = Ftdi.create()) {
			libusb_device_criteria criteria = libusb_find_criteria().vendor(FTDI_VENDOR_ID);
			ftdi.open(criteria);
			ftdi.bitbang(true);
			process(ftdi);
		}
	}

	private static void process(Ftdi ftdi) throws LibUsbException {
		int waitMs = 60 * 1000;
		ftdi.readStream(FtdiStreamTester::stream, "hello!", 1, 3);
		logger.info("Waiting for callback invocation");
		BasicUtil.delay(waitMs);
		logger.info("Done");
	}

	private static boolean stream(ByteBuffer buffer, int length, ftdi_progress_info progress,
		String userData) {
		logger.info("Stream: {} {} {} {}", userData, length, progress);
		printer.print(buffer, length);
		return true;
	}

}
