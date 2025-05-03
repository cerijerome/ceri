package ceri.jna.clib.test;

import static java.lang.reflect.AccessFlag.FINAL;
import static java.lang.reflect.AccessFlag.PUBLIC;
import static java.lang.reflect.AccessFlag.STATIC;
import java.io.PrintStream;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.sun.jna.IntegerType;
import ceri.common.io.IoUtil;
import ceri.common.math.MathUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.time.DateUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.OsUtil;
import ceri.jna.util.JnaArgs;
import ceri.jna.util.JnaOs;

/**
 * Generates c code to compile and run on a target system, in order to print symbol values and
 * definitions. Useful to determine constant values for JNA code.
 */
public class CSymbolGen {
	private static final String PLACEHOLDER_PRE = "$$CSYMBOLGEN_PRE$$";
	private static final String PLACEHOLDER_MAIN = "$$CSYMBOLGEN_MAIN$$";
	private static final String TEMPLATE_DEF = "symbols-template.c";
	private static final String ENUM_VALUE_FIELD_DEF = "value";
	private static final List<String> INCLUDES = List.of("stdlib", "stdio", "string", "ctype");
	private static final List<String> FIELD_EXCLUDES = List.of("SIZE");
	private static final List<AccessFlag> FIELD_FLAGS = List.of(PUBLIC, STATIC, FINAL);
	private static final List<Class<?>> FIELD_TYPES =
		List.of(Number.class, long.class, int.class, short.class, byte.class, Enum.class);
	private static final List<Class<?>> INT_TYPES = List.of(Long.class, Integer.class, Short.class,
		Byte.class, long.class, int.class, short.class, byte.class, IntegerType.class, Enum.class);
	private final String template;
	public final Section pre;
	public final Section main;
	private final Map<Class<?>, EnumType> enumTypes = new HashMap<>();
	private Predicate<Field> fieldFilter;
	private Predicate<Field> intFilter;

	/**
	 * Declare enum type if 1) enum is also a c enum; and/or 2) value field is not "value". No enum
	 * type means java-only enum, value field is "value".
	 */
	public record EnumType(Class<?> cls, boolean c, String valueField) {
		public static final EnumType NULL = new EnumType(null, false, null);
	}

	/**
	 * Generate a file and print build instructions.
	 */
	public static void generate(CSymbolGen gen, String file, PrintStream out) {
		if (file != null) {
			var path = gen.generateFile(Paths.get(file));
			printInstructions(out, path);
		} else {
			var generated = gen.generate();
			if (out != null) out.println(generated);
			printInstructions(out, null);
		}
	}

	private static void printInstructions(PrintStream out, Path path) {
		if (out == null) return;
		if (path != null) out.println("Generated c file: " + path.toAbsolutePath());
		var file = path == null ? "file" : IoUtil.fileNameWithoutExt(path);
		out.println("Build:");
		out.printf("  gcc -I<header-include-path> %1$s.c -o %1$s; chmod a+x ./%1$s%n", file);
		out.printf("  gcc %1$s.c -o %1$s; chmod a+x ./%1$s%n", file);
		out.println();
	}

	public static FieldFilter filter() {
		return new FieldFilter();
	}

	public static FieldFilter fieldFilter() {
		return filter().flags(FIELD_FLAGS).types(FIELD_TYPES).excludeNames(FIELD_EXCLUDES);
	}

	public static FieldFilter intFilter() {
		return filter().types(INT_TYPES);
	}

	public static class FieldFilter implements Predicate<Field> {
		private Predicate<Field> predicate = _ -> true;

		private FieldFilter() {}

		public FieldFilter excludeRegex(String format, Object... args) {
			var pattern = RegexUtil.compile(format, args);
			return add(f -> !pattern.matcher(f.getName()).matches());
		}

		public FieldFilter exclude(String... excludedNames) {
			return excludeNames(Set.of(excludedNames));
		}

		public FieldFilter excludeNames(Collection<String> excludedNames) {
			return add(f -> !excludedNames.contains(f.getName()));
		}

		public FieldFilter exclude(Class<?>... classes) {
			return excludeClasses(Arrays.asList(classes));
		}

		public FieldFilter excludeClasses(Iterable<Class<?>> classes) {
			return add(f -> !memberOf(f, classes));
		}

		public FieldFilter types(Class<?>... includedTypes) {
			return types(Set.of(includedTypes));
		}

		public FieldFilter types(Iterable<Class<?>> includedTypes) {
			return add(f -> ReflectUtil.assignableFromAny(f.getType(), includedTypes));
		}

		public FieldFilter flags(AccessFlag... includedFlags) {
			return flags(Set.of(includedFlags));
		}

		public FieldFilter flags(Collection<AccessFlag> includedFlags) {
			return add(f -> f.accessFlags().containsAll(includedFlags));
		}

		@Override
		public boolean test(Field field) {
			return predicate.test(field);
		}

		public FieldFilter add(Predicate<Field> predicate) {
			this.predicate = this.predicate.and(predicate);
			return this;
		}
	}

	public static class Lines {
		private final CSymbolGen gen;
		private final List<String> lines = new ArrayList<>();

		private Lines(CSymbolGen gen) {
			this.gen = gen;
		}

		public Lines add(String format, Object... args) {
			lines.add(StringUtil.format(format, args));
			return this;
		}

		public Lines sizes(String... names) {
			for (var name : names)
				add("CERI_SIZE(%s);", name);
			return this;
		}

		public Lines fsize(String type, String... fields) {
			sizes(type);
			for (var field : fields)
				add("CERI_FSIZE(%s,%s);", type, field);
			return this;
		}

		public Lines syms(String... names) {
			for (var name : names)
				sym(name, null);
			return this;
		}

		public Lines sym(String name, Object value) {
			return add("CERI_SYM(%s);%s", name, comment(value));
		}

		public Lines symi(String name, Object value) {
			return add("CERI_SYMI(%s);%s", name, comment(value));
		}

		public Lines symiIfDef(String cdefine, String name, Object value) {
			return symiIf(cdefine == null ? null : "defined(" + cdefine + ")", name, value);
		}

		public Lines symiIf(String condition, String name, Object value) {
			return condition == null ? symi(name, value) : add("#if " + condition).symi(name, value)
				.add("#else").sym(name, value).add("#endif");
		}

		public Lines vsymi(String name, Object value) {
			return add("CERI_VSYMI(%s,%s);", name, value);
		}

		public Lines vsymiIfDef(String cdefine, String name, Object value) {
			return vsymiIf(cdefine == null ? null : "defined(" + cdefine + ")", name, value);
		}

		public Lines vsymiIf(String condition, String name, Object value) {
			return condition == null ? vsymi(name, value) : add("#if " + condition)
				.vsymi(name, value).add("#else").sym(name, value).add("#endif");
		}

		public Lines add(Class<?>... classes) {
			return add(Arrays.asList(classes));
		}

		public Lines add(Iterable<Class<?>> classes) {
			for (var cls : classes)
				gen.add(this, cls);
			return this;
		}

		private Lines include(Iterable<String> includes) {
			for (var include : includes)
				add("#include <%s.h>", include);
			return this;
		}

		private void appendTo(StringBuilder out, int tabs) {
			for (var line : lines) {
				if (!line.startsWith("#")) for (int i = 0; i < tabs; i++)
					out.append('\t');
				out.append(line).append('\n');
			}
		}
	}

	public static class Section {
		private final CSymbolGen gen;
		private final int tabs;
		public final Lines lines;
		private final Map<JnaOs, Lines> osLines = new LinkedHashMap<>();

		public Section(CSymbolGen gen, int tabs) {
			this.gen = gen;
			this.tabs = tabs;
			lines = new Lines(gen);
		}

		public Lines lines(JnaOs arch) {
			if (!JnaOs.known(arch)) return lines;
			return osLines.computeIfAbsent(arch, _ -> new Lines(gen));
		}

		@Override
		public String toString() {
			return appendTo(new StringBuilder()).toString();
		}

		private StringBuilder appendTo(StringBuilder out) {
			lines.appendTo(out, tabs);
			osLines.forEach((arch, lines) -> {
				out.append("#ifdef " + arch.cdefine + '\n');
				lines.appendTo(out, tabs);
				out.append("#endif\n");
			});
			return out;
		}
	}

	public static CSymbolGen of() {
		return IoUtil.RUNTIME_IO_ADAPTER
			.get(() -> new CSymbolGen(IoUtil.resourceString(CSymbolGen.class, TEMPLATE_DEF)));
	}

	private CSymbolGen(String template) {
		pre = new Section(this, 0);
		main = new Section(this, 1);
		this.template = template;
		intFilter(intFilter());
		fieldFilter(fieldFilter());
		pre.lines.add("// Generated by %s (%s) %s\n", ReflectUtil.className(this), OsUtil.os(),
			DateUtil.nowSec());
		include(INCLUDES);
	}

	public CSymbolGen include(String... includes) {
		return include(Arrays.asList(includes));
	}

	public CSymbolGen include(Iterable<String> includes) {
		return include(JnaOs.unknown, includes);
	}

	public CSymbolGen include(JnaOs arch, String... includes) {
		return include(arch, Arrays.asList(includes));
	}

	public CSymbolGen include(JnaOs arch, Iterable<String> includes) {
		pre.lines(arch).include(includes);
		return this;
	}

	public CSymbolGen fieldFilter(Predicate<Field> fieldFilter) {
		this.fieldFilter = fieldFilter;
		return this;
	}

	public CSymbolGen intFilter(Predicate<Field> intFilter) {
		this.intFilter = intFilter;
		return this;
	}

	@SafeVarargs
	public final CSymbolGen enums(boolean c, String valueField, Class<?>... enumClasses) {
		return enums(c, valueField, Arrays.asList(enumClasses));
	}

	public CSymbolGen enums(boolean c, String valueField, Iterable<Class<?>> enumClasses) {
		for (var enumCls : enumClasses)
			enums(new EnumType(enumCls, c, valueField));
		return this;
	}

	public CSymbolGen enums(EnumType... types) {
		for (var type : types)
			enumTypes.put(type.cls, type);
		return this;
	}

	public String generate() {
		var template = this.template;
		template = template.replace(PLACEHOLDER_PRE, pre.toString());
		template = template.replace(PLACEHOLDER_MAIN, main.toString());
		return template;
	}

	public Path generateFile(Path path) {
		return IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			Files.writeString(path, generate());
			return path;
		});
	}

	private void add(Lines lines, Class<?> cls) {
		var fields = fields(cls);
		if (!fields.isEmpty()) {
			lines.add("").add("printf(\"\\n// %s types\\n\");", ReflectUtil.name(cls));
			fields.forEach(f -> addSymbol(lines, f));
		}
		for (var c : cls.getDeclaredClasses())
			add(lines, c);
	}

	private void addSymbol(Lines lines, Field field) {
		var value = ReflectUtil.publicFieldValue(null, field);
		if (value instanceof Enum<?> en) addEnumSymbol(lines, field, en);
		else addSymbol(lines, field, value);
	}

	private void addEnumSymbol(Lines lines, Field field, Enum<?> en) {
		var type = enumTypes.getOrDefault(en.getClass(), EnumType.NULL);
		var value = enumValue(type, en);
		if (!type.c) addSymbol(lines, field, value); // not a c enum
		else lines.vsymi(field.getName(), intValue(value)); // c enum
	}

	private Object enumValue(EnumType type, Enum<?> en) {
		var valueField = BasicUtil.defaultValue(type.valueField, ENUM_VALUE_FIELD_DEF);
		return ReflectUtil.publicValue(en, valueField);
	}

	private void addSymbol(Lines lines, Field field, Object value) {
		var name = field.getName();
		if (intFilter.test(field)) {
			if (value != null) lines.vsymiIfDef(name, name, intValue(value));
			else lines.symiIfDef(name, name, value);
		} else lines.sym(name, value);
	}

	private Object intValue(Object value) {
		return switch (value) {
			case Byte b -> MathUtil.ubyte(b);
			case Short s -> MathUtil.ushort(s);
			case Integer i -> MathUtil.uint(i);
			case Number n -> n.longValue();
			default -> value;
		};
	}

	private List<Field> fields(Class<?> cls) {
		return Stream.of(cls.getDeclaredFields()).filter(fieldFilter).toList();
	}

	private static String comment(Object value) {
		return value == null ? "" : " /* " + JnaArgs.DEFAULT.arg(value) + " */";
	}

	private static boolean memberOf(Field f, Iterable<Class<?>> classes) {
		var fcls = f.getDeclaringClass();
		for (var cls : classes)
			if (ReflectUtil.same(fcls, cls)) return true;
		return false;
	}
}
