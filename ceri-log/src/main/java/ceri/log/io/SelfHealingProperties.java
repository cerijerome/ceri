package ceri.log.io;

import ceri.common.property.TypedProperties;

public class SelfHealingProperties extends TypedProperties.Ref {
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

	public SelfHealingProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public SelfHealing.Config config() {
		var b = SelfHealing.Config.builder();
		parse(FIX_RETRY_DELAY_MS_KEY).asInt().accept(b::fixRetryDelayMs);
		parse(RECOVERY_DELAY_MS_KEY).asInt().accept(b::recoveryDelayMs);
		return b.build();
	}
}
