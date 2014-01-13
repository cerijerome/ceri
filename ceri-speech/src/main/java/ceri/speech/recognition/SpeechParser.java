package ceri.speech.recognition;

import java.applet.AudioClip;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.speech.recognition.DictationGrammar;
import javax.speech.recognition.FinalDictationResult;
import javax.speech.recognition.FinalResult;
import javax.speech.recognition.FinalRuleResult;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.ResultToken;
import javax.speech.recognition.RuleGrammar;
import ceri.common.event.EventListener;
import ceri.common.event.EventListenerSupport;
import ceri.common.reflect.ReflectUtil;
import ceri.speech.grammar.ActionGrammar;
import ceri.speech.grammar.DictationParser;

public class SpeechParser {
	public static enum ParseState {
		SUCCESS,
		FAILURE;
	}

	private final SpeechRecognizer recognizer;
	private final Map<String, ActionGrammar> grammarMap = new LinkedHashMap<>();
	private final EventListenerSupport<ParseState> parseListenerSupport;
	private final EventListenerSupport<AudioClip> audioListenerSupport;
	private DictationParser dictationParser = null;

	@SafeVarargs
	public SpeechParser(SpeechRecognizer recognizer, EventListener<AudioClip> audioListener,
		EventListener<ParseState>... parseListeners) {
		this.recognizer = recognizer;
		audioListenerSupport = createAudioListenerSupport(audioListener);
		parseListenerSupport = createParseListenerSupport(Arrays.asList(parseListeners));
		recognizer.addResultListener(new ResultAdapter() {
			@Override
			public void resultAccepted(ResultEvent resultEvent) {
				resultAcceptedEvent((FinalResult) resultEvent.getSource());
			}

			@Override
			public void resultRejected(ResultEvent resultEvent) {
				resultRejectedEvent();
			}
		});
	}

	private EventListenerSupport<ParseState> createParseListenerSupport(
		Collection<EventListener<ParseState>> listeners) {
		EventListenerSupport.Builder<ParseState> builder = EventListenerSupport.builder();
		for (EventListener<ParseState> listener : listeners)
			builder.listener(listener);
		return builder.build();
	}

	private EventListenerSupport<AudioClip> createAudioListenerSupport(
		EventListener<AudioClip> listener) {
		EventListenerSupport.Builder<AudioClip> builder = EventListenerSupport.builder();
		if (listener != null) builder.listener(listener);
		return builder.build();
	}

	public void setDictationMode(boolean on) {
		recognizer.setDictation(on);
	}

	public void setDictationParser(DictationParser dictationParser) {
		this.dictationParser = dictationParser;
	}

	public void removeDictationParser() {
		dictationParser = null;
	}

	public void addGrammar(ActionGrammar grammar) {
		if (grammarMap.containsKey(grammar.getName())) throw new IllegalArgumentException(
			"grammar already exists with name: " + grammar.getName());
		grammarMap.put(grammar.getName(), grammar);
		recognizer.addGrammar(grammar.getJsgf());
	}

	public void clearGrammar() {
		for (ActionGrammar grammar : grammarMap.values())
			removeGrammar(grammar);
	}

	public void removeGrammar(ActionGrammar grammar) {
		recognizer.removeGrammar(grammar.getName());
		grammarMap.remove(grammar.getName());
	}

	public void setGrammarEnabled(ActionGrammar grammar, boolean enabled) {
		recognizer.setGrammarEnabled(grammar.getName(), enabled);
	}

	public void startListening(String profileName) {
		recognizer.startListening(profileName);
	}

	public void close() {
		recognizer.close();
	}

	private boolean parseRule(String grammarName, String rule, List<String> tags) {
		ActionGrammar grammar = grammarMap.get(grammarName);
		if (grammar == null) return false;
		return grammar.parseRule(rule, tags);
	}

	private boolean parseDictation(List<String> words) {
		if (dictationParser == null) return false;
		return dictationParser.parseDictation(words);
	}

	void resultAcceptedEvent(FinalResult result) {
		if (result.getGrammar() instanceof RuleGrammar) ruleResultAcceptedEvent((FinalRuleResult) result);
		else if (result.getGrammar() instanceof DictationGrammar) dictationResultAcceptedEvent((FinalDictationResult) result);
		// Unexpected result type
		else System.out.println("Unexpected result grammar: " + result.getGrammar());
	}

	private void dictationResultAcceptedEvent(FinalDictationResult result) {
		List<String> words = new ArrayList<>();
		for (ResultToken token : result.getBestTokens())
			words.add(token.getSpokenText());
		System.out.println(words);
		audioListenerSupport.event(result.getAudio());
		String context = result.getGrammar().getName();
		System.out.println(context + " <dictation>");
		boolean parsed = parseDictation(words);
		parseListenerSupport.event(parsed ? ParseState.SUCCESS : ParseState.FAILURE);
	}

	private void ruleResultAcceptedEvent(FinalRuleResult result) {
		for (ResultToken token : result.getBestTokens())
			System.out.print(token.getSpokenText() + " ");
		System.out.println();
		audioListenerSupport.event(result.getAudio());
		String grammar = result.getGrammar().getName();
		String rule = result.getRuleName(0);
		rule = rule.substring(1, rule.length() - 1);
		List<String> tags = new ArrayList<>();
		if (result.getTags() != null) Collections.addAll(tags, result.getTags());
		System.out.println(grammar + " " + rule + " " + tags);
		boolean parsed = parseRule(grammar, rule, tags);
		parseListenerSupport.event(parsed ? ParseState.SUCCESS : ParseState.FAILURE);
	}

	void resultRejectedEvent() {
		System.out.println(ReflectUtil.currentMethodName());
		parseListenerSupport.event(ParseState.FAILURE);
	}

}
