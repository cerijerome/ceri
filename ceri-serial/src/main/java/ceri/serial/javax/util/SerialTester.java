package ceri.serial.javax.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import static ceri.common.util.BasicUtil.conditional;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteUtil;
import ceri.common.io.StateChange;
import ceri.common.io.IoUtil;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;

/**
 * Class to test serial ports. Takes commands via System.in and calls methods on the serial
 * connector. Logs data received from the port. Commands:
 * 
 * <pre>
 * b[0|1] = set break bit off/on
 * d[0|1] = set DTR off/on
 * r[0|1] = set RTS off/on
 * f[n|r0|r1|x0|x1] = set flow control none, rts-cts in/out, xon-xoff in/out
 * o[literal-chars] = write chars as bytes to output (e.g. \xff for byte 0xff)
 * z = mark the connector as broken
 * x = exit
 * </pre>
 */
public class SerialTester extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private static final int INPUT_BYTES_MAX = 32 * 1024;
	protected static final int DELAY_MS_DEF = 500;
	private final int delayMs;
	protected final SerialConnector connector;
	private final byte[] buffer = new byte[INPUT_BYTES_MAX];

	public static void test(String commPort) throws IOException {
		SelfHealingSerialConfig config = SelfHealingSerialConfig.of(commPort);
		try (SelfHealingSerialConnector con = SelfHealingSerialConnector.of(config)) {
			test(con);
		}
	}

	public static void test(SerialConnector con) throws IOException {
		try (SerialTester tester = SerialTester.of(con)) {
			tester.connect();
			tester.waitUntilStopped();
		}
	}

	public static SerialTester of(SerialConnector connector) {
		return of(connector, DELAY_MS_DEF);
	}

	public static SerialTester of(SerialConnector connector, int delayMs) {
		return new SerialTester(connector, delayMs);
	}

	protected SerialTester(SerialConnector connector, int delayMs) {
		this.connector = connector;
		this.delayMs = delayMs;
		this.connector.listeners().listen(this::event);
	}

	public void connect() throws IOException {
		connector.connect();
		start();
	}

	@Override
	public void waitUntilStopped() {
		try {
			super.waitUntilStopped();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	@Override
	public void waitUntilStopped(long timeoutMs) {
		try {
			super.waitUntilStopped(timeoutMs);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	protected void event(StateChange state) {
		logger.info("Event: {}", state);
	}

	protected String prompt() {
		return "> ";
	}

	protected void logOutput(ImmutableByteArray dataToPort) {
		System.out.println("OUT >>>");
		BinaryPrinter.DEFAULT.print(dataToPort);
	}

	protected void logInput(ImmutableByteArray dataFromPort) {
		System.out.println("IN <<<");
		BinaryPrinter.DEFAULT.print(dataFromPort);
	}

	protected void processCmd(char cmd, String params) throws IOException {
		if (cmd == 'x') throw new RuntimeInterruptedException("Exiting");
		if (cmd == 'b') safeAccept(bool(params), connector::setBreakBit);
		else if (cmd == 'd') safeAccept(bool(params), connector::setDtr);
		else if (cmd == 'r') safeAccept(bool(params), connector::setRts);
		else if (cmd == 'f') safeAccept(flowControlType(params), connector::setFlowControl);
		else if (cmd == 'o') writeToPort(ByteUtil.toAscii(params));
		else if (cmd == 'z') connector.broken();
	}

	@Override
	protected void loop() {
		try {
			BasicUtil.delay(delayMs);
			readFromPort();
			String command = getInput();
			processCmd(command);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Default implementation - get bytes from stdin.
	 */
	private String getInput() throws IOException {
		System.out.print(prompt());
		String s = IoUtil.readString(System.in).trim();
		return StringUtil.unEscape(s);
	}

	/**
	 * Read and display bytes from port.
	 */
	private void readFromPort() {
		try {
			while (true) {
				int available = connector.in().available();
				if (available <= 0) break;
				int n = connector.in().read(buffer);
				if (n > 0) logInput(ImmutableByteArray.wrap(buffer, 0, n));
				if (n == available) break;
			}
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Display and write bytes to port.
	 */
	private void writeToPort(ImmutableByteArray dataToPort) throws IOException {
		if (dataToPort.length == 0) return;
		logOutput(dataToPort);
		dataToPort.writeTo(connector.out());
		connector.out().flush();
	}

	private void processCmd(String command) throws IOException {
		if (!command.isEmpty()) processCmd(command.charAt(0), command.substring(1));
	}

	private Boolean bool(String s) {
		if ("0".equals(s)) return false;
		if ("1".equals(s)) return true;
		return null;
	}

	private FlowControl flowControlType(String s) {
		if (s.isEmpty()) return null;
		char c = s.charAt(0);
		String p = s.substring(1);
		if (c == 'n') return FlowControl.none;
		if (c == 'r') return conditional(bool(p), FlowControl.rtsCtsOut, FlowControl.rtsCtsIn);
		if (c == 'x') return conditional(bool(p), FlowControl.xonXoffOut, FlowControl.xonXoffIn);
		return null;
	}

}