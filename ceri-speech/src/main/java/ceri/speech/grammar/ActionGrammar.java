package ceri.speech.grammar;

import java.util.List;

public abstract class ActionGrammar {
	private final String name;
	private final String jsgf;

	public ActionGrammar(String grammarName, String jsgf) {
		this.name = grammarName;
		this.jsgf = new Jsgf(grammarName).toString() + jsgf.toString();
	}

	public ActionGrammar(Class<?> cls) {
		this(cls.getName(), GrammarUtil.loadJsgfResource(cls));
	}

	public ActionGrammar(Jsgf jsgf) {
		this.name = jsgf.getName();
		this.jsgf = jsgf.toString();
	}

	public String getName() {
		return name;
	}

	public String getJsgf() {
		return jsgf;
	}

	public abstract boolean parseRule(String rule, List<String> tags);

}
