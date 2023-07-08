package ceri.jna.clib.util;

import ceri.common.property.BaseProperties;
import ceri.log.io.SelfHealingConnectorProperties;

public class SelfHealingFileDescriptorProperties extends BaseProperties {
	private final FileDescriptorProperties fd;
	private final SelfHealingConnectorProperties selfHealing;

	public SelfHealingFileDescriptorProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		fd = new FileDescriptorProperties(this);
		selfHealing = new SelfHealingConnectorProperties(this);
	}

	public SelfHealingFileDescriptorConfig config() {
		return SelfHealingFileDescriptorConfig.builder(fd.opener())
			.selfHealing(selfHealing.config()).build();
	}
}
