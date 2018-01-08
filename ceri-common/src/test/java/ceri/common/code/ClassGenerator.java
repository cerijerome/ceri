package ceri.common.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.FunctionUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Generates code text for an immutable class optionally with a builder. Class contains methods for
 * hashCode(), equals(), and toString().
 * 
 * To generate a non-builder class, run main then enter class name followed by fields on subsequent
 * lines. Fields can contain public/protected/private/final/; as these will be removed. Finish with
 * an empty line and the class will be generated then copied to the clipboard. Example:
 * 
 * <pre>
 * public MyClass<T, S extends Enum<S>>
 * final int id;
 * S s
 * private List<String> names
 * Date date
 * </pre>
 */
public class ClassGenerator {
	private static final Pattern CLASS_REGEX =
		Pattern.compile("^(?:public |class )*([\\w]+)((?:<.*>)?)[\\s{]*$");
	private static final Pattern CLEAN_GENERICS_REGEX =
		Pattern.compile("(<[^<>]+>|&|extends|super|(?<=\\w+)\\s+\\w+)");
	private static final Pattern CLEAN_REGEX =
		Pattern.compile("(import .*|public |protected |private |final |=.*|;)");
	private static final Pattern PRIMITIVE_EQUALS_REGEX =
		Pattern.compile("^(boolean|char|byte|short|int|long)$");
	private static final Pattern FIELD_REGEX = Pattern.compile("^(.*)\\s+([\\w]+)$");
	private final String className;
	private final String classGenerics;
	private final String simpleGenerics;
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
		String s = builder.build().generate().toString();
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
					if (line.isEmpty()) continue;
					Matcher m = CLASS_REGEX.matcher(line);
					if (!m.find()) break;
					builder = builder(m.group(1), m.group(2));
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
		public static final AssignmentPrefix CONSTRUCTOR_WITH_BUILDER =
			new AssignmentPrefix("", "builder.");
		public final String field;
		public final String argument;

		private AssignmentPrefix(String field, String argument) {
			this.field = field;
			this.argument = argument;
		}
	}

	public static class Builder {
		final String className;
		final String classGenerics;
		final Map<String, String> fields = new LinkedHashMap<>();
		boolean hasBuilder;

		Builder(String className, String classGenerics) {
			this.className = className;
			this.classGenerics = classGenerics;
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

	public static Builder builder(String className, String classGenerics) {
		return new Builder(className, classGenerics);
	}

	ClassGenerator(Builder builder) {
		className = builder.className;
		classGenerics = builder.classGenerics;
		simpleGenerics = simpleGenerics(classGenerics);
		fields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.fields));
		hasBuilder = builder.hasBuilder;
	}

	public Context generate() {
		Context con = new Context();
		con.printf("public class %s%s {%n", className, classGenerics);
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateFieldDeclaration(con, entry.getKey(), entry.getValue());
		con.println();
		if (hasBuilder) {
			generateBuilder(con);
			con.println();
			generateBuilderMethod(con);
			con.println();
			generateConstructorWithBuilder(con);
		} else {
			generateOf(con);
			con.println();
			generateConstructor(con);
		}
		con.println();
		generateHashCode(con);
		con.println();
		generateEquals(con);
		con.println();
		generateToString(con);
		con.println();
		con.println("}");
		return con;
	}

	private void generateFieldDeclaration(Context con, String name, String type) {
		con.printf("\tprivate final %s %s;%n", type, name);
	}

	private void generateBuilderMethod(Context con) {
		con.printf("\tpublic static %sBuilder%s builder() {%n", staticGenerics(), simpleGenerics);
		con.printf("\t\treturn new Builder%s();%n", emptyGenerics());
		con.println("\t}");
	}

	private void generateOf(Context con) {
		con.printf("\tpublic static %s%s of(%s) {%n", staticGenerics(), className,
			String.join(", ", toArguments(fields)));
		con.printf("\t\treturn new %s%s(%s);%n", className, emptyGenerics(),
			String.join(", ", fields.keySet()));
		con.println("\t}");
	}

	private void generateConstructor(Context con) {
		con.printf("\tprivate %s(%s) {%n", className, String.join(", ", toArguments(fields)));
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateConstructorAssignment(con, AssignmentPrefix.CONSTRUCTOR, entry.getKey(),
				entry.getValue());
		con.println("\t}");
	}

	private List<String> toArguments(Map<String, String> fields) {
		List<String> arguments = new ArrayList<>();
		for (Map.Entry<String, String> entry : fields.entrySet())
			arguments.add(entry.getValue() + " " + entry.getKey());
		return arguments;
	}

	private void generateConstructorWithBuilder(Context con) {
		con.printf("\t%s(Builder%s builder) {%n", className, simpleGenerics);
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateConstructorAssignment(con, AssignmentPrefix.CONSTRUCTOR_WITH_BUILDER,
				entry.getKey(), entry.getValue());
		con.println("\t}");
	}

	private void generateConstructorAssignment(Context con, AssignmentPrefix prefix, String name,
		String type) {
		if (generateConstructorCollectionAssignment(con, prefix, name, type)) return;
		if (generateConstructorMapAssignment(con, prefix, name, type)) return;
		con.printf(prefixFormat("\t\t%s%s = %s%s;%n", prefix, name));
	}

	private boolean generateConstructorCollectionAssignment(Context con, AssignmentPrefix prefix,
		String name, String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		if ("List".equals(collectionType.type))
			con.printf(prefixFormat("\t\t%s%s = ImmutableUtil.copyAsList(%s%s);%n", prefix, name));
		else con.printf(prefixFormat("\t\t%s%s = ImmutableUtil.copyAsSet(%s%s);%n", prefix, name));
		con.imports(ImmutableUtil.class, collectionType.typeClass());
		return true;
	}

	private boolean generateConstructorMapAssignment(Context con, AssignmentPrefix prefix,
		String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		con.printf(prefixFormat("\t\t%s%s = ImmutableUtil.copyAsMap(%s%s);%n", prefix, name));
		con.imports(ImmutableUtil.class, mapType.typeClass());
		return true;
	}

	private String prefixFormat(String format, AssignmentPrefix prefix, String name) {
		return String.format(format, prefix.field, name, prefix.argument, name);
	}

	private void generateEquals(Context con) {
		con.println("\t@Override");
		con.println("\tpublic boolean equals(Object obj) {");
		con.println("\t\tif (this == obj) return true;");
		con.printf("\t\tif (!(obj instanceof %s)) return false;%n", className);
		con.printf("\t\t%s%s other = (%1$s%2$s) obj;%n", className, wildcardGenerics());
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			if (PRIMITIVE_EQUALS_REGEX.matcher(type).find())
				con.printf("\t\tif (%s != other.%s) return false;%n", name, name);
			else {
				con.printf("\t\tif (!EqualsUtil.equals(%s, other.%s)) return false;%n", name, name);
				con.imports(EqualsUtil.class);
			}
		}
		con.println("\t\treturn true;");
		con.println("\t}");
	}

	private void generateHashCode(Context con) {
		con.println("\t@Override");
		con.println("\tpublic int hashCode() {");
		con.printf("\t\treturn HashCoder.hash(%s);%n", String.join(", ", fields.keySet()));
		con.println("\t}");
		con.imports(HashCoder.class);
	}

	private void generateToString(Context con) {
		con.println("\t@Override");
		con.println("\tpublic String toString() {");
		con.print("\t\treturn ToStringHelper.createByClass(this");
		for (String name : fields.keySet())
			con.print(", " + name);
		con.println(").toString();");
		con.println("\t}");
		con.imports(ToStringHelper.class);
	}

	private void generateBuilder(Context con) {
		con.printf("\tpublic static class Builder%s {%n", classGenerics);
		for (Map.Entry<String, String> entry : fields.entrySet())
			generateBuilderFieldDeclaration(con, entry.getKey(), entry.getValue());
		con.println();
		generateBuilderConstructor(con);
		con.println();
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			generateBuilderSetter(con, entry.getKey(), entry.getValue());
			con.println();
		}
		generateBuilderBuild(con);
		con.println("\t}");
	}

	private void generateBuilderFieldDeclaration(Context con, String name, String type) {
		if (generateBuilderCollectionFieldDeclaration(con, name, type)) return;
		if (generateBuilderMapFieldDeclaration(con, name, type)) return;
		con.printf("\t\t%s %s;%n", type, name);
	}

	private boolean generateBuilderCollectionFieldDeclaration(Context con, String name,
		String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		con.printf("\t\tfinal Collection<%s> %s = new LinkedHashSet<>();%n",
			collectionType.itemType, name);
		con.imports(Collection.class, LinkedHashSet.class);
		return true;
	}

	private boolean generateBuilderMapFieldDeclaration(Context con, String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		con.printf("\t\tfinal Map<%s, %s> %s = new LinkedHashMap<>();%n", mapType.keyType,
			mapType.valueType, name);
		con.imports(Map.class, LinkedHashMap.class);
		return true;
	}

	private void generateBuilderConstructor(Context con) {
		con.println("\t\tBuilder() {}");
	}

	private void generateBuilderSetter(Context con, String name, String type) {
		if (generateBuilderCollectionSetters(con, name, type)) return;
		if (generateBuilderMapSetters(con, name, type)) return;
		generateBuilderStandardSetter(con, name, type);
	}

	private void generateBuilderStandardSetter(Context con, String name, String type) {
		con.printf("\t\tpublic Builder%s %s(%s %2$s) {%n", simpleGenerics, name, type);
		con.printf("\t\t\tthis.%s = %s;%n", name, name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
	}

	private boolean generateBuilderCollectionSetters(Context con, String name, String type) {
		CollectionType collectionType = CollectionType.createFrom(type);
		if (collectionType == null) return false;
		con.printf("\t\tpublic Builder%s %s(%s... %2$s) {%n", simpleGenerics, name,
			collectionType.itemType);
		con.printf("\t\t\treturn %s(Arrays.asList(%1$s));%n", name);
		con.println("\t\t}");
		con.println();
		con.printf("\t\tpublic Builder%s %s(Collection<%s> %2$s) {%n", simpleGenerics, name,
			collectionType.itemType);
		con.printf("\t\t\tthis.%s.addAll(%s);%n", name, name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
		con.imports(Collection.class, Arrays.class, collectionType.typeClass());
		return true;
	}

	private boolean generateBuilderMapSetters(Context con, String name, String type) {
		MapType mapType = MapType.createFrom(type);
		if (mapType == null) return false;
		String nonPluralName = name.endsWith("s") ? name.substring(0, name.length() - 1) : name;
		con.printf("\t\tpublic Builder%s %s(%s key, %s value) {%n", simpleGenerics, nonPluralName,
			mapType.keyType, mapType.valueType);
		con.printf("\t\t\t%s.put(key, value);%n", name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
		con.println();
		con.printf("\t\tpublic Builder%s %s(Map<%s, %s> %2$s) {%n", simpleGenerics, name,
			mapType.keyType, mapType.valueType);
		con.printf("\t\t\tthis.%s.putAll(%1$s);%n", name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
		con.imports(Map.class, mapType.typeClass());
		return true;
	}

	private void generateBuilderBuild(Context con) {
		con.printf("\t\tpublic %s%s build() {%n", className, simpleGenerics);
		con.printf("\t\t\treturn new %s%s(this);%n", className, emptyGenerics());
		con.println("\t\t}");
	}

	private String staticGenerics() {
		return classGenerics.isEmpty() ? "" : classGenerics + " ";
	}

	private String emptyGenerics() {
		if (classGenerics.isEmpty()) return "";
		return "<>";
	}

	private String wildcardGenerics() {
		if (classGenerics.isEmpty()) return "";
		return simpleGenerics.replaceAll("\\w+", "?");
	}

	public String simpleGenerics(String generics) {
		if (generics.isEmpty()) return "";
		String str = FunctionUtil.recurse(generics.substring(1, generics.length() - 1),
			s -> CLEAN_GENERICS_REGEX.matcher(s).replaceAll(""));
		return "<" + str + ">";
	}

}
