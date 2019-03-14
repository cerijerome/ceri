package ceri.serial.javax.util;

import static ceri.common.function.FunctionUtil.namedPredicate;
import java.util.function.Predicate;
import ceri.common.text.ToStringHelper;
import ceri.serial.javax.SerialPort;
import ceri.serial.javax.SerialPortParams;

public class SelfHealingSerialConfig {
	public static final SelfHealingSerialConfig NULL = builder((CommPortSupplier) null).build();
	static final Predicate<Exception> DEFAULT_PREDICATE =
		namedPredicate(SerialPort::isBroken, "SerialPort::isBroken");
	final CommPortSupplier commPortSupplier;
	final SerialPortParams params;
	final int connectionTimeoutMs;
	final int fixRetryDelayMs;
	final int recoveryDelayMs;
	final Predicate<Exception> brokenPredicate;

	/**
	 * Returns config with serial params replaced.
	 */
	public static SelfHealingSerialConfig replace(SelfHealingSerialConfig config,
		SerialPortParams params) {
		if (config == null || params == null) return config;
		if (config.params.equals(params)) return config;
		return SelfHealingSerialConfig.builder(config).params(params).build();
	}

	public static SelfHealingSerialConfig of(String commPort) {
		return of(CommPortSupplier.fixed(commPort));
	}

	public static SelfHealingSerialConfig of(String commPort, SerialPortParams params) {
		return of(CommPortSupplier.fixed(commPort), params);
	}

	public static SelfHealingSerialConfig of(CommPortSupplier commPortSupplier) {
		return new Builder(commPortSupplier).build();
	}

	public static SelfHealingSerialConfig of(CommPortSupplier commPortSupplier,
		SerialPortParams params) {
		return new Builder(commPortSupplier).params(params).build();
	}

	public static class Builder {
		final CommPortSupplier commPortSupplier;
		SerialPortParams params = SerialPortParams.DEFAULT;
		int connectionTimeoutMs = 3000;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

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

		public Builder brokenPredicate(Predicate<Exception> brokenPredicate, String name) {
			this.brokenPredicate = namedPredicate(brokenPredicate, name);
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
		params = builder.params;
		connectionTimeoutMs = builder.connectionTimeoutMs;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public boolean enabled() {
		return commPortSupplier != null;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, commPortSupplier, params, connectionTimeoutMs,
			fixRetryDelayMs, recoveryDelayMs, brokenPredicate).toString();
	}

}