package ceri.serial.ftdi;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.test.BinaryPrinter;
import ceri.serial.libusb.jna.LibUsbException;

public class FtdiStreamTester {
	private static final Logger logger = LogManager.getLogger();
	private static final BinaryPrinter printer =
		BinaryPrinter.builder().columns(4).showBinary(false).build();

	public static void main(String[] args) throws LibUsbException {
		try (FtdiDevice ftdi = FtdiDevice.open()) {
			ftdi.bitBang(true);
			process(ftdi);
		}
	}

	private static void process(FtdiDevice ftdi) throws LibUsbException {
		ftdi.readStream(FtdiStreamTester::stream, 1, 3);
	}

	private static boolean stream(FtdiProgressInfo progress, ByteBuffer buffer) {
		logger.info("Stream: {} {}", progress, buffer);
		if (buffer != null) printer.print(buffer);
		return true;
	}

}
