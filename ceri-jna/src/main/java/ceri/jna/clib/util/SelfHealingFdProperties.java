package ceri.jna.clib.util;

import ceri.common.property.BaseProperties;
import ceri.common.util.Ref;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingFdProperties extends Ref<BaseProperties> {
	private final FileDescriptorProperties fd;
	private final SelfHealingProperties selfHealing;

	public SelfHealingFdProperties(BaseProperties properties, String... groups) {
		super(BaseProperties.from(properties, groups));
		fd = new FileDescriptorProperties(ref);
		selfHealing = new SelfHealingProperties(ref);
	}

	public SelfHealingFd.Config config() {
		return SelfHealingFd.Config.builder(fd.opener()).selfHealing(selfHealing.config()).build();
	}
}
