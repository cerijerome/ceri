package ceri.serial.comm.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.function.FunctionUtil;
import ceri.common.function.Lambdas;
import ceri.common.io.IoUtil;
import ceri.common.property.TypedProperties;
import ceri.common.text.ToString;
import ceri.log.io.SelfHealing;
import ceri.log.io.SelfHealingConnector;
import ceri.log.util.LogUtil;
import ceri.serial.comm.FlowControl;
import ceri.serial.comm.Serial;
import ceri.serial.comm.SerialParams;
import ceri.serial.comm.SerialPort;

/**
 * A self-healing serial port. It will automatically reconnect on fatal errors, such as if the cable
 * is removed and replaced. USB-to-serial connector device names can change after disconnecting and
 * reconnecting. The PortSupplier interface can be used to provide handling logic in this case.
 */
public class SelfHealingSerial extends SelfHealingConnector<Serial> implements Serial.Fixable {
	private final Config config;
	private final SerialConfig.Builder serialConfig;

	/**
	 * Serial port and self-healing configuration.
	 */
	public static class Config {
		public static final Config NULL = builder((PortSupplier) null).build();
		private static final Predicate<Exception> DEFAULT_PREDICATE =
			Lambdas.register(SerialPort::isFatal, "SerialPort::isFatal");
		private final Function<Config, Serial.Fixable> serialFn;
		public final PortSupplier portSupplier;
		public final SerialFactory factory;
		public final SerialConfig serial;
		public final SelfHealing.Config selfHealing;

		public static interface SerialFactory {
			Serial open(String port) throws IOException;
		}

		public static Config of(String port) {
			return builder(port).build();
		}

		public static class Builder {
			final PortSupplier portSupplier;
			Function<Config, Serial.Fixable> serialFn = SelfHealingSerial::of;
			SerialFactory factory = SerialPort::open;
			SerialConfig serial = SerialConfig.DEFAULT;
			SelfHealing.Config.Builder selfHealing =
				SelfHealing.Config.builder().brokenPredicate(DEFAULT_PREDICATE);

			Builder(PortSupplier portSupplier) {
				this.portSupplier = portSupplier;
			}

			/**
			 * Useful to override construction of the serial controller.
			 */
			public Builder serialFn(Function<Config, Serial.Fixable> serialFn) {
				this.serialFn = serialFn;
				return this;
			}

			/**
			 * Useful to override construction of the wrapped serial device.
			 */
			public Builder factory(SerialFactory factory) {
				this.factory = factory;
				return this;
			}

			public Builder serial(SerialConfig serial) {
				this.serial = serial;
				return this;
			}

			public Builder selfHealing(SelfHealing.Config selfHealing) {
				this.selfHealing.apply(selfHealing);
				return this;
			}

			public Config build() {
				return new Config(this);
			}
		}

		public static Builder builder(String port) {
			return builder(PortSupplier.fixed(port));
		}

		public static Builder builder(PortSupplier portSupplier) {
			return new Builder(portSupplier);
		}

		public static Builder builder(Config config) {
			return new Builder(config.portSupplier).serialFn(config.serialFn)
				.factory(config.factory).serial(config.serial).selfHealing(config.selfHealing);
		}

		Config(Builder builder) {
			serialFn = builder.serialFn;
			portSupplier = builder.portSupplier;
			factory = builder.factory;
			serial = builder.serial;
			selfHealing = builder.selfHealing.build();
		}

		/**
		 * Useful to override construction of the serial controller.
		 */
		public Serial.Fixable serial() {
			return serialFn.apply(this);
		}

		public boolean enabled() {
			return portSupplier != null;
		}

		/**
		 * Override serial params.
		 */
		public Config replace(SerialParams params) {
			if (params == null || this.serial.params.equals(params)) return this;
			return builder(this).serial(serial.replace(params)).build();
		}

		@Override
		public String toString() {
			return ToString.forClass(this, Lambdas.name(portSupplier), Lambdas.name(factory),
				serial, selfHealing);
		}
	}

	/**
	 * Serial port and self-healing configuration properties.
	 */
	public static class Properties extends TypedProperties.Ref {
		private final SerialConfig.Properties serial;
		private final SelfHealing.Properties selfHealing;

		public Properties(TypedProperties properties, String... groups) {
			super(properties, groups);
			serial = new SerialConfig.Properties(ref);
			selfHealing = new SelfHealing.Properties(ref);
		}

		public Config config() {
			return SelfHealingSerial.Config.builder(serial.portSupplier()).serial(serial.config())
				.selfHealing(selfHealing.config()).build();
		}
	}

	/**
	 * Create an instance from configuration.
	 */
	public static SelfHealingSerial of(Config config) {
		return new SelfHealingSerial(config);
	}

	private SelfHealingSerial(Config config) {
		super(config.selfHealing);
		this.config = config;
		serialConfig = SerialConfig.builder(config.serial);
	}

	@Override
	public String port() {
		String port = device.applyIfSet(Serial::port, null);
		if (port == null) port = FunctionUtil.getSilently(config.portSupplier::get, null);
		return port;
	}

	@Override
	public void inBufferSize(int size) {
		serialConfig.inBufferSize(size);
		device.acceptIfSet(serial -> serial.inBufferSize(size));
	}

	@Override
	public int inBufferSize() {
		return serialConfig.inBufferSize;
	}

	@Override
	public void outBufferSize(int size) {
		serialConfig.outBufferSize(size);
		device.acceptIfSet(serial -> serial.outBufferSize(size));
	}

	@Override
	public int outBufferSize() {
		return serialConfig.outBufferSize;
	}

	@Override
	public void params(SerialParams params) throws IOException {
		serialConfig.params(params);
		device.acceptIfSet(serial -> serial.params(params));
	}

	@Override
	public SerialParams params() {
		return serialConfig.params;
	}

	@Override
	public void flowControl(Collection<FlowControl> flowControl) throws IOException {
		serialConfig.flowControl(flowControl);
		device.acceptIfSet(serial -> serial.flowControl(flowControl));
	}

	@Override
	public Set<FlowControl> flowControl() {
		return serialConfig.flowControl;
	}

	@Override
	public void brk(boolean on) throws IOException {
		device.acceptValid(serial -> serial.brk(on));
	}

	@Override
	public void rts(boolean on) throws IOException {
		device.acceptValid(serial -> serial.rts(on));
	}

	@Override
	public void dtr(boolean on) throws IOException {
		device.acceptValid(serial -> serial.dtr(on));
	}

	@Override
	public boolean rts() throws IOException {
		return device.applyValid(Serial::rts);
	}

	@Override
	public boolean dtr() throws IOException {
		return device.applyValid(Serial::dtr);
	}

	@Override
	public boolean cd() throws IOException {
		return device.applyValid(Serial::cd);
	}

	@Override
	public boolean cts() throws IOException {
		return device.applyValid(Serial::cts);
	}

	@Override
	public boolean dsr() throws IOException {
		return device.applyValid(Serial::dsr);
	}

	@Override
	public boolean ri() throws IOException {
		return device.applyValid(Serial::ri);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, config);
	}

	@SuppressWarnings("resource")
	@Override
	protected Serial openConnector() throws IOException {
		Serial serial = null;
		try {
			String port = config.portSupplier.get();
			serial = config.factory.open(port);
			IoUtil.clear(serial.in());
			serialConfig.build().applyTo(serial);
			return serial;
		} catch (RuntimeException | IOException e) {
			LogUtil.close(serial);
			throw e;
		}
	}
}
