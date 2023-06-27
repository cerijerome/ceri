package ceri.log.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.io.Connector;
import ceri.common.io.ReplaceableInputStream;
import ceri.common.io.ReplaceableOutputStream;
import ceri.common.io.StateChange;
import ceri.common.text.ToString;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * The base logic for self-healing connectors. It will automatically reconnect if the connector is
 * fatally broken, as determined by the config broken predicate.
 */
public abstract class SelfHealingConnector<T extends Connector.Fixable> extends LoopingExecutor
	implements Connector.Fixable {
	protected static final Logger logger = LogManager.getFormatterLogger();
	private final SelfHealingConnectorConfig config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final ReplaceableInputStream in = new ReplaceableInputStream();
	private final ReplaceableOutputStream out = new ReplaceableOutputStream();
	private final BooleanCondition sync = BooleanCondition.of();
	private final AtomicBoolean open = new AtomicBoolean(false);
	private volatile T connector = null;

	protected SelfHealingConnector(SelfHealingConnectorConfig config) {
		this.config = config;
		in.listeners().listen(this::checkIfBroken);
		out.listeners().listen(this::checkIfBroken);
		start();
	}

	@Override
	public void broken() {
		setBroken();
	}

	@Override
	public void open() throws IOException {
		if (open.getAndSet(true)) return; // only open once
		try {
			initConnector();
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
	public InputStream in() {
		return in;
	}

	@Override
	public OutputStream out() {
		return out;
	}

	@Override
	public void close() {
		super.close();
		LogUtil.close(logger, connector);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, connector);
	}

	protected abstract T openConnector() throws IOException;

	@Override
	protected void loop() throws InterruptedException {
		sync.awaitPeek();
		logger.info("Connector is broken - attempting to fix");
		fixConnector();
		logger.info("Connector is now fixed");
		// wait for streams to recover before clearing
		ConcurrentUtil.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(StateChange.fixed);
	}

	private void fixConnector() {
		ExceptionTracker exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initConnector();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix connector, retrying: {}", e);
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

	private void checkIfBroken(Exception e) {
		if (!config.brokenPredicate.test(e)) return;
		if (sync.isSet()) return;
		setBroken();
	}

	private void setBroken() {
		sync.signal();
		notifyListeners(StateChange.broken);
	}

	@SuppressWarnings("resource")
	private void initConnector() throws IOException {
		LogUtil.close(logger, connector);
		connector = openConnector();
		logger.debug("Connected to {}", connector.name());
		in.setInputStream(connector.in());
		out.setOutputStream(connector.out());
	}

}
