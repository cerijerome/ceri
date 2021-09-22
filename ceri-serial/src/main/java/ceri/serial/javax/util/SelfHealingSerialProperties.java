package ceri.serial.javax.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;
import ceri.serial.javax.SerialPortProperties;

public class SelfHealingSerialProperties extends BaseProperties {
	private static final String USB_KEY = "usb";
	private static final String PORT_KEY = "port";
	private static final String CONNECTION_TIMEOUT_MS_KEY = "connection.timeout.ms";
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";
	private final CommPortProperties commPort;
	private final SerialPortProperties params;

	public SelfHealingSerialProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		commPort = new CommPortProperties(this, USB_KEY);
		params = new SerialPortProperties(this, PORT_KEY);
	}

	public SelfHealingSerialConfig config() {
		SelfHealingSerialConfig.Builder b =
			SelfHealingSerialConfig.builder(commPort.supplier()).params(params.params());
		safeAccept(connectionTimeoutMs(), b::connectionTimeoutMs);
		safeAccept(fixRetryDelayMs(), b::fixRetryDelayMs);
		safeAccept(recoveryDelayMs(), b::recoveryDelayMs);
		return b.build();
	}

	private Integer connectionTimeoutMs() {
		return intValue(CONNECTION_TIMEOUT_MS_KEY);
	}

	private Integer fixRetryDelayMs() {
		return intValue(FIX_RETRY_DELAY_MS_KEY);
	}

	private Integer recoveryDelayMs() {
		return intValue(RECOVERY_DELAY_MS_KEY);
	}

}
