package ceri.ci.audio;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class AudioAlerterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String PITCH_KEY = "pitch";

	public AudioAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public float pitch() {
		return floatValue(Audio.NORMAL_PITCH, PITCH_KEY);
	}

}
