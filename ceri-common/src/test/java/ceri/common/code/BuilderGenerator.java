package ceri.common.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Generates code text for a class with a builder. Run main then enter class
 * name followed by fields on subsequent lines. Fields can contain
 * public/protected/private/final/; as these will be removed. Finish with an
 * empty line and the class with builder will be generated. Copy the output into
 * a new Java file. e.g.
 * 
 * <pre>
 * MyClass
 * int id
 * String name
 * Date date
 * </pre>
 */
public class BuilderGenerator {
	private static final Pattern CLEAN_REGEX = Pattern
		.compile("(public |protected |private |final |;)");
	private final String className;
	private final Map<String, String> fields;

	public static void main(String[] args) {
		System.out.println("Enter class name then fields:\n");
		Reader r = new InputStreamReader(System.in);
		generate(r);
	}

	public static class Builder {
		final String className;
		final Map<String, String> fields = new LinkedHashMap<>();

		Builder(String className) {
			this.className = className;
		}

		public Builder field(String type, String name) {
			fields.put(name, type);
			return this;
		}

		public BuilderGenerator build() {
			return new BuilderGenerator(this);
		}
	}

	public static Builder builder(String className) {
		return new Builder(className);
	}

	BuilderGenerator(Builder builder) {
		className = builder.className;
		fields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.fields));
	}

	public void generate(PrintStream out) {
		out.println("public class " + className + " {");
		for (Map.Entry<String, String> entry : fields.entrySet())
			out.println("\tprivate final " + entry.getValue() + " " + entry.getKey() + ";");
		out.println();
		generateBuilder(out);
		out.println();
		out.println("\tpublic static Builder builder() {");
		out.println("\t\treturn new Builder();");
		out.println("\t}");
		out.println();
		out.println("\t" + className + "(Builder builder) {");
		for (String name : fields.keySet())
			out.println("\t\t" + name + " = builder." + name + ";");
		out.println("\t}");
		out.println();
		generateToString(out);
		out.println();
		out.println("}");
	}

	private void generateToString(PrintStream out) {
		out.println("\t@Override");
		out.println("\tpublic String toString() {");
		out.print("\t\treturn ToStringHelper.createByClass(this");
		for (String name : fields.keySet())
			out.print(", " + name);
		out.println(").toString();");
		out.println("\t}");
	}

	private void generateBuilder(PrintStream out) {
		out.println("\tpublic static class Builder {");
		for (Map.Entry<String, String> entry : fields.entrySet())
			out.println("\t\t" + entry.getValue() + " " + entry.getKey() + ";");
		out.println();
		out.println("\t\tBuilder() {");
		out.println("\t\t}");
		out.println();
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			out.println("\t\tpublic Builder " + name + "(" + type + " " + name + ") {");
			out.println("\t\t\tthis." + name + " = " + name + ";");
			out.println("\t\t\treturn this;");
			out.println("\t\t}");
			out.println();
		}
		out.println("\t\tpublic " + className + " build() {");
		out.println("\t\t\treturn new " + className + "(this);");
		out.println("\t\t}");
		out.println("\t}");
	}

	public static void generate(Reader r) {
		BufferedReader in = new BufferedReader(r);
		Builder builder = null;
		String line;
		try {
			while ((line = in.readLine()) != null) {
				line = CLEAN_REGEX.matcher(line).replaceAll("").trim();
				if (builder == null) {
					builder = builder(line);
					continue;
				}
				String[] ss = line.split("\\s+");
				if (ss == null || ss.length < 2) break;
				builder.field(ss[0], ss[1]);
			}
		} catch (IOException e) {}
		if (builder != null) builder.build().generate(System.out);
	}

}
