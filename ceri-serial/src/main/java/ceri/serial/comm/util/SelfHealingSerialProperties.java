package ceri.serial.comm.util;

import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingSerialProperties extends Ref<BaseProperties> {
	private final SerialProperties serial;
	private final SelfHealingProperties selfHealing;

	public SelfHealingSerialProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
		serial = new SerialProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingSerial.Config config() {
		return SelfHealingSerial.Config.builder(serial.portSupplier()).serial(serial.config())
			.selfHealing(selfHealing.config()).build();
	}
}
