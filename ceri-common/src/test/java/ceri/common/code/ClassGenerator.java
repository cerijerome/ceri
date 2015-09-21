package ceri.common.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Generates code text for an immutable class optionally with a builder. Class contains methods for
 * hashCode(), equals(), and toString().
 * 
 * To generate a non-builder class, run main then enter class name followed by fields on subsequent
 * lines. Fields can contain public/protected/private/final/; as these will be removed. Finish with
 * an empty line and the class will be generated then copied to the clipboard. Example:
 * 
 * <pre>
 * MyClass
 * int id
 * String name
 * Date date
 * </pre>
 */
public class ClassGenerator {
	private static final Pattern CLEAN_REGEX = Pattern
		.compile("(public |protected |private |final |;)");
	private static final Pattern PRIMITIVE_REGEX = Pattern
		.compile("^(boolean|byte|short|int|long|float)$");
	private static final Pattern FIELD_REGEX = Pattern.compile("^(.*)\\s+([\\w]+)$");
	private final String className;
	private final Map<String, String> fields;
	private final boolean hasBuilder;

	public static void main(String[] args) {
		createToClipBoardFromSystemIn(false);
	}

	public static void createToClipBoardFromSystemIn(boolean hasBuilder) {
		System.out.println("Enter class name then fields:\n");
		Reader r = new InputStreamReader(System.in);
		Builder builder = createBuilderFrom(r);
		if (hasBuilder) builder.hasBuilder();
		String s = builder.build().generate();
		System.out.print("Copied to clipboard");
		BasicUtil.copyToClipBoard(s);
	}

	private static Builder createBuilderFrom(Reader r) {
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
				Matcher m = FIELD_REGEX.matcher(line);
				if (!m.find()) break;
				builder.field(m.group(1), m.group(2));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return builder;
	}

	private static class AssignmentPrefix {
		public static final AssignmentPrefix CONSTRUCTOR = new AssignmentPrefix("this.", "");
		public static final AssignmentPrefix CONSTRUCTOR_WITH_BUILDER = new AssignmentPrefix("",
			"builder.");
		public final String field;
		public final String argument;

		private AssignmentPrefix(String field, String argument) {
			this.field = field;
			this.argument = argument;
		}
	}

	public static class Builder {
		final String className;
		final Map<String, String> fields = new LinkedHashMap<>();
		boolean hasBuilder;

		Builder(String className) {
			this.className = className;
		}

		public Builder field(String type, String name) {
			fields.put(name, type);
			return this;
		}

		public Builder hasBuilder() {
			hasBuilder = true;
			return this;
		}

		public ClassGenerator build() {
			return new ClassGenerator(this);
		}
	}

	public static Builder builder(String className) {
		return new Builder(className);
	}

	ClassGenerator(Builder builder) {
		className = builder.className;
		fields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.fields));
		hasBuilder = builder.hasBuilder;
	}

	public String generate() {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			out.println("public class " + className + " {");
			for (Map.Entry<String, String> entry : fields.entrySet())
				generateFieldDeclaration(out, entry.getKey(), entry.getValue());
			out.println();
			if (hasBuilder) {
				generateBuilder(out);
				out.println();
				generateBuilderMethod(out);
				out.println();
				generateConstructorWithBuilder(out);
			} else generateConstructor(out);
			out.println();
			generateHashCode(out);
			out.println();
			generateEquals(out);
			out.println();
			generateToString(out);
			out.println();
			out.println("}");
		}
		return b.toString();
	}

	private void generateFieldDeclaration(PrintStream out, String name, String type) {
		out.printf("\tprivate final %s %s;%n", type, name);
	}

	private void generateBuilderMethod(PrintStream out) {
		out.println("\tpublic static Builder builder() {");
		out.println("\t\treturn new Builder();");
		out.println("\t}");
	}

	private void generateConstructor(PrintStream out) {
		List<String> arguments = toArguments(fields);
		out.print(StringUtil.toString("\tpublic " + className + "(", ") {\n", ", ", arguments));
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateConstructorAssignment(out, AssignmentPrefix.CONSTRUCTOR, entry.getKey(), entry
				.getValue());
		out.println("\t}");
	}

	private List<String> toArguments(Map<String, String> fields) {
		List<String> arguments = new ArrayList<>();
		for (Map.Entry<String, String> entry : fields.entrySet())
			arguments.add(entry.getValue() + " " + entry.getKey());
		return arguments;
	}

	private void generateConstructorWithBuilder(PrintStream out) {
		out.println("\t" + className + "(Builder builder) {");
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateConstructorAssignment(out, AssignmentPrefix.CONSTRUCTOR_WITH_BUILDER, entry
				.getKey(), entry.getValue());
		out.println("\t}");
	}

	private void generateConstructorAssignment(PrintStream out, AssignmentPrefix prefix,
		String name, String type) {
		if (generateConstructorCollectionAssignment(out, prefix, name, type)) return;
		if (generateConstructorMapAssignment(out, prefix, name, type)) return;
		if (generateConstructorDateAssignment(out, prefix, name, type)) return;
		out.printf(prefixFormat("\t\t%s%s = %s%s;%n", prefix, name));
	}

	private boolean generateConstructorCollectionAssignment(PrintStream out,
		AssignmentPrefix prefix, String name, String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		if ("List".equals(collectionType.type)) out.printf(prefixFormat(
			"\t\t%s%s = ImmutableUtil.copyAsList(%s%s);%n", prefix, name));
		else out.printf(prefixFormat("\t\t%s%s = ImmutableUtil.copyAsSet(%s%s);%n", prefix, name));
		return true;
	}

	private boolean generateConstructorMapAssignment(PrintStream out, AssignmentPrefix prefix,
		String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		out.printf(prefixFormat("\t\t%s%s = ImmutableUtil.copyAsMap(%s%s);%n", prefix, name));
		return true;
	}

	private boolean generateConstructorDateAssignment(PrintStream out, AssignmentPrefix prefix,
		String name, String type) {
		if (!"Date".equals(type)) return false;
		out.printf(prefixFormat("\t\t%s%s = ImmutableDate.create(%s%s);%n", prefix, name));
		return true;
	}

	private String prefixFormat(String format, AssignmentPrefix prefix, String name) {
		return String.format(format, prefix.field, name, prefix.argument, name);
	}

	private void generateEquals(PrintStream out) {
		out.println("\t@Override");
		out.println("\tpublic boolean equals(Object obj) {");
		out.println("\t\tif (this == obj) return true;");
		out.printf("\t\tif (!(obj instanceof %s)) return false;%n", className);
		out.printf("\t\t%s other = (%s) obj;%n", className, className);
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			if (PRIMITIVE_REGEX.matcher(type).find()) out.printf(
				"\t\tif (%s != other.%s) return false;%n", name, name);
			else out
				.printf("\t\tif (!EqualsUtil.equals(%s, other.%s)) return false;%n", name, name);
		}
		out.println("\t\treturn true;");
		out.println("\t}");
	}

	private void generateHashCode(PrintStream out) {
		out.println("\t@Override");
		out.println("\tpublic int hashCode() {");
		out.println(StringUtil.toString("\t\treturn HashCoder.hash(", ");", ", ", fields.keySet()));
		out.println("\t}");
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
			generateBuilderFieldDeclaration(out, entry.getKey(), entry.getValue());
		out.println();
		generateBuilderConstructor(out);
		out.println();
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			generateBuilderSetter(out, entry.getKey(), entry.getValue());
			out.println();
		}
		generateBuilderBuild(out);
		out.println("\t}");
	}

	private void generateBuilderFieldDeclaration(PrintStream out, String name, String type) {
		if (generateBuilderCollectionFieldDeclaration(out, name, type)) return;
		if (generateBuilderMapFieldDeclaration(out, name, type)) return;
		out.printf("\t\t%s %s;%n", type, name);
	}

	private boolean generateBuilderCollectionFieldDeclaration(PrintStream out, String name,
		String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		out.printf("\t\tfinal Collection<%s> %s = new LinkedHashSet<>();%n",
			collectionType.itemType, name);
		return true;
	}

	private boolean generateBuilderMapFieldDeclaration(PrintStream out, String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		out.printf("\t\tfinal Map<%s, %s> %s = new HashMap<>();%n", mapType.keyType,
			mapType.valueType, name);
		return true;
	}

	private void generateBuilderConstructor(PrintStream out) {
		out.println("\t\tBuilder() {}");
	}

	private void generateBuilderSetter(PrintStream out, String name, String type) {
		if (generateBuilderCollectionSetters(out, name, type)) return;
		if (generateBuilderMapSetters(out, name, type)) return;
		generateBuilderStandardSetter(out, name, type);
	}

	private void generateBuilderStandardSetter(PrintStream out, String name, String type) {
		out.printf("\t\tpublic Builder %s(%s %s) {%n", name, type, name);
		out.printf("\t\t\tthis.%s = %s;%n", name, name);
		out.println("\t\t\treturn this;");
		out.println("\t\t}");
	}

	private boolean generateBuilderCollectionSetters(PrintStream out, String name, String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		out.printf("\t\tpublic Builder %s(%s... %s) {%n", name, collectionType.itemType, name);
		out.printf("\t\t\treturn %s(Arrays.asList(%s));%n", name, name);
		out.println("\t\t}");
		out.println();
		out.printf("\t\tpublic Builder %s(Collection<%s> %s) {%n", name, collectionType.itemType,
			name);
		out.printf("\t\t\tthis.%s.addAll(%s);%n", name, name);
		out.println("\t\t\treturn this;");
		out.println("\t\t}");
		return true;
	}

	private boolean generateBuilderMapSetters(PrintStream out, String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		out.printf("\t\tpublic Builder %s(%s key, %s value) {%n", name, mapType.keyType,
			mapType.valueType);
		out.printf("\t\t\t%s.put(key, value);%n", name);
		out.println("\t\t\treturn this;");
		out.println("\t\t}");
		out.println();
		out.printf("\t\tpublic Builder %s(Map<%s, %s> %s) {%n", name, mapType.keyType,
			mapType.valueType, name);
		out.printf("\t\t\tthis.%s.putAll(%s);%n", name, name);
		out.println("\t\t\treturn this;");
		out.println("\t\t}");
		return true;
	}

	private void generateBuilderBuild(PrintStream out) {
		out.printf("\t\tpublic %s build() {%n", className);
		out.printf("\t\t\treturn new %s(this);%n", className);
		out.println("\t\t}");
	}

}
