package ceri.common.code;

import static ceri.common.code.Prefix.FROM_BUILDER;
import static ceri.common.code.Prefix.THIS;
import static ceri.common.collection.CollectionUtil.last;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import ceri.common.collection.ImmutableUtil;
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
 * an empty line and the class will be generated then copied to the clipboard. A class stub
 * containing fields without blank lines can also be entered.
 * 
 * Example:
 * 
 * <pre>
 * public MyClass{@literal<}T, S extends Enum{@literal<}S{@literal>}{@literal>} {
 * final int id;
 * S s
 * private List<String> names
 * Date date
 * }
 * </pre>
 */
public class ClassGenerator {
	private static final Pattern CLASS_REGEX =
		Pattern.compile("^(?:public |class )*([\\w]+)((?:<.*>)?)[\\s{]*$");
	private static final Pattern CLEAN_REGEX =
		Pattern.compile("(import .*|public |protected |private |final |=.*|;)");
	private static final Pattern FIELD_REGEX = Pattern.compile("^(.*)\\s+([\\w]+)$");
	private final String className;
	private final Generics generics;
	private final List<Field> fields;
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
		try {
			return builderFrom(r);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Builder builderFrom(Reader r) throws IOException {
		BufferedReader in = new BufferedReader(r);
		Builder builder = null;
		String line;
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
		return builder;
	}

	public static class Builder {
		final String className;
		final String classGenerics;
		final Set<Field> fields = new LinkedHashSet<>();
		boolean hasBuilder;

		Builder(String className, String classGenerics) {
			this.className = className;
			this.classGenerics = classGenerics;
		}

		public Builder field(String type, String name) {
			fields.add(Field.of(name, type));
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
		generics = Generics.of(builder.classGenerics);
		fields = ImmutableUtil.copyAsList(builder.fields);
		hasBuilder = builder.hasBuilder;
	}

	public Context generate() {
		Context con = new Context();
		con.printf("public class %s%s {%n", className, generics.cls);
		fields.forEach(field -> con.printf("\tprivate final %s;%n", field));
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
			generateConstructorWithOf(con);
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

	private void generateOf(Context con) {
		Field last = last(fields);
		if (last.collection != null) {
			generateVarArgOf(con, last.name);
			con.println();
		}
		con.printf("\tpublic static %s%s%s of(%s) {%n", generics.stat, className, generics.simple,
			join(field -> field.asLooseArg(con, null)));
		con.printf("\t\treturn new %s%s(%s);%n", className, generics.empty,
			join(field -> field.asCopy(con, "")));
		con.println("\t}");
	}

	private void generateVarArgOf(Context con, String varArg) {
		con.printf("\tpublic static %s%s%s of(%s) {%n", generics.stat, className, generics.simple,
			join(field -> field.asLooseArg(con, varArg)));
		con.printf("\t\treturn of(%s);%n", join(field -> field.fromVarArgParam(con, varArg)));
		con.println("\t}");
	}

	private void generateConstructorWithOf(Context con) {
		con.printf("\tprivate %s(%s) {%n", className, join(Field::toString));
		fields.forEach(field -> con.printf("\t\t%s;%n", field.asAssignment(THIS)));
		con.println("\t}");
	}

	private void generateBuilderMethod(Context con) {
		con.printf("\tpublic static %sBuilder%s builder() {%n", generics.stat, generics.simple);
		con.printf("\t\treturn new Builder%s();%n", generics.empty);
		con.println("\t}");
	}

	private void generateBuilder(Context con) {
		con.printf("\tpublic static class Builder%s {%n", generics.cls);
		fields.forEach(field -> con.printf("\t\t%s;%n", field.asBuilderField(con)));
		con.println();
		con.println("\t\tBuilder() {}");
		con.println();
		fields.forEach(field -> {
			generateBuilderSetter(con, field);
			con.println();
		});
		generateBuilderBuild(con);
		con.println("\t}");
	}

	private void generateBuilderSetter(Context con, Field field) {
		if (field.collection != null) generateBuilderCollectionSetters(con, field);
		else if (field.map != null) generateBuilderMapSetters(con, field);
		else generateBuilderStandardSetter(con, field);
	}

	private void generateBuilderStandardSetter(Context con, Field field) {
		con.printf("\t\tpublic Builder%s %s(%s) {%n", generics.simple, field.name, field);
		con.printf("\t\t\tthis.%s = %1$s;%n", field.name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
	}

	private void generateBuilderCollectionSetters(Context con, Field field) {
		con.printf("\t\tpublic Builder%s %s(%s) {%n", generics.simple, field.name,
			field.asVarArg());
		con.imports(Arrays.class);
		con.printf("\t\t\treturn %s(Arrays.asList(%1$s));%n", field.name);
		con.println("\t\t}");
		con.println();
		con.printf("\t\tpublic Builder%s %s(%s) {%n", generics.simple, field.name,
			field.asCollection(con));
		con.printf("\t\t\tthis.%s.addAll(%1$s);%n", field.name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
	}

	private void generateBuilderMapSetters(Context con, Field field) {
		con.printf("\t\tpublic Builder%s %s(%s key, %s value) {%n", generics.simple,
			field.nonPluralName(), field.map.keyType, field.map.valueType);
		con.printf("\t\t\t%s.put(key, value);%n", field.name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
		con.println();
		con.printf("\t\tpublic Builder%s %s(%s) {%n", generics.simple, field.name,
			field.asMap(con));
		con.printf("\t\t\tthis.%s.putAll(%1$s);%n", field.name);
		con.println("\t\t\treturn this;");
		con.println("\t\t}");
	}

	private void generateBuilderBuild(Context con) {
		con.printf("\t\tpublic %s%s build() {%n", className, generics.simple);
		con.printf("\t\t\treturn new %s%s(this);%n", className, generics.empty);
		con.println("\t\t}");
	}

	private void generateConstructorWithBuilder(Context con) {
		con.printf("\t%s(Builder%s builder) {%n", className, generics.simple);
		fields.forEach(field -> con.printf("\t\t%s;%n", field.asCopyAssignment(con, FROM_BUILDER)));
		con.println("\t}");
	}

	private void generateEquals(Context con) {
		con.println("\t@Override");
		con.println("\tpublic boolean equals(Object obj) {");
		con.println("\t\tif (this == obj) return true;");
		con.printf("\t\tif (!(obj instanceof %s)) return false;%n", className);
		con.printf("\t\t%s%s other = (%1$s%2$s) obj;%n", className, generics.wildcard);
		fields.forEach(field -> generateEqualsLine(con, field));
		con.println("\t\treturn true;");
		con.println("\t}");
	}

	private void generateEqualsLine(Context con, Field field) {
		if (field.primitiveEquals)
			con.printf("\t\tif (%s != other.%1$s) return false;%n", field.name);
		else {
			con.printf("\t\tif (!EqualsUtil.equals(%s, other.%1$s)) return false;%n", field.name);
			con.imports(EqualsUtil.class);
		}
	}

	private void generateHashCode(Context con) {
		con.println("\t@Override");
		con.println("\tpublic int hashCode() {");
		con.printf("\t\treturn HashCoder.hash(%s);%n", join(Field::name));
		con.println("\t}");
		con.imports(HashCoder.class);
	}

	private void generateToString(Context con) {
		con.println("\t@Override");
		con.println("\tpublic String toString() {");
		con.print("\t\treturn ToStringHelper.createByClass(this");
		fields.forEach(field -> con.print(", " + field.name));
		con.println(").toString();");
		con.println("\t}");
		con.imports(ToStringHelper.class);
	}

	private String join(Function<Field, String> fieldFn) {
		return join(fieldFn, fields);
	}

	private String join(Function<Field, String> fieldFn, List<Field> fields) {
		return fields.stream().map(fieldFn).collect(Collectors.joining(", "));
	}

}
