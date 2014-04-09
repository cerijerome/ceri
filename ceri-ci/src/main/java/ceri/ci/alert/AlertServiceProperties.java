package ceri.ci.alert;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import ceri.common.property.BaseProperties;

public class AlertServiceProperties extends BaseProperties {
	private static final long REMINDER_MS_DEF = TimeUnit.MINUTES.toMillis(15);
	private static final String REMINDER_MS_KEY = "reminder.ms";

	public AlertServiceProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public long reminderMs() {
		return longValue(REMINDER_MS_DEF, REMINDER_MS_KEY);
	}

}
