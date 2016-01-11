package ceri.jssc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import jssc.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StreamNotSetException;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A self-healing serial connector. It will automatically reconnect if the cable is removed and
 * replaced. USB-to-serial connector device names can change after disconnecting and reconnecting.
 * The CommPortSupplier interface can be used to provide handling logic in this case.
 */
public class SelfHealingSerialConnector extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	public static final Pattern BROKEN_CONNECTION_REGEX = Pattern
		.compile("(?i)failed");
	private final CommPortSupplier commPortSupplier;
	private final int baud;
	private final int dataBits;
	private final int stopBits;
	private final int parity;
	private final int fixRetryDelayMs;
	private final int recoveryDelayMs;
	private final Predicate<IOException> brokenPredicate;
	private final Listeners<State> listeners = new Listeners<>();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.create();
	private volatile JsscSerialPort serialPort;

	public static interface CommPortSupplier {
		String get() throws IOException;

		static CommPortSupplier fixed(String commPort) {
			return () -> commPort;
		}
	}

	public static class Builder {
		final CommPortSupplier commPortSupplier;
		int baud = 9600;
		int dataBits = SerialPort.DATABITS_8;
		int stopBits = SerialPort.STOPBITS_1;
		int parity = SerialPort.PARITY_NONE;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<IOException> brokenPredicate = e -> streamNotSet(e) ||
			messageMatches(BROKEN_CONNECTION_REGEX, e);

		Builder(CommPortSupplier commPortSupplier) {
			this.commPortSupplier = commPortSupplier;
		}

		public Builder baud(int baud) {
			this.baud = baud;
			return this;
		}

		public Builder dataBits(int dataBits) {
			this.dataBits = dataBits;
			return this;
		}

		public Builder stopBits(int stopBits) {
			this.stopBits = stopBits;
			return this;
		}

		public Builder parity(int parity) {
			this.parity = parity;
			return this;
		}

		public Builder fixRetryDelayMs(int fixRetryDelayMs) {
			this.fixRetryDelayMs = fixRetryDelayMs;
			return this;
		}

		public Builder recoveryDelayMs(int recoveryDelayMs) {
			this.recoveryDelayMs = recoveryDelayMs;
			return this;
		}

		public Builder brokenPredicate(Predicate<IOException> brokenPredicate) {
			this.brokenPredicate = brokenPredicate;
			return this;
		}

		public SelfHealingSerialConnector build() {
			return new SelfHealingSerialConnector(this);
		}
	}

	public static Builder builder(String commPort) {
		return new Builder(CommPortSupplier.fixed(commPort));
	}

	public static Builder builder(CommPortSupplier commPortSupplier) {
		return new Builder(commPortSupplier);
	}

	SelfHealingSerialConnector(Builder builder) {
		commPortSupplier = builder.commPortSupplier;
		baud = builder.baud;
		stopBits = builder.stopBits;
		dataBits = builder.dataBits;
		parity = builder.parity;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
		in.listen(this::ioException);
		out.listen(this::ioException);
		start();
	}

	public static enum State {
		fixed,
		broken;
	}

	/**
	 * Manually notify the connector it is broken. Useful if the connector cannot determine it is
	 * broken from IOExceptions alone.
	 */
	public void broken() {
		setBroken();
	}

	public void connect() throws IOException {
		initSerialPort();
	}

	public Listenable<State> listeners() {
		return listeners;
	}

	public InputStream in() {
		return in;
	}

	public OutputStream out() {
		return out;
	}

	public void setDTR(boolean state) throws IOException {
		serialPort().setDTR(state);
	}

	public void setRTS(boolean state) throws IOException {
		serialPort().setRTS(state);
	}

	private JsscSerialPort serialPort() throws IOException {
		JsscSerialPort serialPort = this.serialPort;
		if (serialPort == null) throw new IOException("Serial port not connected");
		return serialPort;
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, serialPort);
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Attempting to fix connection");
		String lastErrorMsg = null;
		while (true) {
			try {
				initSerialPort();
				break;
			} catch (IOException e) {
				String errorMsg = e.getMessage();
				if (lastErrorMsg == null || !lastErrorMsg.equals(errorMsg)) logger.debug(
					"Failed to fix connection, retrying: {}", errorMsg);
				lastErrorMsg = errorMsg;
				BasicUtil.delay(fixRetryDelayMs);
			}
		}
		logger.info("Connection is now fixed");
		BasicUtil.delay(recoveryDelayMs); // wait for streams to recover before clearing 
		sync.clear();
		notifyListeners(State.fixed);
	}

	private void notifyListeners(State state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void ioException(IOException e) {
		if (!brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		logger.warn("Connection is broken");
		notifyListeners(State.broken);
	}

	private void initSerialPort() throws IOException {
		LogUtil.close(logger, serialPort);
		String commPort = commPortSupplier.get();
		serialPort = openSerialPort(commPort);
		serialPort.purge(SerialPort.PURGE_TXABORT | SerialPort.PURGE_RXABORT);
		in.setInputStream(serialPort.getInputStream());
		out.setOutputStream(serialPort.getOutputStream());
	}

	private JsscSerialPort openSerialPort(String commPort) throws IOException {
		JsscSerialPort serialPort = JsscSerialPort.open(commPort);
		serialPort.setParams(baud, dataBits, stopBits, parity);
		return serialPort;
	}

	public static boolean streamNotSet(IOException e) {
		return e instanceof StreamNotSetException;
	}

	public static boolean messageMatches(Pattern pattern, IOException e) {
		String message = e.getMessage();
		if (message == null) return false;
		return pattern.matcher(message).find();
	}

}
