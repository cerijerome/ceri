package ceri.ci.alert;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import ceri.common.property.BaseProperties;

public class AlertServiceProperties extends BaseProperties {
	private static final long REMINDER_MS_DEF = TimeUnit.MINUTES.toMillis(15);
	private static final long TIMEOUT_MS_DEF = 5000;
	private static final int PORT_DEF = 80;
	private static final String REMINDER_MS_KEY = "reminder.ms";
	private static final String TIMEOUT_MS_KEY = "timeout.ms";
	private static final String PORT_KEY = "port";

	public AlertServiceProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public long reminderMs() {
		return longValue(REMINDER_MS_DEF, REMINDER_MS_KEY);
	}

	public long timeoutMs() {
		return longValue(TIMEOUT_MS_DEF, TIMEOUT_MS_KEY);
	}

	public int port() {
		return intValue(PORT_DEF, PORT_KEY);
	}

}
