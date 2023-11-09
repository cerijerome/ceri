package ceri.x10.cm11a;

import ceri.common.io.Connector;
import ceri.common.io.DeviceMode;
import ceri.common.text.ToString;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11a;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;

/**
 * Configuration for a Cm11a container.
 */
public class Cm11aConfig {
	public final int id;
	public final DeviceMode mode;
	public final Cm11aDeviceConfig device;
	public final SelfHealingSerialConfig serial;

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

	/**
	 * Convenience constructor for simple case.
	 */
	public static Cm11aConfig of(String commPort) {
		// Container overrides serial port params
		return builder().serial(SelfHealingSerialConfig.of(commPort)).build();
	}

	public static class Builder {
		int id = 1;
		DeviceMode mode = DeviceMode.enabled;
		Cm11aDeviceConfig device = Cm11aDeviceConfig.DEFAULT;
		SelfHealingSerialConfig serial = SelfHealingSerialConfig.NULL;

		Builder() {}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder mode(DeviceMode mode) {
			this.mode = mode;
			return this;
		}

		public Builder device(Cm11aDeviceConfig device) {
			this.device = device;
			return this;
		}

		public Builder serial(SelfHealingSerialConfig deviceSerial) {
			this.serial = deviceSerial;
			return this;
		}

		public Cm11aConfig build() {
			return new Cm11aConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Cm11aConfig(Builder builder) {
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
