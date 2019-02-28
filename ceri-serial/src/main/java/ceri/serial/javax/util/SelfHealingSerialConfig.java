package ceri.serial.javax.util;

import java.util.function.Predicate;
import ceri.serial.javax.SerialPort;
import ceri.serial.javax.SerialPortParams;

public class SelfHealingSerialConfig {
	final CommPortSupplier commPortSupplier;
	final SerialPortParams params;
	final int connectionTimeoutMs;
	final int fixRetryDelayMs;
	final int recoveryDelayMs;
	final Predicate<Exception> brokenPredicate;

	public static SelfHealingSerialConfig of(String commPort) {
		return of(CommPortSupplier.fixed(commPort));
	}

	public static SelfHealingSerialConfig of(CommPortSupplier commPortSupplier) {
		return new Builder(commPortSupplier).build();
	}

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
	
}