package ceri.log.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.event.Listenable;
import ceri.common.event.Listeners;
import ceri.common.exception.ExceptionTracker;
import ceri.common.function.Lambdas;
import ceri.common.function.Predicates;
import ceri.common.io.Fixable;
import ceri.common.io.Replaceable;
import ceri.common.io.StateChange;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.common.util.Named;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.LogUtil;

/**
 * The base logic for self-healing devices. It will automatically reconnect if the device is fatally
 * broken, as determined by the config broken predicate.
 */
public abstract class SelfHealing<T extends Named & Closeable> extends LoopingExecutor
	implements Fixable {
	protected static final Logger logger = LogManager.getFormatterLogger();
	private final Config config;
	private final Listeners<StateChange> listeners = Listeners.of();
	private final BoolCondition sync = BoolCondition.of();
	private final AtomicBoolean open = new AtomicBoolean(false);
	protected final Replaceable.Field<T> device = Replaceable.field("device");

	public static class Config {
		public static final Predicate<Exception> NULL_PREDICATE = Predicates.no();
		public static final Config DEFAULT = new Builder().build();
		public static final Config NULL = of(0, 0, NULL_PREDICATE);
		public final int fixRetryDelayMs;
		public final int recoveryDelayMs;
		public final Predicate<Exception> brokenPredicate;

		public static Config of(int fixRetryDelayMs, int recoveryDelayMs,
			Predicate<Exception> brokenPredicate) {
			return builder().fixRetryDelayMs(fixRetryDelayMs).recoveryDelayMs(recoveryDelayMs)
				.brokenPredicate(brokenPredicate).build();
		}

		public static class Builder {
			int fixRetryDelayMs = 2000;
			int recoveryDelayMs = fixRetryDelayMs / 2;
			Predicate<Exception> brokenPredicate = NULL_PREDICATE;

			Builder() {}

			public Builder apply(Config config) {
				if (config.hasBrokenPredicate()) brokenPredicate(config.brokenPredicate);
				return fixRetryDelayMs(config.fixRetryDelayMs)
					.recoveryDelayMs(config.recoveryDelayMs);
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

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder() {
			return new Builder();
		}

		Config(Builder builder) {
			fixRetryDelayMs = builder.fixRetryDelayMs;
			recoveryDelayMs = builder.recoveryDelayMs;
			brokenPredicate = builder.brokenPredicate;
		}

		public boolean broken(Exception e) {
			return brokenPredicate.test(e);
		}

		public boolean hasBrokenPredicate() {
			return brokenPredicate != NULL_PREDICATE;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, fixRetryDelayMs, recoveryDelayMs,
				Lambdas.name(brokenPredicate));
		}
	}

	public static class Properties extends TypedProperties.Ref {
		private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
		private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		public Config config() {
			var b = Config.builder();
			parse(FIX_RETRY_DELAY_MS_KEY).asInt().accept(b::fixRetryDelayMs);
			parse(RECOVERY_DELAY_MS_KEY).asInt().accept(b::recoveryDelayMs);
			return b.build();
		}
	}

	protected SelfHealing(Config config) {
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
