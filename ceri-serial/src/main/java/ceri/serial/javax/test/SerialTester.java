package ceri.serial.javax.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.test.BinaryPrinter;
import ceri.common.test.TestUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.serial.javax.SelfHealingSerialConnector;

/**
 * Class to test serial ports. Writes data received from the port in binary, and reads input from
 * System.in to send to the port. Escaped characters will be encoded. Override writeToPort and/or
 * getInput to customize behavior.
 */
public class SerialTester extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private static final int INPUT_BYTES_MAX = 32 * 1024;
	private static final int DELAY_MS = 500;
	protected final SelfHealingSerialConnector connector;
	private final byte[] buffer = new byte[INPUT_BYTES_MAX];

	public static void test(String commPort) throws IOException {
		try (SelfHealingSerialConnector con = SelfHealingSerialConnector.builder(commPort).build()) {
			test(con);
		}
	}

	public static void test(SelfHealingSerialConnector con) throws IOException {
		try (SerialTester tester = new SerialTester(con)) {
			while (true)
				BasicUtil.delay(DELAY_MS);
		}
	}

	public SerialTester(SelfHealingSerialConnector connector) throws IOException {
		this.connector = connector;
		this.connector.connect();
		start();
	}

	@Override
	protected void loop() {
		try {
			BasicUtil.delay(DELAY_MS);
			ImmutableByteArray dataFromPort = readFromPort();
			ImmutableByteArray dataToPort = getInput(dataFromPort);
			writeToPort(dataToPort);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Display and write bytes to port.
	 */
	protected void writeToPort(ImmutableByteArray dataToPort) throws IOException {
		if (dataToPort.length == 0) return;
		System.out.println("OUT >>>");
		BinaryPrinter.DEFAULT.print(dataToPort.copy());
		dataToPort.writeTo(connector.out());
		connector.out().flush();
	}

	/**
	 * Default implementation - get bytes from stdin.
	 */
	protected ImmutableByteArray getInput(ImmutableByteArray dataFromPort) throws IOException {
		BasicUtil.unused(dataFromPort);
		System.out.print("> ");
		String s = TestUtil.readString(System.in);
		s = StringUtil.unEscape(s);
		byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1); // extended ascii 0x00-0xff
		return ImmutableByteArray.wrap(bytes);
	}

	/**
	 * Read and display bytes from port.
	 */
	protected ImmutableByteArray readFromPort() throws IOException {
		while (true) {
			int available = connector.in().available();
			if (available <= 0) return ImmutableByteArray.EMPTY;
			int n = connector.in().read(buffer);
			System.out.println("IN <<<");
			if (n > 0) BinaryPrinter.DEFAULT.print(buffer, 0, n);
			if (n == available) return ImmutableByteArray.wrap(buffer, n);
		}
	}

}
