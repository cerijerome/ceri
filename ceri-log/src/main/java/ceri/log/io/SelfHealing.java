package ceri.log.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.Listenable;
import ceri.common.concurrent.Listeners;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.except.ExceptionTracker;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.io.Device;
import ceri.common.io.Fixable;
import ceri.common.io.Replaceable;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.common.util.Capability;
import ceri.log.concurrent.LoopingExecutor;
import ceri.log.util.Logs;

/**
 * The base logic for self-healing devices. It will automatically reconnect if the device is fatally
 * broken, as determined by the config broken predicate.
 */
public abstract class SelfHealing<T extends Capability.Name & Closeable> extends LoopingExecutor
	implements Fixable {
	protected static final Logger logger = LogManager.getFormatterLogger();
	private final Config config;
	private final Listeners<Device.Change> listeners = Listeners.of();
	private final BoolCondition sync = BoolCondition.of();
	private final AtomicBoolean open = new AtomicBoolean(false);
	protected final Replaceable.Field<T> device = Replaceable.ofField("device");

	/**
	 * Self-healing configuration.
	 */
	public static class Config {
		public static final Config DEFAULT = new Builder().build();
		public static final Config NULL = of(0, 0, Filters.NO);
		public final int fixRetryDelayMs;
		public final int recoveryDelayMs;
		public final Functions.Predicate<? super Exception> brokenPredicate;

		/**
		 * Convenience constructor.
		 */
		public static Config of(int fixRetryDelayMs, int recoveryDelayMs,
			Functions.Predicate<? super Exception> brokenPredicate) {
			return builder().fixRetryDelayMs(fixRetryDelayMs).recoveryDelayMs(recoveryDelayMs)
				.brokenPredicate(brokenPredicate).build();
		}

		/**
		 * Builds the configuration.
		 */
		public static class Builder {
			int fixRetryDelayMs = 2000;
			int recoveryDelayMs = fixRetryDelayMs / 2;
			Functions.Predicate<? super Exception> brokenPredicate = Filters.NO;

			Builder() {}

			/**
			 * Applies the configuration. Only applies the predicate if overridden.
			 */
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

			public Builder brokenPredicate(Functions.Predicate<? super Exception> brokenPredicate) {
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

		/**
		 * Returns true if the predicate determines the exception indicates a break.
		 */
		public boolean broken(Exception e) {
			return brokenPredicate.test(e);
		}

		/**
		 * Returns true if the broken predicate has been overridden.
		 */
		public boolean hasBrokenPredicate() {
			return brokenPredicate != NULL.brokenPredicate;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, fixRetryDelayMs, recoveryDelayMs,
				Lambdas.name(brokenPredicate));
		}
	}

	/**
	 * Properties for generating config.
	 */
	public static class Properties extends TypedProperties.Ref {
		private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
		private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
		}

		/**
		 * 
		 */
		public Config config() {
			var b = Config.builder();
			parse(FIX_RETRY_DELAY_MS_KEY).asInt().accept(b::fixRetryDelayMs);
			parse(RECOVERY_DELAY_MS_KEY).asInt().accept(b::recoveryDelayMs);
			return b.build();
		}
	}

	/**
	 * The base logic for self-healing connectors. It will automatically reconnect if the connector
	 * is fatally broken, as determined by the config broken predicate.
	 */
	public static abstract class Connector<T extends ceri.common.io.Connector>
		extends SelfHealing<T> implements ceri.common.io.Connector.Fixable {
		private final Replaceable.In in = Replaceable.ofIn();
		private final Replaceable.Out out = Replaceable.ofOut();

		protected Connector(SelfHealing.Config config) {
			super(config);
			in.errors().listen(this::checkIfBroken);
			out.errors().listen(this::checkIfBroken);
		}

		@Override
		public InputStream in() {
			return in;
		}

		@Override
		public OutputStream out() {
			return out;
		}

		/**
		 * Opens the connector.
		 */
		protected abstract T openConnector() throws IOException;

		@SuppressWarnings("resource")
		@Override
		protected T openDevice() throws IOException {
			T connector = openConnector();
			in.set(connector.in());
			out.set(connector.out());
			return connector;
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
	public Listenable<Device.Change> listeners() {
		return listeners;
	}

	@Override
	public void close() {
		super.close();
		Logs.close(device);
	}

	/**
	 * Opens and initializes the device.
	 */
	protected abstract T openDevice() throws IOException;

	/**
	 * Makes sure the healing process has started if the exception indicates a break.
	 */
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
		Concurrent.delay(config.recoveryDelayMs);
		sync.clear();
		notifyListeners(Device.Change.fixed);
	}

	private void fixDevice() {
		var exceptions = ExceptionTracker.of();
		while (true) {
			try {
				initDevice();
				break;
			} catch (IOException e) {
				if (exceptions.add(e)) logger.error("Failed to fix, retrying: %s", e);
				Concurrent.delay(config.fixRetryDelayMs);
			}
		}
	}

	private void notifyListeners(Device.Change state) {
		try {
			listeners.accept(state);
		} catch (RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			logger.catching(e);
		}
	}

	private void setBroken() {
		if (sync.signal()) notifyListeners(Device.Change.broken);
	}

	@SuppressWarnings("resource")
	private synchronized void initDevice() throws IOException {
		Logs.close(this.device);
		var device = openDevice();
		this.device.set(device);
		logger.debug("Connected: %s", device.name());
	}
}
