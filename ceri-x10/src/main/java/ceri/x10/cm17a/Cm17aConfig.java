package ceri.x10.cm17a;

import ceri.common.io.DeviceMode;
import ceri.common.text.ToString;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;

/**
 * Configuration for a CM17a container.
 */
public class Cm17aConfig {
	public final int id;
	public final DeviceMode mode;
	public final Cm17aDeviceConfig device;
	public final SelfHealingSerialConfig serial;

	/**
	 * Convenience constructor for simple case.
	 */
	public static Cm17aConfig of(String commPort) {
		// Container overrides serial port params
		return builder().serial(SelfHealingSerialConfig.of(commPort)).build();
	}

	public static class Builder {
		int id = 1;
		DeviceMode mode = DeviceMode.enabled;
		Cm17aDeviceConfig device = Cm17aDeviceConfig.DEFAULT;
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

		public Builder serial(SelfHealingSerialConfig serial) {
			this.serial = serial;
			return this;
		}

		public Builder device(Cm17aDeviceConfig device) {
			this.device = device;
			return this;
		}

		public Cm17aConfig build() {
			return new Cm17aConfig(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Cm17aConfig(Builder builder) {
		id = builder.id;
		mode = builder.mode;
		serial = builder.serial;
		device = builder.device;
	}

	public boolean isTest() {
		return mode == DeviceMode.test;
	}

	public boolean isDevice() {
		return mode == DeviceMode.enabled && serial.enabled();
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, mode, device, serial);
	}

}
