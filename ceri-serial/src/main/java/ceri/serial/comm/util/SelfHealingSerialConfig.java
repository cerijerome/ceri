package ceri.serial.comm.util;

import static ceri.common.function.FunctionUtil.lambdaName;
import static ceri.common.function.FunctionUtil.named;
import java.io.IOException;
import java.util.function.Predicate;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealingConfig;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.SerialPort;

public class SelfHealingSerialConfig {
	public static final SelfHealingSerialConfig NULL = builder((PortSupplier) null).build();
	public static final Predicate<Exception> DEFAULT_PREDICATE =
		named(SerialPort::isBroken, "SerialPort::isBroken");
	public final PortSupplier portSupplier;
	public final SerialFactory factory;
	public final SerialConfig serial;
	public final SelfHealingConfig selfHealing;

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
		SelfHealingConfig.Builder selfHealing =
			SelfHealingConfig.builder().brokenPredicate(DEFAULT_PREDICATE);

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

		public Builder selfHealing(SelfHealingConfig selfHealing) {
			this.selfHealing.apply(selfHealing);
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
		return new Builder(config.portSupplier).factory(config.factory).serial(config.serial)
			.selfHealing(config.selfHealing);
	}

	SelfHealingSerialConfig(Builder builder) {
		portSupplier = builder.portSupplier;
		factory = builder.factory;
		serial = builder.serial;
		selfHealing = builder.selfHealing.build();
	}

	public boolean enabled() {
		return portSupplier != null;
	}

	/**
	 * Override serial params.
	 */
	public SelfHealingSerialConfig replace(SerialParams params) {
		if (params == null || this.serial.params.equals(params)) return this;
		return builder(this).serial(serial.replace(params)).build();
	}
	
	@Override
	public String toString() {
		return ToString.forClass(this, lambdaName(portSupplier), lambdaName(factory), serial,
			selfHealing);
	}

}