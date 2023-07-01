package ceri.log.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionUtil;
import ceri.common.io.Fixable;
import ceri.common.io.StateChange;
import ceri.common.util.Named;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * The base logic for self-healing devices. It will automatically reconnect if the device is
 * fatally broken, as determined by the config broken predicate.
 */
public abstract class SelfHealingDevice<T extends Named & AutoCloseable> extends LoopingExecutor
	implements Fixable {
	protected static final Logger logger = LogManager.getFormatterLogger();
	private final SelfHealingConnectorConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final BooleanCondition sync = BooleanCondition.of();
	private final AtomicBoolean open = new AtomicBoolean(false);
	private volatile T device = null;

	protected SelfHealingDevice(SelfHealingConnectorConfig config) {
		this.config = config;
		start();
	}

	@Override
	public void broken() {
		setBroken();
	}

	/**
	 * Should only be called to initialize the connector.
	 */
	@Override
	public void open() throws IOException {
		if (open.getAndSet(true)) return; // only open once
		try {
			initDevice();
		} catch (RuntimeException | IOException e) {
			broken();
			throw e;
		}
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, device);
	}

	protected abstract T openDevice() throws IOException;

	/**
	 * Invokes the consumer with the current connector. Does nothing if the connector is not valid.
	 */
	protected <E extends Exception> void acceptDevice(ExceptionConsumer<E, T> consumer) throws E {
		var device = this.device;
		FunctionUtil.safeAccept(device, consumer);
	}

	/**
	 * Invokes the function with the current connector. Returns the given default value if the
	 * connector is not valid.
	 */
	protected <E extends Exception, R> R applyDevice(ExceptionFunction<E, T, R> function, R def)
		throws E {
		var device = this.device;
		return FunctionUtil.safeApply(device, function, def);
	}

	/**
	 * Invokes the consumer with the current connector. If the connector is not valid, an
	 * IOException is thrown. Error listeners are notified of any exception thrown by the consumer.
	 */
	protected <E extends Exception> void acceptValidDevice(ExceptionConsumer<E, T> consumer)
		throws IOException, E {
		applyValidConnector(socket -> {
			consumer.accept(socket);
			return null;
		});
	}

	/**
	 * Invokes the function with the current connector. If the connector is not valid, an
	 * IOException is thrown. Error listeners are notified of any exception thrown by the function.
	 */
	@SuppressWarnings("resource")
	protected <E extends Exception, R> R applyValidConnector(ExceptionFunction<E, T, R> function)
		throws IOException, E {
		var connector = validConnector();
		try {
			return function.apply(connector);
		} catch (Exception e) {
			checkIfBroken(e);
			throw e;
		}
	}

	/**
	 * Access the connector. Throws IOException if not connected.
	 */
	protected T validConnector() throws IOException {
		var device = this.device;
		if (device == null) throw new IOException("Device unavailable");
		return device;
	}

	protected void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		open.set(true); // no need to call open() now
		logger.info("Connector is broken, attempting to fix");
		fixDevice();
		logger.info("Connector is now fixed");
		// wait for streams to recover before clearing
		ConcurrentUtil.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixDevice() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initDevice();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix, retrying: %s", e);
				ConcurrentUtil.delay(config.fixRetryDelayMs);
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

	private void setBroken() {
		if (sync.signal()) notifyListeners(StateChange.broken);
	}

	private synchronized void initDevice() throws IOException {
		LogUtil.close(logger, device);
		device = openDevice();
		logger.debug("Connected: %s", device.name());
	}
}
