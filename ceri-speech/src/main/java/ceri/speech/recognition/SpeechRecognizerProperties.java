package ceri.speech.recognition;

import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.util.PrimitiveUtil;

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
		return PrimitiveUtil.valueOf(value(NUM_RESULT_ALTERNATIVES),
			NUM_RESULT_ALTERNATIVES_DEFAULT);
	}

	public boolean resultAudioProvided() {
		return PrimitiveUtil.valueOf(value(RESULT_AUDIO_PROVIDED), false);
	}

	public Float sensitivity() {
		return PrimitiveUtil.valueOf(value(SENSITIVITY), (Float) null);
	}

	public Float speedVsAccuracy() {
		return PrimitiveUtil.valueOf(value(SPEED_VS_ACCURACY), (Float) null);
	}

	public Float confidenceLevel() {
		return PrimitiveUtil.valueOf(value(CONFIDENCE_LEVEL), (Float) null);
	}

	public Float completeTimeout() {
		return PrimitiveUtil.valueOf(value(COMPLETE_TIMEOUT), (Float) null);
	}

	public Float incompleteTimeout() {
		return PrimitiveUtil.valueOf(value(INCOMPLETE_TIMEOUT), (Float) null);
	}

}
