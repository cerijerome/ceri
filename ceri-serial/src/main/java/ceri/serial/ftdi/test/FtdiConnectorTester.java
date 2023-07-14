package ceri.serial.ftdi.test;

import static ceri.common.function.FunctionUtil.safeAccept;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.io.StateChange;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;
import ceri.common.util.Enclosed;
import ceri.common.util.PrimitiveUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiConnector;
import ceri.serial.ftdi.FtdiFlowControl;
import ceri.serial.ftdi.util.SelfHealingFtdiConfig;
import ceri.serial.ftdi.util.SelfHealingFtdi;

/**
 * Class to test ftdi devices. Takes commands via System.in and calls methods on the ftdi connector.
 * Logs data received from the port. Commands:
 *
 * <pre>
 * b[0|1] = set bitbang off/on
 * d[0|1] = set DTR off/on
 * r[0|1] = set RTS off/on
 * f[n|r|d|x] = set flow control none, rts-cts, dtr-dsr, xon-xoff
 * p = read pins
 * i[n] = read input n bytes (1 if no n)
 * o[literal-chars] = write chars as bytes to output (e.g. \xff for byte 0xff)
 * z = mark the connector as broken
 * x = exit
 * </pre>
 */
public class FtdiConnectorTester extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	protected static final int DELAY_MS_DEF = 200;
	private final ExceptionRunnable<IOException> fixConnectorFn;
	private final int delayMs;
	protected final FtdiConnector connector;
	private final Enclosed<RuntimeException, ?> listener;
	private boolean showHelp = true;

	public static void test(String finder) throws IOException {
		SelfHealingFtdiConfig config = SelfHealingFtdiConfig.of(finder);
		try (SelfHealingFtdi con = SelfHealingFtdi.of(config)) {
			con.open();
			test(con, null);
		}
	}

	public static void test(FtdiConnector con, ExceptionRunnable<IOException> fixConnectorFn) {
		// Make sure connector is connected first
		try (FtdiConnectorTester tester = FtdiConnectorTester.of(con, fixConnectorFn)) {
			tester.waitUntilStopped();
		}
	}

	public static FtdiConnectorTester of(FtdiConnector connector,
		ExceptionRunnable<IOException> fixConnectorFn) {
		return of(connector, fixConnectorFn, DELAY_MS_DEF);
	}

	public static FtdiConnectorTester of(FtdiConnector connector,
		ExceptionRunnable<IOException> fixConnectorFn, int delayMs) {
		return new FtdiConnectorTester(connector, fixConnectorFn, delayMs);
	}

	protected FtdiConnectorTester(FtdiConnector connector,
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

	protected void logInput(String name, ByteProvider dataFromPort) {
		System.out.println(name + " <<<");
		BinaryPrinter.STD.print(dataFromPort);
	}

	// p = read pins
	// i[n] = read input n bytes (1 if no n)

	protected void processCmd(char cmd, String params) throws IOException {
		if (cmd == 'x') throw new RuntimeInterruptedException("Exiting");
		if (cmd == '?') showHelp = true;
		else if (cmd == 'b') safeAccept(bool(params), this::bitbang);
		else if (cmd == 'd') safeAccept(bool(params), connector::dtr);
		else if (cmd == 'r') safeAccept(bool(params), connector::rts);
		else if (cmd == 'f') safeAccept(flowControlType(params), connector::flowControl);
		else if (cmd == 'p') readPins();
		else if (cmd == 'i') readIn(PrimitiveUtil.valueOf(params, 1));
		else if (cmd == 'o') writeOut(ByteUtil.toAscii(params));
		else if (cmd == 'z') connector.broken();
		else if (cmd == 'Z' && fixConnectorFn != null) fixConnectorFn.run();
	}

	@Override
	protected void loop() {
		try {
			ConcurrentUtil.delay(delayMs);
			String command = getInput();
			processCmd(command);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

	private void bitbang(boolean enabled) throws IOException {
		connector.bitmode(enabled ? FtdiBitMode.BITBANG : FtdiBitMode.OFF);
	}

	private String getInput() throws IOException {
		if (showHelp) showHelp();
		System.out.print(prompt());
		String s = IoUtil.pollString(System.in).trim();
		return StringUtil.unEscape(s);
	}

	private void readPins() throws IOException {
		byte[] pins = ByteUtil.toMsb(connector.readPins());
		logInput("PINS", ByteProvider.of(pins));
	}

	private void readIn(int n) throws IOException {
		if (n == 0) return;
		byte[] bytes = connector.read(n);
		logInput("IN", ByteProvider.of(bytes));
	}

	private void writeOut(ByteProvider data) throws IOException {
		if (data.length() == 0) return;
		logOutput(data);
		connector.write(data.copy(0));
	}

	private void processCmd(String command) throws IOException {
		if (!command.isEmpty()) processCmd(command.charAt(0), command.substring(1));
	}

	private Boolean bool(String s) {
		if ("0".equals(s)) return false;
		if ("1".equals(s)) return true;
		return null;
	}

	private FtdiFlowControl flowControlType(String s) {
		if (s.isEmpty()) return null;
		char c = s.charAt(0);
		if (c == 'n') return FtdiFlowControl.disabled;
		if (c == 'r') return FtdiFlowControl.rtsCts;
		if (c == 'd') return FtdiFlowControl.dtrDsr;
		if (c == 'x') return FtdiFlowControl.xonXoff;
		return null;
	}

	private void showHelp() {
		System.out.println("Commands:");
		showHelpCommands();
		showHelp = false;
	}

	protected void showHelpCommands() {
		System.out.println("  b[01] = set bitbang off/on");
		System.out.println("  d[01] = set DTR off/on");
		System.out.println("  r[01] = set RTS off/on");
		System.out.println("  f[nrdx] = set flow control none, rts-cts, dtr-dsr, xon-xoff");
		System.out.println("  p = read pins");
		System.out.println("  i[n] = read n char bytes from input, 1 if n not specified");
		System.out.println("  o<literal-chars> = write char bytes to output (e.g. \\xff for 0xff)");
		System.out.println("  z = mark the connector as broken");
		if (fixConnectorFn != null) System.out.println("  Z = mark the connector as fixed");
		System.out.println("  ? = show this message");
		System.out.println("  x = exit");
	}

}
