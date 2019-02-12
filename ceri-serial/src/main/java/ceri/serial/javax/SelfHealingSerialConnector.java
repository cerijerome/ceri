package ceri.serial.javax;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.util.BasicUtil;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * A self-healing serial connector. It will automatically reconnect if the cable is removed and
 * replaced. USB-to-serial connector device names can change after disconnecting and reconnecting.
 * The CommPortSupplier interface can be used to provide handling logic in this case.
 */
public class SelfHealingSerialConnector extends LoopingExecutor implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final CommPortSupplier commPortSupplier;
	private final SerialPortParams params;
	private final int connectionTimeoutMs;
	private final int fixRetryDelayMs;
	private final int recoveryDelayMs;
	private final Predicate<Exception> brokenPredicate;
	private final Listeners<State> listeners = new Listeners<>();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.create();
	private volatile SerialPort serialPort;

	public static class Builder {
		final CommPortSupplier commPortSupplier;
		SerialPortParams params = SerialPortParams.DEFAULT;
		int connectionTimeoutMs = 3000;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = SerialPort::isBroken;

		Builder(CommPortSupplier commPortSupplier) {
			this.commPortSupplier = commPortSupplier;
		}

		public Builder params(SerialPortParams params) {
			this.params = params;
			return this;
		}

		public Builder connectionTimeoutMs(int connectionTimeoutMs) {
			this.connectionTimeoutMs = connectionTimeoutMs;
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

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate) {
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
		params = builder.params;
		connectionTimeoutMs = builder.connectionTimeoutMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
		in.listen(this::checkIfBroken);
		out.listen(this::checkIfBroken);
		start();
	}

	/**
	 * Manually notify the connector it is broken. Useful if the connector cannot determine it is
	 * broken from IOExceptions alone.
	 */
	@Override
	public void broken() {
		setBroken();
	}

	@Override
	public void connect() throws IOException {
		initSerialPort();
	}

	@Override
	public Listenable<State> listeners() {
		return listeners;
	}

	@Override
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void setDtr(boolean state) throws IOException {
		exec(port -> port.setDTR(state));
	}

	@Override
	public void setRts(boolean state) throws IOException {
		exec(port -> port.setRTS(state));
	}

	@Override
	public void setFlowControl(FlowControl flowControl) throws IOException {
		exec(port -> port.setFlowControl(flowControl));
	}

	@Override
	public void setBreakBit(boolean on) throws IOException {
		if (on) exec(SerialPort::setBreakBit);
		else exec(SerialPort::clearBreakBit);
	}

	@SuppressWarnings("resource")
	private void exec(ExceptionConsumer<IOException, SerialPort> consumer) throws IOException {
		SerialPort serialPort = serialPort();
		try {
			consumer.accept(serialPort);
		} catch (RuntimeException | IOException e) {
			checkIfBroken(e);
			throw e;
		}
	}

	private SerialPort serialPort() throws IOException {
		SerialPort serialPort = this.serialPort;
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
		logger.info("Connection is broken - attempting to fix");
		String lastErrorMsg = null;
		while (true) {
			try {
				initSerialPort();
				break;
			} catch (IOException e) {
				String errorMsg = e.getMessage();
				if (lastErrorMsg == null || !lastErrorMsg.equals(errorMsg))
					logger.debug("Failed to fix connection, retrying: {}", errorMsg);
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

	private void checkIfBroken(Exception e) {
		if (!brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(State.broken);
	}

	private void initSerialPort() throws IOException {
		LogUtil.close(logger, serialPort);
		String commPort = commPortSupplier.get();
		serialPort = openSerialPort(commPort, connectionTimeoutMs);
		in.setInputStream(serialPort.getInputStream());
		out.setOutputStream(serialPort.getOutputStream());
	}

	private SerialPort openSerialPort(String commPort, int connectionTimeoutMs) throws IOException {
		SerialPort sp = null;
		try {
			sp = SerialPort.open(commPort, getClass().getSimpleName(), connectionTimeoutMs);
			sp.setParams(params);
			return sp;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, sp);
			throw e;
		}
	}

}
