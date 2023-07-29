package ceri.log.io;

import java.io.Closeable;
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
import ceri.common.io.Fixable;
import ceri.common.io.Replaceable;
import ceri.common.io.StateChange;
import ceri.common.util.Named;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * The base logic for self-healing devices. It will automatically reconnect if the device is fatally
 * broken, as determined by the config broken predicate.
 */
public abstract class SelfHealingDevice<T extends Named & Closeable> extends LoopingExecutor
	implements Fixable {
	protected static final Logger logger = LogManager.getFormatterLogger();
	private final SelfHealingConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final BooleanCondition sync = BooleanCondition.of();
	private final AtomicBoolean open = new AtomicBoolean(false);
	protected final Replaceable.Field<T> device = Replaceable.field("device");

	protected SelfHealingDevice(SelfHealingConfig config) {
		this.config = config;
		device.errors().listen(this::checkIfBroken);
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
		LogUtil.close(device);
	}

	protected abstract T openDevice() throws IOException;

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

	@SuppressWarnings("resource")
	private synchronized void initDevice() throws IOException {
		LogUtil.close(this.device);
		var device = openDevice();
		this.device.set(device);
		logger.debug("Connected: %s", device.name());
	}
}
