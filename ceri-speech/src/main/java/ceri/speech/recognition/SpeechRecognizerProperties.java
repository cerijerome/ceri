package ceri.speech.recognition;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class SpeechRecognizerProperties extends BaseProperties {
	private static final String ENGINE_NAME = "engineName";
	private static final String RESULT_AUDIO_PROVIDED = "resultAudioProvided";
	private static final String NUM_RESULT_ALTERNATIVES = "numResultAlternatives";
	private static final int NUM_RESULT_ALTERNATIVES_DEFAULT = 5;
	private static final String SENSITIVITY = "sensitivity";
	private static final String SPEED_VS_ACCURACY = "speedVsAccuracy";
	private static final String CONFIDENCE_LEVEL = "confidenceLevel";
	private static final String COMPLETE_TIMEOUT = "completeTimeout";
	private static final String INCOMPLETE_TIMEOUT = "incompleteTimeout";

	public SpeechRecognizerProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public String engineName() {
		return value(ENGINE_NAME);
	}

	public int numResultAlternatives() {
		return intValue(NUM_RESULT_ALTERNATIVES_DEFAULT, NUM_RESULT_ALTERNATIVES);
	}

	public Boolean resultAudioProvided() {
		return booleanValue(RESULT_AUDIO_PROVIDED);
	}

	public Float sensitivity() {
		return floatValue(SENSITIVITY);
	}

	public Float speedVsAccuracy() {
		return floatValue(SPEED_VS_ACCURACY);
	}

	public Float confidenceLevel() {
		return floatValue(CONFIDENCE_LEVEL);
	}

	public Float completeTimeout() {
		return floatValue(COMPLETE_TIMEOUT);
	}

	public Float incompleteTimeout() {
		return floatValue(INCOMPLETE_TIMEOUT);
	}

}
