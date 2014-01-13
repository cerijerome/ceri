package ceri.speech.grammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ceri.speech.recognition.SpeechParser;

public class ContextGrammar {
	private final Map<String, ActionGrammar> grammarMap;
	private final SpeechParser parser;
	private final Set<String> currentNames = Collections.emptySet();

	public ContextGrammar(SpeechParser parser, ActionGrammar... grammars) {
		this.parser = parser;
		Map<String, ActionGrammar> grammarMap = new HashMap<>();
		for (ActionGrammar grammar : grammars) {
			parser.addGrammar(grammar);
			parser.setGrammarEnabled(grammar, false);
			grammarMap.put(grammar.getName(), grammar);
		}
		this.grammarMap = Collections.unmodifiableMap(grammarMap);
	}

	public void clearContext() {
		setContext();
	}

	public void setContext(String... grammarNames) {
		Set<String> newNames = new HashSet<>();
		Collections.addAll(newNames, grammarNames);
		if (currentNames.equals(newNames)) return;
		for (String newName : newNames)
			if (grammarMap.containsKey(newName)) throw new IllegalArgumentException(
				"No contextual grammar with name " + newName);
		for (Map.Entry<String, ActionGrammar> entry : grammarMap.entrySet()) {
			boolean enable = newNames.contains(entry.getKey());
			parser.setGrammarEnabled(entry.getValue(), enable);
		}
		currentNames.clear();
		currentNames.addAll(newNames);
		System.out.println("Context=>" + currentNames);
	}

}
