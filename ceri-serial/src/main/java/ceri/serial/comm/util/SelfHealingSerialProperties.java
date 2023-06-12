package ceri.serial.comm.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;

public class SelfHealingSerialProperties extends BaseProperties {
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";
	private final SerialProperties serial;

	public SelfHealingSerialProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		serial = new SerialProperties(this);
	}

	public SelfHealingSerialConfig config() {
		SelfHealingSerialConfig.Builder b =
			SelfHealingSerialConfig.builder(serial.portSupplier()).serial(serial.config());
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
