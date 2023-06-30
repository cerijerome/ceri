package ceri.log.io;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;

public class SelfHealingConnectorProperties extends BaseProperties {
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

	public SelfHealingConnectorProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public SelfHealingConnectorConfig config() {
		SelfHealingConnectorConfig.Builder b = SelfHealingConnectorConfig.builder();
		safeAccept(fixRetryDelayMs(), b::fixRetryDelayMs);
		safeAccept(recoveryDelayMs(), b::recoveryDelayMs);
		return b.build();
	}

	private Integer fixRetryDelayMs() {
		return intValue(FIX_RETRY_DELAY_MS_KEY);
	}

	private Integer recoveryDelayMs() {
		return intValue(RECOVERY_DELAY_MS_KEY);
	}
}
