package ceri.speech.grammar;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Jsgf {
	public static final String JSGF_V1_HEADER = "#JSGF V1.0;";
	private final Grammar grammar;
	private final List<Import> imports = new ArrayList<>();
	private final List<Rule> rules = new ArrayList<>();

	public Jsgf(String grammarName) {
		grammar = new Grammar(grammarName);
	}

	public String getName() {
		return grammar.getName();
	}

	public Jsgf addImport(String imprt) {
		imports.add(new Import(imprt));
		return this;
	}

	public Jsgf addRule(boolean isPublic, String name, String content) {
		rules.add(new Rule(isPublic, name, content));
		return this;
	}

	public void write(Writer writer) {
		PrintWriter out = new PrintWriter(writer);
		out.println(JSGF_V1_HEADER);
		out.println("// Generated by " + getClass());
		out.println("// " + new Date());
		out.println();
		out.println(grammar);
		out.println();
		for (Import imprt : imports)
			out.println(imprt);
		if (!imports.isEmpty()) out.println();
		for (Rule rule : rules)
			out.println(rule);
		out.println();
		out.flush();
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		write(writer);
		return writer.toString();
	}

	public static class Grammar {
		private static final String GRAMMAR = "grammar";
		private final String grammarName;

		public Grammar(String grammarName) {
			this.grammarName = grammarName;
		}

		public String getName() {
			return grammarName;
		}

		@Override
		public String toString() {
			return GRAMMAR + " " + grammarName + ";";
		}
	}

	public static class Import {
		private static final String IMPORT = "import";
		private final String imprt;

		public Import(String imprt) {
			this.imprt = imprt;
		}

		public String getValue() {
			return imprt;
		}

		@Override
		public String toString() {
			return IMPORT + " <" + imprt + ">;";
		}
	}

	public static class Rule {
		private static final String PUBLIC = "public";
		private final boolean isPublic;
		private final String name;
		private final String content;

		public Rule(boolean isPublic, String name, String content) {
			this.isPublic = isPublic;
			this.name = name;
			this.content = content;
		}

		public boolean isPublic() {
			return isPublic;
		}

		public String getName() {
			return name;
		}

		public String getContent() {
			return content;
		}

		@Override
		public String toString() {
			return (isPublic ? PUBLIC : "") + " <" + name + "> = " + content + ";";
		}
	}

}