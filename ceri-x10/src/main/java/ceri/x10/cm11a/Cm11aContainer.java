package ceri.x10.cm11a;

import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.Connector;
import ceri.common.io.DeviceMode;
import ceri.common.io.Fixable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.x10.cm11a.device.Cm11a;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm11a.device.Cm11aEmulator;

/**
 * Container for a cm11a controller.
 */
public class Cm11aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getFormatterLogger();
	public final int id;
	public final Type type;
	private final Connector.Fixable createdConnector;
	private final Connector.Fixable connector;
	private final Cm11a createdCm11a;
	public final Cm11a cm11a;

	/**
	 * The container type, determined by references and config.
	 */
	public static enum Type {
		cm11aRef,
		connectorRef,
		connector,
		test,
		noOp;
	}

	public static class Config {
		public final int id;
		public final DeviceMode mode;
		public final Cm11aDevice.Config device;
		public final SelfHealingSerial.Config serial;

		/**
		 * Convenience constructor for simple case.
		 */
		public static Config of(String commPort) {
			// Container overrides serial port params
			return builder().serial(SelfHealingSerial.Config.of(commPort)).build();
		}

		public static class Builder {
			int id = 1;
			DeviceMode mode = DeviceMode.enabled;
			Cm11aDevice.Config device = Cm11aDevice.Config.DEFAULT;
			SelfHealingSerial.Config serial = SelfHealingSerial.Config.NULL;

			Builder() {}

			public Builder id(int id) {
				this.id = id;
				return this;
			}

			public Builder mode(DeviceMode mode) {
				this.mode = mode;
				return this;
			}

			public Builder device(Cm11aDevice.Config device) {
				this.device = device;
				return this;
			}

			public Builder serial(SelfHealingSerial.Config deviceSerial) {
				this.serial = deviceSerial;
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
			id = builder.id;
			mode = builder.mode;
			device = builder.device;
			serial = builder.serial;
		}

		public Type type(Cm11a cm11aRef, Connector.Fixable connectorRef) {
			if (cm11aRef != null) return Type.cm11aRef;
			if (connectorRef != null) return Type.connectorRef;
			if (mode == DeviceMode.enabled && serial.enabled()) return Type.connector;
			if (mode == DeviceMode.test) return Type.test;
			return Type.noOp;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, id, mode, device, serial);
		}
	}

	/**
	 * Constructs the controller and connector.
	 */
	public static Cm11aContainer of(Config config) {
		return new Cm11aContainer(config, null, null);
	}

	/**
	 * Container with id for the given controller.
	 */
	public static Cm11aContainer of(int id, Cm11a cm11a) {
		return new Cm11aContainer(Config.builder().id(id).build(), cm11a, null);
	}

	/**
	 * Constructs a controller for the given connector.
	 */
	public static Cm11aContainer of(int id, Connector.Fixable connector) {
		return new Cm11aContainer(Config.builder().id(id).build(), null, connector);
	}

	/**
	 * Constructs a controller for the given connector.
	 */
	public static Cm11aContainer of(int id, Connector.Fixable connector,
		Cm11aDevice.Config device) {
		return new Cm11aContainer(Config.builder().id(id).device(device).build(), null, connector);
	}

	private Cm11aContainer(Config config, Cm11a cm11a, Connector.Fixable connector) {
		try {
			id = config.id;
			type = config.type(cm11a, connector);
			createdConnector = createConnector(config.serial);
			this.connector = defaultValue(createdConnector, connector);
			createdCm11a = createCm11a(this.connector, config.device);
			this.cm11a = defaultValue(createdCm11a, cm11a);
			logger.info("[%d:%s] started", id, type);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		LogUtil.close(createdCm11a, createdConnector);
		logger.info("[%d:%s] stopped", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, connector);
	}

	@SuppressWarnings("resource")
	private Connector.Fixable createConnector(SelfHealingSerial.Config config) {
		return type == Type.connector ?
			Fixable.openSilently(config.replace(Cm11a.SERIAL).serial()) : null;
	}

	@SuppressWarnings("resource")
	private Cm11a createCm11a(Connector.Fixable connector, Cm11aDevice.Config config) {
		return switch (type) {
			case cm11aRef -> null;
			case connectorRef, connector -> Cm11aDevice.of(config, connector);
			case test -> Cm11aEmulator.of(config.queuePollTimeoutMs);
			default -> Cm11a.NULL;
		};
	}
}
