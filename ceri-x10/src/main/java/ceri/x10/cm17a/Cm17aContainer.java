package ceri.x10.cm17a;

import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.DeviceMode;
import ceri.common.io.Fixable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.comm.Serial;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.x10.cm17a.device.Cm17a;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aEmulator;

/**
 * Container for a cm17a controller.
 */
public class Cm17aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getFormatterLogger();
	public final int id;
	public final Type type;
	private final Serial.Fixable createdSerial;
	private final Serial.Fixable serial;
	private final Cm17a createdCm17a;
	public final Cm17a cm17a;

	/**
	 * The container type, determined by references and config.
	 */
	public static enum Type {
		cm17aRef,
		serialRef,
		serial,
		test,
		noOp;
	}

	public static class Config {
		public final int id;
		public final DeviceMode mode;
		public final Cm17aDevice.Config device;
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
			Cm17aDevice.Config device = Cm17aDevice.Config.DEFAULT;
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

			public Builder device(Cm17aDevice.Config device) {
				this.device = device;
				return this;
			}

			public Builder serial(SelfHealingSerial.Config serial) {
				this.serial = serial;
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

		public Type type(Cm17a cm17aRef, Serial.Fixable serialRef) {
			if (cm17aRef != null) return Type.cm17aRef;
			if (serialRef != null) return Type.serialRef;
			if (mode == DeviceMode.enabled && serial.enabled()) return Type.serial;
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
	public static Cm17aContainer of(Config config) {
		return new Cm17aContainer(config, null, null);
	}

	/**
	 * Container with id for the given controller.
	 */
	public static Cm17aContainer of(int id, Cm17a cm17a) {
		return new Cm17aContainer(Config.builder().id(id).build(), cm17a, null);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, Serial.Fixable serial) {
		return of(id, serial, Cm17aDevice.Config.DEFAULT);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, Serial.Fixable serial, Cm17aDevice.Config device) {
		return new Cm17aContainer(Config.builder().id(id).device(device).build(), null, serial);
	}

	private Cm17aContainer(Config config, Cm17a cm17a, Serial.Fixable serial) {
		try {
			id = config.id;
			type = config.type(cm17a, serial);
			createdSerial = createSerial(config.serial);
			this.serial = defaultValue(createdSerial, serial);
			createdCm17a = createCm17a(this.serial, config.device);
			this.cm17a = defaultValue(createdCm17a, cm17a);
			logger.info("[%d:%s] started", id, type);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		LogUtil.close(createdCm17a, createdSerial);
		logger.info("[%d:%s] stopped", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, cm17a, serial);
	}

	@SuppressWarnings("resource")
	private Serial.Fixable createSerial(SelfHealingSerial.Config config) {
		return type == Type.serial ? Fixable.openSilently(config.serial()) : null;
	}

	private Cm17a createCm17a(Serial.Fixable serial, Cm17aDevice.Config config) {
		return switch (type) {
			case cm17aRef -> null;
			case serialRef, serial -> Cm17aDevice.of(config, Cm17aConnector.of(serial));
			case test -> Cm17aEmulator.of(config.queuePollTimeoutMs);
			default -> Cm17a.NULL;
		};
	}
}
