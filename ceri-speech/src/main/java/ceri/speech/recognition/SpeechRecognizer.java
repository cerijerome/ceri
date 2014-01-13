package ceri.speech.recognition;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.EngineAdapter;
import javax.speech.EngineErrorEvent;
import javax.speech.EngineException;
import javax.speech.EngineListener;
import javax.speech.recognition.DictationGrammar;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.RecognizerModeDesc;
import javax.speech.recognition.RecognizerProperties;
import javax.speech.recognition.ResultListener;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.SpeakerProfile;
import ceri.common.util.BasicUtil;
import ceri.common.util.RuntimeInterruptedException;

public class SpeechRecognizer {
	private static String DICTATION_CONTEXT = "dictation";
	private final SpeechRecognizerProperties properties;
	private final Recognizer recognizer;
	private final EngineListener engineListener = new EngineAdapter() {
		@Override
		public void engineError(EngineErrorEvent event) {
			engineErrorEvent(event);
		}
	};
	private Throwable engineError = null;

	public SpeechRecognizer(SpeechRecognizerProperties properties) throws InterruptedException {
		this.properties = properties;
		recognizer = createRecognizer();
	}

	public void addResultListener(ResultListener resultListener) {
		recognizer.addResultListener(resultListener);
	}

	public void removeResultListener(ResultListener resultListener) {
		recognizer.removeResultListener(resultListener);
	}

	public void addGrammar(String jsgf) {
		try (Reader in = new StringReader(jsgf)) {
			recognizer.suspend();
			recognizer.loadJSGF(in);
			commitChanges();
		} catch (GrammarException e) {
			throw new IllegalArgumentException("Invalid content", e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private void commitChanges() throws GrammarException {
		for (int i = 0; i < 3; i++) {
			try {
				recognizer.commitChanges();
				return;
			} catch (RuntimeException e) {
				BasicUtil.delay(200);
			}
		}
	}

	public void removeGrammar(String name) {
		try {
			RuleGrammar grammar = recognizer.getRuleGrammar(name);
			recognizer.suspend();
			recognizer.deleteRuleGrammar(grammar);
			commitChanges();
		} catch (GrammarException e) {
			throw new IllegalArgumentException("Unabled to find grammar: " + name, e);
		}
	}

	public void setGrammarEnabled(String name, boolean enabled) {
		try {
			RuleGrammar grammar = recognizer.getRuleGrammar(name);
			recognizer.suspend();
			grammar.setEnabled(enabled);
			commitChanges();
		} catch (GrammarException e) {
			throw new IllegalArgumentException("Unabled to find grammar: " + name, e);
		}
	}

	public void startListening(String profileName) {
		checkEngineError();
		SpeakerProfile profile = new SpeakerProfile(profileName, profileName, profileName);
		recognizer.getSpeakerManager().setCurrentSpeaker(profile);
		recognizer.requestFocus();
		try {
			recognizer.resume();
			System.out.println("Listening...");
		} catch (AudioException e) {
			throw new IllegalStateException("Unable to access audio channel", e);
		}
	}

	public void close() {
		try {
			recognizer.forceFinalize(true);
			recognizer.removeEngineListener(engineListener);
			recognizer.deallocate();
			recognizer.waitEngineState(Engine.DEALLOCATED);
		} catch (EngineException e) {
			throw new IllegalStateException("Error deallocating resources", e);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	public void setDictation(boolean on) {
		setDictation(recognizer, on);
	}

	private void setDictation(Recognizer recognizer, boolean on) {
		try {
			DictationGrammar dictation = recognizer.getDictationGrammar(DICTATION_CONTEXT);
			recognizer.suspend();
			dictation.setEnabled(on);
			commitChanges();
		} catch (GrammarException e) {
			throw new IllegalStateException(e);
		}
	}

	private void checkEngineError() {
		if (engineError != null) throw new IllegalStateException("Recognizer is in a bad state",
			engineError);
	}

	private Recognizer createRecognizer() throws InterruptedException {
		try {
			RecognizerModeDesc desc =
				new RecognizerModeDesc(properties.engineName(), null, null, null, null, null);
			Recognizer recognizer = Central.createRecognizer(desc);
			recognizer.addEngineListener(engineListener);
			recognizer.allocate();
			recognizer.waitEngineState(Engine.ALLOCATED);
			setProperties(recognizer.getRecognizerProperties());
			setDictation(recognizer, false);
			return recognizer;
		} catch (EngineException e) {
			throw new IllegalStateException("Unable to create speech recognizer", e);
		}
	}

	void engineErrorEvent(EngineErrorEvent event) {
		engineError = event.getEngineError();
	}

	private void setProperties(RecognizerProperties props) {
		try {
			props.setResultAudioProvided(properties.resultAudioProvided());
			props.setNumResultAlternatives(properties.numResultAlternatives());
			if (properties.sensitivity() != null) props.setSensitivity(properties.sensitivity());
			if (properties.speedVsAccuracy() != null) props.setSpeedVsAccuracy(properties
				.speedVsAccuracy());
			if (properties.confidenceLevel() != null) props.setConfidenceLevel(properties
				.confidenceLevel());
			if (properties.completeTimeout() != null) props.setCompleteTimeout(properties
				.completeTimeout());
			if (properties.incompleteTimeout() != null) props.setIncompleteTimeout(properties
				.incompleteTimeout());
			if (properties.incompleteTimeout() != null) props.setIncompleteTimeout(properties
				.incompleteTimeout());

		} catch (java.beans.PropertyVetoException e) {
			throw new IllegalArgumentException("Unable to set property", e);
		}
	}

}
