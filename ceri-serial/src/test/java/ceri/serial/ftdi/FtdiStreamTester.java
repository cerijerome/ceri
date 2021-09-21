package ceri.serial.ftdi;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.test.BinaryPrinter;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiStreamTester {
	private static final Logger logger = LogManager.getLogger();
	private static final BinaryPrinter printer =
		BinaryPrinter.builder().columns(4).showBinary(false).build();

	public static void main(String[] args) throws LibUsbException {
		try (Ftdi ftdi = Ftdi.open()) {
			ftdi.bitBang(true);
			process(ftdi);
		}
	}

	private static void process(Ftdi ftdi) throws LibUsbException {
		int waitMs = 60 * 1000;
		try (var x = ftdi.readStream(FtdiStreamTester::stream, "hello!", 1, 3)) {
			logger.info("Waiting for callback invocation");
			ConcurrentUtil.delay(waitMs);
			logger.info("Done");
		}
	}

	private static boolean stream(ByteBuffer buffer, int length, FtdiProgressInfo progress,
		String userData) {
		logger.info("Stream: {} {} {}", userData, length, progress);
		printer.print(buffer, length);
		return true;
	}

}
