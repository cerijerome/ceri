package ceri.serial.ftdi.util;

import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingFtdiProperties extends BaseProperties {
	private final FtdiProperties ftdi;
	private final SelfHealingProperties selfHealing;

	public SelfHealingFtdiProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		ftdi = new FtdiProperties(this);
		selfHealing = new SelfHealingProperties(this);
	}

	public SelfHealingFtdiConfig config() {
		return SelfHealingFtdiConfig.builder().finder(ftdi.finder()).iface(ftdi.iface())
			.ftdi(ftdi.config()).selfHealing(selfHealing.config()).build();
	}
}
