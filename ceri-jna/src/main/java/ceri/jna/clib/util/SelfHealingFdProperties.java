package ceri.jna.clib.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import static ceri.common.function.FunctionUtil.safeApply;
import java.util.List;
import ceri.common.property.BaseProperties;
import ceri.jna.clib.Mode;
import ceri.jna.clib.OpenFlag;

public class SelfHealingFdProperties extends BaseProperties {
	private static final String PATH_KEY = "path";
	private static final String MODE_KEY = "mode";
	private static final String OPEN_FLAGS_KEY = "open.flags";
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";

	public SelfHealingFdProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public SelfHealingFdConfig config() {
		SelfHealingFdConfig.Builder b = SelfHealingFdConfig.builder(path(), mode(), openFlags());
		safeAccept(fixRetryDelayMs(), b::fixRetryDelayMs);
		safeAccept(recoveryDelayMs(), b::recoveryDelayMs);
		return b.build();
	}

	private String path() {
		return value(PATH_KEY);
	}

	private Mode mode() {
		return safeApply(intValue(MODE_KEY), Mode::of, Mode.NONE);
	}

	private List<OpenFlag> openFlags() {
		return enumValues(OpenFlag.class, OPEN_FLAGS_KEY);
	}

	private Integer fixRetryDelayMs() {
		return intValue(FIX_RETRY_DELAY_MS_KEY);
	}

	private Integer recoveryDelayMs() {
		return intValue(RECOVERY_DELAY_MS_KEY);
	}

}
