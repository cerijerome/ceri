package ceri.ci.alert;

import java.util.concurrent.TimeUnit;
import ceri.common.property.BaseProperties;

/**
 * Properties to configure the alert service.
 */
public class AlertProperties extends BaseProperties {
	private static final long REMINDER_MS_DEF = TimeUnit.MINUTES.toMillis(15);
	private static final long SHUTDOWN_TIMEOUT_MS_DEF = TimeUnit.SECONDS.toMillis(3);
	private static final String REMINDER_MS_KEY = "reminder.ms";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public AlertProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public long reminderMs() {
		return longValue(REMINDER_MS_DEF, REMINDER_MS_KEY);
	}

	public long shutdownTimeoutMs() {
		return longValue(SHUTDOWN_TIMEOUT_MS_DEF, SHUTDOWN_TIMEOUT_MS_KEY);
	}

}
