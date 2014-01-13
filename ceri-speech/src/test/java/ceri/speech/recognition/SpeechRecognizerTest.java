package ceri.speech.recognition;

import java.util.Arrays;
import java.util.Properties;
import javax.speech.recognition.FinalRuleResult;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.ResultToken;
import org.junit.Test;
import ceri.common.property.PropertyUtil;
import ceri.speech.grammar.NumberGrammar;

public class SpeechRecognizerTest {

	@Test
	public void testSpeechRecognizer() throws Exception {
		SpeechRecognizer recognizer = create();
		recognizer.addResultListener(new ResultAdapter() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void resultAccepted(ResultEvent resultEvent) {
				resultAcceptedEvent(resultEvent);
			}

			@Override
			@SuppressWarnings("synthetic-access")
			public void resultRejected(ResultEvent resultEvent) {
				resultRejectedEvent(resultEvent);
			}
		});
		Thread.sleep(600000);
		recognizer.close();
	}

	private void resultAcceptedEvent(ResultEvent resultEvent) {
		FinalRuleResult result = (FinalRuleResult) resultEvent.getSource();
		for (ResultToken token : result.getBestTokens())
			System.out.print(token.getSpokenText() + " ");
		System.out.println();
		String[] tags = result.getTags();
		String ruleName = result.getRuleName(0);
		long value = NumberGrammar.getLongFromTags(Arrays.asList(tags));
		System.out.println(ruleName + " " + Arrays.toString(tags) + " " + value);
	}

	private void resultRejectedEvent(ResultEvent resultEvent) {
		System.out.println("Fail!!! " + resultEvent);
	}

	private SpeechRecognizer create() throws Exception {
		Properties properties = PropertyUtil.load(getClass(), "test.properties");
		SpeechRecognizerProperties speechProperties =
			new SpeechRecognizerProperties(properties, "speech.recognizer");
		//String grammarFile = "C:/Documents and Settings/cjerome/My Documents/" +
		//	"workspace/home/src/java/ceri/home/speech/recognition/grammar/NumberGrammar.jsgf";
		//String grammarFile = "C:/Documents and Settings/cjerome/My Documents/" +
		//	"workspace/home/src/java/ceri/home/speech/recognition/grammar/Tv.jsgf";
		return new SpeechRecognizer(speechProperties);
	}

}
