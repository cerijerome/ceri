package ceri.jna.clib.util;

import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingProperties;

public class SelfHealingFdProperties extends BaseProperties {
	private final FileDescriptorProperties fd;
	private final SelfHealingProperties selfHealing;

	public SelfHealingFdProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		fd = new FileDescriptorProperties(this);
		selfHealing = new SelfHealingProperties(this);
	}

	public SelfHealingFdConfig config() {
		return SelfHealingFdConfig.builder(fd.opener())
			.selfHealing(selfHealing.config()).build();
	}
}
