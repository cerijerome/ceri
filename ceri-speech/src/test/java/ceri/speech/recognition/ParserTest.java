package ceri.speech.recognition;

import java.io.FileReader;
import java.io.Reader;
import javax.speech.Central;
import javax.speech.Engine;
import javax.speech.recognition.FinalRuleResult;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.ResultToken;
import javax.speech.recognition.RuleGrammar;

public class ParserTest {
	public ParserTest() {}

	public static void main(String[] args) {
		try {
			Recognizer rec = Central.createRecognizer(null);
			rec.allocate();
			rec.waitEngineState(Engine.ALLOCATED);

			RuleGrammar gram = rec.newRuleGrammar("test");
			gram.setRule("number", gram.ruleForJSGF("a {1} | one {1} | two {2} | three {3}"), true);
			gram.setRule(
				"item",
				gram.ruleForJSGF("(snicker | snickers) {CANDY BAR} | (bananas | banana) {FRUIT} | toothbrush {DENTAL}"),
				true);
			gram.setRule("list_item", gram.ruleForJSGF("<number> <item> "), true);
			gram.setRule("list", gram.ruleForJSGF("<list_item> (and <list_item>)*"), true);
			gram.setEnabled(true);
			String[] rules = gram.listRuleNames();
			for (int i = 0; i < rules.length; i++) {
				System.out.println("rule " + rules[i] + " = " + gram.getRule(rules[i]));
			}

			String grammarFile =
				"C:/Documents and Settings/cjerome/My Documents/"
					+ "workspace/home/test/java/ceri/home/speech/recognition/grammar.jsgf";
			try (Reader in = new FileReader(grammarFile)) {
				rec.loadJSGF(in);
			}

			rec.commitChanges();
			rec.waitEngineState(Recognizer.LISTENING);

			rec.addResultListener(new ResultAdapter() {
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

			Thread.sleep(60000);
			rec.deallocate();
			rec.waitEngineState(Engine.DEALLOCATED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void resultAcceptedEvent(ResultEvent resultEvent) {
		FinalRuleResult result = (FinalRuleResult) resultEvent.getSource();
		for (ResultToken token : result.getBestTokens()) {
			System.out.println(token.getSpokenText());
		}
	}

	private static void resultRejectedEvent(ResultEvent resultEvent) {
		System.out.println("Fail!!! " + resultEvent);
	}

}
