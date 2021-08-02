package ceri.log.io.test;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.io.StateChange;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;
import ceri.common.util.Enclosed;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.io.SelfHealingSocketConfig;
import ceri.log.io.SelfHealingSocketConnector;
import ceri.log.io.SocketConnector;
import ceri.log.util.LogUtil;

/**
 * Class to test socket connectors. Takes commands via System.in and calls methods on the socket
 * connector. Logs data received from the port. Commands:
 *
 * <pre>
 * o[literal-chars] = write chars as bytes to output (e.g. \xff for byte 0xff)
 * z = mark the connector as broken
 * Z = fix the connector (if supported)
 * x = exit
 * </pre>
 */
public class SocketConnectorTester extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	protected static final int DELAY_MS_DEF = 200;
	private final ExceptionRunnable<IOException> fixConnectorFn;
	private final int delayMs;
	protected final SocketConnector connector;
	private final Enclosed<RuntimeException, ?> listener;
	private boolean showHelp = true;

	public static void main(String[] args) throws IOException {
		try (EchoServerSocket ss = EchoServerSocket.of()) {
			test("localhost", ss.port());
		}
	}

	public static void test(String host, int port) throws IOException {
		SelfHealingSocketConfig config = SelfHealingSocketConfig.of(host, port);
		try (SelfHealingSocketConnector con = SelfHealingSocketConnector.of(config)) {
			con.connect();
			test(con, null);
		}
	}

	public static void test(SocketConnector con, ExceptionRunnable<IOException> fixConnectorFn) {
		// Make sure connector is connected first
		try (SocketConnectorTester tester = SocketConnectorTester.of(con, fixConnectorFn)) {
			tester.waitUntilStopped();
		}
	}

	public static SocketConnectorTester of(SocketConnector connector,
		ExceptionRunnable<IOException> fixConnectorFn) {
		return of(connector, fixConnectorFn, DELAY_MS_DEF);
	}

	public static SocketConnectorTester of(SocketConnector connector,
		ExceptionRunnable<IOException> fixConnectorFn, int delayMs) {
		return new SocketConnectorTester(connector, fixConnectorFn, delayMs);
	}

	protected SocketConnectorTester(SocketConnector connector,
		ExceptionRunnable<IOException> fixConnectorFn, int delayMs) {
		this.connector = connector;
		this.fixConnectorFn = fixConnectorFn;
		this.delayMs = delayMs;
		listener = connector.listeners().enclose(this::event);
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
	public void close() {
		LogUtil.close(logger, listener);
		super.close();
	}

	protected void event(StateChange state) {
		logger.info("Event: {}", state);
	}

	protected String prompt() {
		return "> ";
	}

	protected void logOutput(ByteProvider dataToPort) {
		System.out.println("OUT >>>");
		BinaryPrinter.STD.print(dataToPort);
	}

	protected void logInput(ByteProvider dataFromPort) {
		System.out.println("IN <<<");
		BinaryPrinter.STD.print(dataFromPort);
	}

	protected void processCmd(char cmd, String params) throws IOException {
		if (cmd == 'x') throw new RuntimeInterruptedException("Exiting");
		if (cmd == '?') showHelp = true;
		else if (cmd == 'o') writeToPort(ByteUtil.toAscii(params));
		else if (cmd == 'z') connector.broken();
		else if (cmd == 'Z' && fixConnectorFn != null) fixConnectorFn.run();
	}

	@Override
	protected void loop() {
		try {
			ConcurrentUtil.delay(delayMs);
			readFromPort();
			String command = getInput();
			processCmd(command);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Get command from stdin.
	 */
	private String getInput() throws IOException {
		if (showHelp) showHelp();
		System.out.print(prompt());
		String s = IoUtil.pollString(System.in).trim();
		return StringUtil.unEscape(s);
	}

	/**
	 * Read and display bytes from port.
	 */
	@SuppressWarnings("resource")
	private void readFromPort() {
		try {
			int available = connector.in().available();
			if (available <= 0) return;
			byte[] bytes = connector.in().readNBytes(available);
			if (bytes.length > 0) logInput(Immutable.wrap(bytes));
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	/**
	 * Display and write bytes to port.
	 */
	@SuppressWarnings("resource")
	private void writeToPort(ByteProvider dataToPort) throws IOException {
		if (dataToPort.length() == 0) return;
		logOutput(dataToPort);
		dataToPort.writeTo(0, connector.out());
		connector.out().flush();
	}

	private void processCmd(String command) throws IOException {
		if (!command.isEmpty()) processCmd(command.charAt(0), command.substring(1));
	}

	private void showHelp() {
		System.out.println("Commands:");
		showHelpCommands();
		showHelp = false;
	}

	protected void showHelpCommands() {
		System.out.println("  o<literal-chars> = write char bytes to output (e.g. \\xff for 0xff)");
		System.out.println("  z = mark the connector as broken");
		if (fixConnectorFn != null) System.out.println("  Z = mark the connector as fixed");
		System.out.println("  ? = show this message");
		System.out.println("  x = exit");
	}

}
