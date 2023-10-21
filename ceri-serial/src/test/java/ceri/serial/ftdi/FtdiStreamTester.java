package ceri.serial.ftdi;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.test.BinaryPrinter;

public class FtdiStreamTester {
	private static final Logger logger = LogManager.getLogger();
	private static final BinaryPrinter printer =
		BinaryPrinter.builder().columns(4).showBinary(false).build();

	public static void main(String[] args) throws IOException {
		try (FtdiDevice ftdi = FtdiDevice.open()) {
			ftdi.bitBang(true);
			process(ftdi);
		}
	}

	private static void process(FtdiDevice ftdi) throws IOException {
		ftdi.readStream(FtdiStreamTester::stream, 1, 3);
	}

	private static boolean stream(FtdiProgressInfo progress, ByteBuffer buffer) {
		logger.info("Stream: {} {}", progress, buffer);
		if (buffer != null) printer.print(buffer);
		return true;
	}

}
