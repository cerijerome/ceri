package ceri.serial.ftdi.util;

import ceri.common.function.FunctionUtil;
import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingFtdiProperties extends Ref<BaseProperties> {
	private final FtdiProperties ftdi;
	private final SelfHealingProperties selfHealing;

	public SelfHealingFtdiProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
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
