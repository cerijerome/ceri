package ceri.ci.audio;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class AudioAlerterProperties extends BaseProperties {
	private static final String PITCH_KEY = "pitch";

	public AudioAlerterProperties(Properties properties) {
		this(properties, null);
	}

	public AudioAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public float pitch() {
		return floatValue(Audio.NORMAL_PITCH, PITCH_KEY);
	}

}
