package ceri.serial.comm.util;

import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingSerialProperties extends Ref<TypedProperties> {
	private final SerialProperties serial;
	private final SelfHealingProperties selfHealing;

	public SelfHealingSerialProperties(TypedProperties properties, String... groups) {
		super(TypedProperties.from(properties, groups));
		serial = new SerialProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingSerial.Config config() {
		return SelfHealingSerial.Config.builder(serial.portSupplier()).serial(serial.config())
			.selfHealing(selfHealing.config()).build();
	}
}
