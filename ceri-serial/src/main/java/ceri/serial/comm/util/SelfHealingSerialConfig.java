package ceri.serial.comm.util;

import static ceri.common.function.FunctionUtil.lambdaName;
import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.util.function.Predicate;
import ceri.common.text.ToString;
import ceri.serial.comm.SerialPort;

public class SelfHealingSerialConfig {
	public static final SelfHealingSerialConfig NULL = builder((PortSupplier) null).build();
	public static final Predicate<Exception> DEFAULT_PREDICATE =
		named(SerialPort::isBroken, "SerialPort::isBroken");
	public final PortSupplier portSupplier;
	public final SerialFactory factory;
	public final SerialConfig serial;
	public final int fixRetryDelayMs;
	public final int recoveryDelayMs;
	public final Predicate<Exception> brokenPredicate;

	public static interface SerialFactory {
		SerialPort open(String port) throws IOException;
	}

	public static SelfHealingSerialConfig of(String port) {
		return builder(port).build();
	}
	
	public static class Builder {
		final PortSupplier portSupplier;
		SerialFactory factory = SerialPort::open;
		SerialConfig serial = SerialConfig.DEFAULT;
		int fixRetryDelayMs = 2000;
		int recoveryDelayMs = fixRetryDelayMs / 2;
		Predicate<Exception> brokenPredicate = DEFAULT_PREDICATE;

		Builder(PortSupplier portSupplier) {
			this.portSupplier = portSupplier;
		}

		public Builder factory(SerialFactory factory) {
			this.factory = factory;
			return this;
		}

		public Builder serial(SerialConfig serial) {
			this.serial = serial;
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

	public static Builder builder(String port) {
		return builder(PortSupplier.fixed(port));
	}

	public static Builder builder(PortSupplier portSupplier) {
		return new Builder(portSupplier);
	}

	public static Builder builder(SelfHealingSerialConfig config) {
		return new Builder(config.portSupplier).serial(config.serial)
			.fixRetryDelayMs(config.fixRetryDelayMs).recoveryDelayMs(config.recoveryDelayMs)
			.brokenPredicate(config.brokenPredicate);
	}

	SelfHealingSerialConfig(Builder builder) {
		portSupplier = builder.portSupplier;
		factory = builder.factory;
		serial = builder.serial;
		fixRetryDelayMs = builder.fixRetryDelayMs;
		recoveryDelayMs = builder.recoveryDelayMs;
		brokenPredicate = builder.brokenPredicate;
	}

	public boolean enabled() {
		return portSupplier != null;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, lambdaName(portSupplier), lambdaName(factory), serial,
			fixRetryDelayMs, recoveryDelayMs, lambdaName(brokenPredicate));
	}

}