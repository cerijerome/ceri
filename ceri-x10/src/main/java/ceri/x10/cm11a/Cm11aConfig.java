package ceri.x10.cm11a;

import ceri.common.io.DeviceMode;
import ceri.common.text.ToStringHelper;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;

/**
 * Configuration for an CM11a container.
 */
public class Cm11aConfig {
	public final int id;
	public final DeviceMode mode;
	public final Cm11aDeviceConfig device;
	public final SelfHealingSerialConfig deviceSerial;

	/**
	 * Convenience constructor for simple case.
	 */
	public static Cm11aConfig of(String commPort) {
		// Container overrides serial port params
		return builder().deviceSerial(SelfHealingSerialConfig.of(commPort)).build();
	}

	public static class Builder {
		int id = 1;
		DeviceMode mode = DeviceMode.enabled;
		Cm11aDeviceConfig device = Cm11aDeviceConfig.DEFAULT;
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

		public Builder device(Cm11aDeviceConfig device) {
			this.device = device;
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
		deviceSerial = builder.deviceSerial;
		device = builder.device;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, id, mode, device, deviceSerial).toString();
	}

}
