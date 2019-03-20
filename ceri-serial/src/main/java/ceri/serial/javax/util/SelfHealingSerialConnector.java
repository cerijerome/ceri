package ceri.serial.javax.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.function.ExceptionConsumer;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;
import ceri.serial.javax.FlowControl;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.SerialPort;

/**
 * A self-healing serial connector. It will automatically reconnect if the cable is removed and
 * replaced. USB-to-serial connector device names can change after disconnecting and reconnecting.
 * The CommPortSupplier interface can be used to provide handling logic in this case.
 */
public class SelfHealingSerialConnector extends LoopingExecutor implements SerialConnector {
	private static final Logger logger = LogManager.getLogger();
	private final SelfHealingSerialConfig config;
	private final Listeners<StateChange> listeners = new Listeners<>();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.of();
	private volatile SerialPort serialPort;

	public static SelfHealingSerialConnector of(SelfHealingSerialConfig config) {
		return new SelfHealingSerialConnector(config);
	}

	private SelfHealingSerialConnector(SelfHealingSerialConfig config) {
		this.config = config;
		in.listeners().listen(this::checkIfBroken);
		out.listeners().listen(this::checkIfBroken);
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
		try {
			initSerialPort();
		} catch (IOException e) {
			broken();
			throw e;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
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
	public String toString() {
		return ToStringHelper.createByClass(this, config).toString();
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Connection is broken - attempting to fix");
		fixSerialPort();
		logger.info("Connection is now fixed");
		// wait for streams to recover before clearing
		BasicUtil.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixSerialPort() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initSerialPort();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix connection, retrying:", e);
				BasicUtil.delay(config.fixRetryDelayMs);
			}
		}
	}

	private void notifyListeners(StateChange state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	private void initSerialPort() throws IOException {
		LogUtil.close(logger, serialPort);
		String commPort = config.commPortSupplier.get();
		serialPort = openSerialPort(commPort, config.connectionTimeoutMs);
		logger.debug("Connected to {}", commPort);
		in.setInputStream(serialPort.getInputStream());
		out.setOutputStream(serialPort.getOutputStream());
	}

	private SerialPort openSerialPort(String commPort, int connectionTimeoutMs) throws IOException {
		SerialPort sp = null;
		try {
			sp = SerialPort.open(commPort, getClass().getSimpleName(), connectionTimeoutMs);
			sp.setParams(config.params);
			return sp;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(logger, sp);
			throw e;
		}
	}

}
