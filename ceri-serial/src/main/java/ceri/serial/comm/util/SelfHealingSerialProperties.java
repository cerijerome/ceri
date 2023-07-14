package ceri.serial.comm.util;

import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingSerialProperties extends BaseProperties {
	private final SerialProperties serial;
	private final SelfHealingProperties selfHealing;

	public SelfHealingSerialProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		serial = new SerialProperties(this);
		selfHealing = new SelfHealingProperties(this);
	}

	public SelfHealingSerialConfig config() {
		return SelfHealingSerialConfig.builder(serial.portSupplier()).serial(serial.config())
			.selfHealing(selfHealing.config()).build();
	}
}
