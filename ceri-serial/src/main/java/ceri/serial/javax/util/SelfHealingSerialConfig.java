package ceri.serial.javax.util;

import static ceri.common.function.Namer.lambda;
import java.io.IOException;
import java.util.function.Predicate;
import ceri.common.function.Namer;
import ceri.common.text.ToString;
import ceri.serial.javax.SerialPort;
import ceri.serial.javax.SerialPortParams;

public class SelfHealingSerialConfig {
	public static final SelfHealingSerialConfig NULL = of((CommPortSupplier) null);
	private static final Predicate<Exception> DEFAULT_PREDICATE =
		Namer.predicate(SerialPort::isBroken, "SerialPort::isBroken");
	public final CommPortSupplier commPortSupplier;
	public final SerialFactory factory;
	public final SerialPortParams params;
	public final int connectionTimeoutMs;
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static interface SerialFactory {
		SerialPort open(String comPort, String name, int timeoutMs) throws IOException;
	}

	/**
	 * Returns config with serial params replaced.
	 */
	public static SelfHealingSerialConfig replace(SelfHealingSerialConfig config,
		SerialPortParams params) {
		return config == null ? null : config.replace(params);
	}

	public static SelfHealingSerialConfig of(String commPort) {
		return of(commPort, SerialPortParams.DEFAULT);
	}

	public static SelfHealingSerialConfig of(String commPort, SerialPortParams params) {
		return of(CommPortSupplier.fixed(commPort), params);
	}

	public static SelfHealingSerialConfig of(CommPortSupplier commPortSupplier) {
		return of(commPortSupplier, SerialPortParams.DEFAULT);
	}

	public static SelfHealingSerialConfig of(CommPortSupplier commPortSupplier,
		SerialPortParams params) {
		return new Builder(commPortSupplier).params(params).build();
	}

	public static class Builder {
		final CommPortSupplier commPortSupplier;
		SerialFactory factory = SerialPort::open;
		SerialPortParams params = SerialPortParams.DEFAULT;
		int connectionTimeoutMs = 3000;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder(CommPortSupplier commPortSupplier) {
			this.commPortSupplier = commPortSupplier;
		}

		public Builder factory(SerialFactory factory) {
			this.factory = factory;
			return this;
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

		public SelfHealingSerialConfig build() {
			return new SelfHealingSerialConfig(this);
		}
	}

	public static Builder builder(String commPort) {
		return builder(CommPortSupplier.fixed(commPort));
	}

	public static Builder builder(CommPortSupplier commPortSupplier) {
		return new Builder(commPortSupplier);
	}

	public static Builder builder(SelfHealingSerialConfig config) {
		return new Builder(config.commPortSupplier).params(config.params)
			.connectionTimeoutMs(config.connectionTimeoutMs).fixRetryDelayMs(config.fixRetryDelayMs)
			.recoveryDelayMs(config.recoveryDelayMs).brokenPredicate(config.brokenPredicate);
	}

	SelfHealingSerialConfig(Builder builder) {
		commPortSupplier = builder.commPortSupplier;
		factory = builder.factory;
		params = builder.params;
		connectionTimeoutMs = builder.connectionTimeoutMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public SerialPort open(String comPort, String name, int timeoutMs) throws IOException {
		return factory.open(comPort, name, timeoutMs);
	}

	public boolean enabled() {
		return commPortSupplier != null;
	}

	/**
	 * Returns config with serial params replaced.
	 */
	public SelfHealingSerialConfig replace(SerialPortParams params) {
		if (params == null || params.equals(this.params)) return this;
		return builder(this).params(params).build();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, lambda(commPortSupplier), lambda(factory), params,
			connectionTimeoutMs, fixRetryDelayMs, recoveryDelayMs, lambda(brokenPredicate));
	}

}