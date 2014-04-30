package ceri.ci.audio;

import ceri.common.property.BaseProperties;

/**
 * Properties to configure the audio alerter.
 */
public class AudioProperties extends BaseProperties {
	private static final String VOICE_DEF = "en-us/lauren";
	private static final String ENABLED_KEY = "enabled";
	private static final String PITCH_KEY = "pitch";
	private static final String VOICE_KEY = "voice";

	public AudioProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public float pitch() {
		return floatValue(Audio.NORMAL_PITCH, PITCH_KEY);
	}

	public String voice() {
		return stringValue(VOICE_DEF, VOICE_KEY);
	}

}
