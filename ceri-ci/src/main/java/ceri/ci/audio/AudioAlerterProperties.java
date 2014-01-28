package ceri.ci.audio;

import java.util.Properties;
import ceri.common.date.TimeUnit;
import ceri.common.property.BaseProperties;

public class AudioAlerterProperties extends BaseProperties {
	static final long REMINDER_MS_DEF = TimeUnit.minute.ms * 10;
	static final long SHUTDOWN_MS_DEF = TimeUnit.second.ms;
	static final float PITCH_DEF = 1.0f;
	private static final String REMINDER_MS_KEY = "reminder.ms";
	private static final String SHUTDOWN_MS_KEY = "shutdown.ms";
	private static final String PITCH_KEY = "pitch";

	public AudioAlerterProperties(Properties properties) {
		this(properties, null);
	}

	public AudioAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public long reminderMs() {
		return longValue(REMINDER_MS_DEF, REMINDER_MS_KEY);
	}

	public long shutdownreminderMs() {
		return longValue(SHUTDOWN_MS_DEF, SHUTDOWN_MS_KEY);
	}

	public float pitch() {
		return floatValue(PITCH_DEF, PITCH_KEY);
	}

}
