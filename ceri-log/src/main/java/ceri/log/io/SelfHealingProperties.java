package ceri.log.io;

import ceri.common.function.FunctionUtil;
import ceri.common.property.BaseProperties;

public class SelfHealingProperties extends BaseProperties {
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

	public SelfHealingProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public SelfHealing.Config config() {
		var b = SelfHealing.Config.builder();
		FunctionUtil.safeAccept(fixRetryDelayMs(), b::fixRetryDelayMs);
		FunctionUtil.safeAccept(recoveryDelayMs(), b::recoveryDelayMs);
		return b.build();
	}

	private Integer fixRetryDelayMs() {
		return intValue(FIX_RETRY_DELAY_MS_KEY);
	}

	private Integer recoveryDelayMs() {
		return intValue(RECOVERY_DELAY_MS_KEY);
	}
}
