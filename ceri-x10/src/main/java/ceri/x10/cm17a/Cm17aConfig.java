package ceri.x10.cm17a;

import ceri.common.io.DeviceMode;
import ceri.common.text.ToStringHelper;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;

/**
 * Configuration for a CM17a container.
 */
public class Cm17aConfig {
	public final int id;
	public final DeviceMode mode;
	public final Cm17aDeviceConfig device;
	public final SelfHealingSerialConfig deviceSerial;

	/**
	 * Convenience constructor for simple case.
	 */
	public static Cm17aConfig of(String commPort) {
		// Container overrides serial port params
		return builder().deviceSerial(SelfHealingSerialConfig.of(commPort)).build();
	}

	public static class Builder {
		int id = 1;
		DeviceMode mode = DeviceMode.enabled;
		Cm17aDeviceConfig device = Cm17aDeviceConfig.DEFAULT;
		SelfHealingSerialConfig deviceSerial = SelfHealingSerialConfig.NULL;

		Builder() {}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder mode(DeviceMode mode) {
			this.mode = mode;
			return this;
		}

		public Builder deviceSerial(SelfHealingSerialConfig deviceSerial) {
			this.deviceSerial = deviceSerial;
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
		deviceSerial = builder.deviceSerial;
		device = builder.device;
	}

	public boolean isTest() {
		return mode == DeviceMode.test;
	}

	public boolean isDevice() {
		return mode == DeviceMode.enabled && deviceSerial.enabled();
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, id, mode, device, deviceSerial).toString();
	}

}
