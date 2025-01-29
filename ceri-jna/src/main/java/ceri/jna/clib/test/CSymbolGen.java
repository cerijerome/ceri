package ceri.jna.clib.test;

import static java.lang.reflect.AccessFlag.FINAL;
import static java.lang.reflect.AccessFlag.STATIC;
import java.io.IOException;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ceri.common.io.IoUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.time.DateUtil;

/**
 * Generates c code to compile and run on a target system, in order to print symbol values and
 * definitions. Useful to determine constant values for JNA code.
 */
public class CSymbolGen {
	private static final String PLACEHOLDER_PRE = "$$CSYMBOLGEN_PRE$$";
	private static final String PLACEHOLDER_MAIN = "$$CSYMBOLGEN_MAIN$$";
	private static final String TEMPLATE_DEF = "symbols-template.c";
	private static final Set<Class<?>> INT_TYPES = Set.of(Long.class, Integer.class, Short.class,
		Byte.class, long.class, int.class, short.class, byte.class);
	private final String template;
	public final Section pre = new Section();
	private final List<Section> main = new ArrayList<>();

	public static String generateForTest() throws IOException {
		var gen = CSymbolGen.of();
		gen.pre.lines.add("").add("#define EMPTY").add("#define STRING \"string\"");
		gen.main().lines.sym("UNDEFINED", "EMPTY", "STRING");
		return gen.generate();
	}

	public static FieldFilter filter() {
		return new FieldFilter();
	}

	public static FieldFilter filterDef() {
		return filter().flags(STATIC, FINAL).types(Number.class, long.class, int.class, short.class,
			byte.class);
	}

	public static class FieldFilter implements Predicate<Field> {
		private Predicate<Field> predicate = _ -> true;

		private FieldFilter() {}

		public FieldFilter excludeRegex(String format, Object... args) {
			var pattern = RegexUtil.compile(format, args);
			return add(f -> !pattern.matcher(f.getName()).matches());
		}

		public FieldFilter exclude(String... excludedNames) {
			return exclude(Set.of(excludedNames));
		}

		public FieldFilter exclude(Collection<String> excludedNames) {
			return add(f -> !excludedNames.contains(f.getName()));
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

		private FieldFilter add(Predicate<Field> predicate) {
			this.predicate = this.predicate.and(predicate);
			return this;
		}
	}

	public static enum Arch {
		apple("__APPLE__"),
		linux("__linux__");

		public final String define;

		private Arch(String define) {
			this.define = define;
		}
	}

	public static class Lines {
		private final List<String> lines = new ArrayList<>();

		public Lines add(String format, Object... args) {
			lines.add(StringUtil.format(format, args));
			return this;
		}

		public Lines include(String... names) {
			for (var name : names)
				add("#include <%s.h>", name);
			return this;
		}

		public Lines size(String... names) {
			for (var name : names)
				add("SIZE(%s);", name);
			return this;
		}

		public Lines fsize(String type, String... fields) {
			size(type);
			for (var field : fields)
				add("FSIZE(%s,%s);", type, field);
			return this;
		}

		public Lines sym(String... names) {
			for (var name : names)
				add("SYM(%s);", name);
			return this;
		}

		public Lines symi(String... names) {
			for (var name : names)
				add("SYMI(%s);", name);
			return this;
		}

		public void add(Predicate<Field> predicate, Class<?>... classes) {
			add(predicate, filter().types(INT_TYPES), classes);
		}

		public void add(Predicate<Field> predicate, Predicate<Field> useSymInt,
			Class<?>... classes) {
			for (var cls : classes)
				CSymbolGen.add(this, cls, predicate, useSymInt);
		}
	}

	public static class Section {
		public final Lines lines = new Lines();
		private final Map<Arch, Lines> archLines = new LinkedHashMap<>();

		public Lines lines(Arch arch) {
			return archLines.computeIfAbsent(arch, _ -> new Lines());
		}

		public void appendTo(StringBuilder out, int tabs) {
			for (var line : lines.lines)
				appendTo(line, out, tabs);
			archLines.forEach((arch, lines) -> {
				out.append("#ifdef " + arch.define + '\n');
				for (var line : lines.lines)
					appendTo(line, out, tabs);
				out.append("#endif\n");
			});
		}

		private void appendTo(String line, StringBuilder out, int tabs) {
			if (!line.startsWith("#")) for (int i = 0; i < tabs; i++)
				out.append('\t');
			out.append(line).append('\n');
		}
	}

	public static CSymbolGen of() throws IOException {
		return new CSymbolGen(IoUtil.resourceString(CSymbolGen.class, TEMPLATE_DEF));
	}

	private CSymbolGen(String template) {
		this.template = template;
		next().pre.lines
			.add("// Generated by %s %s\n", ReflectUtil.className(this), DateUtil.nowSec())
			.include("stdlib", "stdio", "string", "ctype");
	}

	public Section main() {
		return main.getLast();
	}

	public CSymbolGen next() {
		main.add(new Section());
		return this;
	}

	public String generate() {
		var template = this.template;
		template = template.replace(PLACEHOLDER_PRE, preLines());
		template = template.replace(PLACEHOLDER_MAIN, mainLines());
		return template;
	}

	public Path generateFile(Path path) throws IOException {
		Files.writeString(path, generate());
		return path;
	}

	private String preLines() {
		var b = new StringBuilder();
		pre.appendTo(b, 0);
		return b.toString();
	}

	private String mainLines() {
		var b = new StringBuilder();
		for (var section : main)
			section.appendTo(b, 1);
		return b.toString();
	}

	private static void add(Lines lines, Class<?> cls, Predicate<Field> predicate,
		Predicate<Field> useSymInt) {
		var fields = fields(cls, predicate);
		if (!fields.isEmpty()) {
			lines.add("").add("printf(\"\\n// %s types\\n\");", ReflectUtil.name(cls));
			fields.forEach(f -> addSymbol(lines, f, useSymInt));
		}
		for (var c : cls.getDeclaredClasses())
			add(lines, c, predicate, useSymInt);
	}

	private static void addSymbol(Lines lines, Field field, Predicate<Field> useSymInt) {
		var name = field.getName();
		if (useSymInt.test(field))
			lines.add("#ifdef %s", name).symi(name).add("#else").sym(name).add("#endif");
		else lines.sym(name);
	}

	private static List<Field> fields(Class<?> cls, Predicate<Field> predicate) {
		return Stream.of(cls.getDeclaredFields()).filter(predicate).toList();
	}
}
