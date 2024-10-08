package ceri.serial.ftdi.util;

import ceri.common.function.FunctionUtil;
import ceri.common.property.TypedProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingFtdiProperties extends TypedProperties.Ref {
	private final FtdiProperties ftdi;
	private final SelfHealingProperties selfHealing;

	public SelfHealingFtdiProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
		ftdi = new FtdiProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingFtdi.Config config() {
		var b = SelfHealingFtdi.Config.builder();
		FunctionUtil.safeAccept(ftdi.finder(), b::finder);
		FunctionUtil.safeAccept(ftdi.iface(), b::iface);
		return b.ftdi(ftdi.config()).selfHealing(selfHealing.config()).build();
	}
}
