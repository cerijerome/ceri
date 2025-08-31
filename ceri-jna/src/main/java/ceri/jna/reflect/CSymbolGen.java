package ceri.jna.reflect;

import static ceri.common.exception.ExceptionAdapter.shouldNotThrow;
import static java.lang.reflect.AccessFlag.FINAL;
import static java.lang.reflect.AccessFlag.PUBLIC;
import static java.lang.reflect.AccessFlag.STATIC;
import java.io.PrintStream;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import ceri.common.collection.Lists;
import ceri.common.collection.Sets;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.io.IoUtil;
import ceri.common.reflect.ClassReloader;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Strings;
import ceri.common.text.TextUtil;
import ceri.common.time.DateUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.OsUtil;
import ceri.jna.type.IntType;
import ceri.jna.util.JnaArgs;
import ceri.jna.util.JnaOs;
import ceri.jna.util.JnaUtil;

/**
 * Generates c code to compile and run on a target system, in order to print symbol values and
 * definitions. Useful to verify constant values for JNA code.
 */
public class CSymbolGen {
	private static final String PLACEHOLDER = "//$$PLACEHOLDER$$";
	private static final String TEMPLATE = "symbols-template.c";
	private static final String LOCATION_DEF = "src/test/c/";
	private static final String FILENAME_DEF = "symbols";
	private static final String C_EXT = ".c";
	private static final List<Class<?>> TYPE_IGNORE_FIELDS =
		List.of(Structure.class, NativeMapped.class, Enum.class, Record.class);
	private static final List<AccessFlag> TYPE_ACCESS_FLAGS = List.of(PUBLIC, STATIC);
	private static final List<AccessFlag> FIELD_ACCESS_FLAGS = List.of(PUBLIC, STATIC, FINAL);
	private static final List<Class<?>> FIELD_TYPES =
		List.of(Number.class, long.class, int.class, short.class, byte.class);
	private final String template;
	private final JnaOs os;
	/** Provides filters to change default behavior. */
	public final Overrides overrides = new Overrides();
	/** Allows includes to be added. */
	public final Includes includes = new Includes();
	/** Allows macros to be added. */
	public final Macros macros = new Macros();
	/** Allows lines to be added directly. */
	public final Lines lines = new Lines();
	private PrintStream out = System.out;

	/**
	 * Automatic generation of c code based on annotations.
	 */
	public static class Auto {

		private Auto() {}

		/**
		 * Generate based on the class annotations.
		 */
		public static void gen(Class<?> cls) {
			gen(cls, null);
		}

		/**
		 * Generate based on the class annotations, with pre-process configurator.
		 */
		public static <E extends Exception> void gen(Class<?> cls,
			Excepts.BiConsumer<E, JnaOs, CSymbolGen> configurator) throws E {
			gen(CAnnotations.cgen(cls), cls, configurator);
		}

		private static <E extends Exception> void gen(CAnnotations.CGen.Value cgen, Class<?> cls,
			Excepts.BiConsumer<E, JnaOs, CSymbolGen> configurator) throws E {
			var location = location(cgen, cls);
			for (var os : cgen.os())
				os.accept(_ -> genOs(os, cgen, location, configurator));
		}

		private static <E extends Exception> void genOs(JnaOs os, CAnnotations.CGen.Value cgen,
			Path location, Excepts.BiConsumer<E, JnaOs, CSymbolGen> configurator) throws E {
			var file = IoUtil.changeName(location, os::file);
			var reloader = ClassReloader.ofNested(cgen.classes());
			var targets = Stream.of(cgen.target()).map(c -> reloader.forName(c, false)).toList();
			var gen = CSymbolGen.of();
			if (configurator != null) configurator.accept(os, gen);
			gen.add(targets).generateFile(file);
		}
	}

	/**
	 * Matching rules based on OS and type.
	 */
	public static class Matcher<T, R> {
		private final R undefined;
		private final List<Match<T, R>> matchers = Lists.of();

		private record Match<T, R>(Functions.BiPredicate<? super T, JnaOs> predicate,
			Functions.BiFunction<? super T, JnaOs, R> supplier) {}

		private Matcher(R undefined) {
			this.undefined = undefined;
		}

		/**
		 * Add a match with undefined response.
		 */
		public void add(Functions.BiPredicate<? super T, JnaOs> predicate) {
			add(predicate, undefined);
		}

		/**
		 * Add a match with fixed response.
		 */
		public void add(Functions.BiPredicate<? super T, JnaOs> predicate, R value) {
			add(predicate, (_, _) -> value);
		}

		/**
		 * Add a match with dynamic response.
		 */
		public void add(Functions.BiPredicate<? super T, JnaOs> predicate,
			Functions.BiFunction<? super T, JnaOs, R> supplier) {
			matchers.add(new Match<>(predicate, supplier));
		}

		private R match(T t, JnaOs os) {
			for (var matcher : matchers) {
				if (!matcher.predicate().test(t, os)) continue;
				var value = matcher.supplier().apply(t, os);
				if (value != null) return value;
			}
			return null;
		}
	}

	/**
	 * Overrides annotations or default handling of types and fields, including enums.
	 */
	public static class Overrides {
		public final Matcher<Class<?>, CAnnotations.CInclude.Value> includes =
			new Matcher<>(CAnnotations.CInclude.Value.NONE);
		public final Matcher<Class<?>, CAnnotations.CType.Value> classes =
			new Matcher<>(CAnnotations.CType.Value.UNDEFINED);
		public final Matcher<Field, CAnnotations.CType.Value> fields =
			new Matcher<>(CAnnotations.CType.Value.UNDEFINED);
		public final Matcher<Enum<?>, CAnnotations.CType.Value> enums =
			new Matcher<>(CAnnotations.CType.Value.UNDEFINED);

		private Set<String> includes(Class<?> cls, JnaOs os) {
			return BasicUtil.def(includes.match(cls, os), () -> CAnnotations.cincludes(cls))
				.includes(os);
		}

		private CAnnotations.CType.Value ctype(Class<?> cls, JnaOs os) {
			return BasicUtil.def(classes.match(cls, os), () -> CAnnotations.ctype(cls, os));
		}

		private CAnnotations.CType.Value ctype(Field field, JnaOs os) {
			return BasicUtil.def(fields.match(field, os), () -> CAnnotations.ctype(field, os));
		}

		private CAnnotations.CType.Value ctype(Enum<?> en, JnaOs os) {
			return BasicUtil.def(enums.match(en, os), () -> CAnnotations.ctype(en, os));
		}
	}

	/**
	 * Collects includes.
	 */
	public class Includes {
		private static final List<String> TEMPLATE_INCLUDES = List.of("stdio.h", "string.h");
		private final Set<String> includes = Sets.link();

		private Includes() {
			add(TEMPLATE_INCLUDES);
		}

		/**
		 * Add include lines.
		 */
		public CSymbolGen add(String... includes) {
			return add(Arrays.asList(includes));
		}

		/**
		 * Add include lines.
		 */
		public CSymbolGen add(Collection<String> includes) {
			this.includes.addAll(includes);
			return CSymbolGen.this;
		}

		private String generate() {
			var lines = new Lines();
			for (var include : includes)
				lines.add("#include <%s>", include);
			return lines.generate(false);
		}
	}

	/**
	 * Generate template macros.
	 */
	public class Macros {

		private Macros() {}

		/**
		 * Macro to display a symbol value, which may be undefined.
		 */
		public CSymbolGen sym(String name, Object value) {
			return lines.add("CERI_SYM(%s);%s", name, comment(value));
		}

		/**
		 * Macro to display an integer symbol value; will fail to build if undefined.
		 */
		public CSymbolGen symi(String name, Object value) {
			return lines.add("CERI_SYMI(%s);%s", name, comment(value));
		}

		/**
		 * Macro to display and verify an integer symbol value; will fail to build if undefined.
		 */
		public CSymbolGen vsymi(String name, Object value) {
			return lines.add("CERI_VSYMI(%s,%s);", name, value);
		}

		/**
		 * Macro to display a type size; will fail to build if undefined.
		 */
		public CSymbolGen size(String type) {
			return lines.add("CERI_SIZE(%s);", type);
		}

		/**
		 * Macro to display a type field size in bytes; will fail to build if undefined.
		 */
		public CSymbolGen fsize(String type, String field) {
			return lines.add("CERI_FSIZE(%s,%s);", type, field);
		}

		/**
		 * Macro to display and verify a type size; will fail to build if undefined.
		 */
		public CSymbolGen vsize(String type, int size) {
			return lines.add("CERI_VSIZE(%s,%s);", type, size);
		}

		private String comment(Object value) {
			if (value == null) return "";
			return String.format("/* " + JnaArgs.DEFAULT.arg(value) + " */");
		}
	}

	/**
	 * Generate lines.
	 */
	public class Lines {
		private final List<String> lines = Lists.of();

		private Lines() {}

		/**
		 * Add a new line.
		 */
		public CSymbolGen add(String format, Object... args) {
			lines.add(Strings.format(format, args));
			return CSymbolGen.this;
		}

		/**
		 * Add a preprocessor #if defined construct.
		 */
		public <E extends Exception> CSymbolGen addIfDef(String defined,
			Excepts.Runnable<E> runIf) throws E {
			return addIfDef(defined, runIf, null);
		}

		/**
		 * Add a preprocessor #if defined/#else construct.
		 */
		public <E extends Exception> CSymbolGen addIfDef(String defined,
			Excepts.Runnable<E> runIf, Excepts.Runnable<E> runElse) throws E {
			var condition = (defined == null ? null : "defined(" + defined + ")");
			return addIf(condition, runIf, runElse);
		}

		/**
		 * Add a preprocessor #if construct.
		 */
		public <E extends Exception> CSymbolGen addIf(String condition, Excepts.Runnable<E> runIf)
			throws E {
			return addIf(condition, runIf, null);
		}

		/**
		 * Add a preprocessor #if/#else construct.
		 */
		public <E extends Exception> CSymbolGen addIf(String condition, Excepts.Runnable<E> runIf,
			Excepts.Runnable<E> runElse) throws E {
			if (condition != null) add("#if " + condition);
			runIf.run();
			if (condition != null && runElse != null) add("#else");
			if (runElse != null) runElse.run();
			if (condition != null) add("#endif");
			return CSymbolGen.this;
		}

		/**
		 * Add a printf line; special (non-escaped) chars are allowed.
		 */
		public CSymbolGen printf(String format, Object... args) {
			return add("printf(\"%s\");", Chars.escape(Strings.format(format, args)));
		}

		/**
		 * Append content to the last line.
		 */
		public CSymbolGen append(String format, Object... args) {
			var line = lines.isEmpty() ? "" : lines.removeLast();
			return add(line + Strings.format(format, args));
		}

		/**
		 * Append comment to the last line.
		 */
		public CSymbolGen appendComment(String format, Object... args) {
			return append(" /* " + Strings.format(format, args) + " */");
		}

		private String generate(boolean indent) {
			var b = new StringBuilder();
			for (var line : lines) {
				if (indent && !line.startsWith("#")) b.append('\t');
				b.append(line).append('\n');
			}
			return b.toString();
		}
	}

	/**
	 * Create a generator instance.
	 */
	public static CSymbolGen of() {
		return new CSymbolGen(JnaOs.current());
	}

	private CSymbolGen(JnaOs os) {
		this.template = shouldNotThrow.get(() -> IoUtil.resourceString(getClass(), TEMPLATE));
		this.os = os;
	}

	/**
	 * Change the generation output stream
	 */
	public CSymbolGen out(PrintStream out) {
		if (out == null) out = IoUtil.nullPrintStream();
		this.out = out;
		return this;
	}

	/**
	 * Extract c fields and types from the classes.
	 */
	public CSymbolGen add(Class<?>... classes) {
		return add(Arrays.asList(classes));
	}

	/**
	 * Extract c fields and types from the classes.
	 */
	public CSymbolGen add(Iterable<? extends Class<?>> classes) {
		for (var cls : classes) {
			var ctype = overrides.ctype(cls, os);
			if (ctype.undefined()) continue;
			lines.add("").lines.printf("\n");
			addType(cls, ctype);
		}
		return this;
	}

	/**
	 * Generate c code and save to the file.
	 */
	public String generateFile(Path file) {
		return ExceptionAdapter.runtimeIo.get(() -> {
			var filename = IoUtil.filenameWithoutExt(file);
			var gen = generate(filename);
			Files.writeString(file, gen);
			out.println(gen);
			out.println("Generated file: " + file);
			out.println();
			return gen;
		});
	}

	/**
	 * Generate c code.
	 */
	public String generate() {
		var gen = generate(FILENAME_DEF);
		out.println(gen);
		return gen;
	}

	private String generate(String filename) {
		return header(filename, os) + includes.generate()
			+ template.replace(PLACEHOLDER, lines.generate(true));
	}

	private void addType(Class<?> cls, CAnnotations.CType.Value ctype) {
		includes.add(overrides.includes(cls, os));
		if (!Reflect.assignableFromAny(cls, TYPE_IGNORE_FIELDS)) addFields(cls);
		if (addSpecialType(cls, ctype)) return;
		addNestedTypes(cls);
	}

	private void addNestedTypes(Class<?> outer) {
		for (var cls : outer.getDeclaredClasses()) {
			if (!cls.accessFlags().containsAll(TYPE_ACCESS_FLAGS)) continue;
			var ctype = overrides.ctype(cls, os);
			if (ctype.undefined()) continue;
			addType(cls, ctype);
		}
	}

	private int addFields(Class<?> cls) {
		int count = 0;
		for (var field : cls.getDeclaredFields()) {
			if (!field.accessFlags().containsAll(FIELD_ACCESS_FLAGS)) continue;
			if (!Reflect.assignableFromAny(field.getType(), FIELD_TYPES)) continue;
			if (count++ == 0) printType(cls);
			var ctype = overrides.ctype(field, os);
			if (ctype.undefined()) lines.add("// Ignore: %s = %s", field.getName(),
				Reflect.publicFieldValue(null, field));
			else addField(field, ctype);
		}
		return count;
	}

	private void addField(Field field, CAnnotations.CType.Value ctype) {
		var fieldName = field.getName();
		var name = ctype.name(fieldName);
		var value = Reflect.publicFieldValue(null, field);
		macros.vsymi(name, longValue(value, ctype.signed()));
		if (!fieldName.equals(name)) lines.appendComment(fieldName);
	}

	private boolean addSpecialType(Class<?> cls, CAnnotations.CType.Value ctype) {
		if (Structure.class.isAssignableFrom(cls)) addStruct(BasicUtil.unchecked(cls), ctype);
		else if (Enum.class.isAssignableFrom(cls))
			addEnums(BasicUtil.unchecked(cls), ctype.valueField());
		else if (IntType.class.isAssignableFrom(cls)) addIntType(BasicUtil.unchecked(cls), ctype);
		else return false;
		return true;
	}

	private void addEnums(Class<? extends Enum<?>> cls, String valueField) {
		printType(cls);
		for (var en : cls.getEnumConstants()) {
			var ctype = overrides.ctype(en, os);
			if (ctype.undefined())
				lines.add("// Ignore: %s = %s", en.name(), Reflect.publicValue(en, valueField));
			else addEnum(en, valueField, ctype);
		}
	}

	private void addEnum(Enum<?> en, String valueField, CAnnotations.CType.Value ctype) {
		var enName = en.name();
		var name = ctype.name(enName);
		var value = Reflect.publicValue(en, valueField);
		macros.vsymi(name, longValue(value, ctype.signed()));
		if (!enName.equals(name)) lines.appendComment(enName);
	}

	private void addStruct(Class<? extends Structure> cls, CAnnotations.CType.Value ctype) {
		if (cls.accessFlags().contains(AccessFlag.ABSTRACT)) return;
		printType(cls);
		var clsName = cls.getSimpleName();
		var name = ctype.name(clsName);
		macros.vsize(structDecl(name, cls, ctype), structSize(cls));
		if (!clsName.equals(name)) lines.appendComment(clsName);
	}

	private void addIntType(Class<? extends IntType<?>> cls, CAnnotations.CType.Value ctype) {
		if (cls.accessFlags().contains(AccessFlag.ABSTRACT)) return;
		printType(cls);
		var clsName = cls.getSimpleName();
		var name = ctype.name(clsName);
		macros.vsize(name, intTypeSize(cls));
		if (!clsName.equals(name)) lines.appendComment(clsName);
	}

	private void printType(Class<?> cls) {
		lines.printf("// %s\n", Reflect.name(cls));
	}

	private static Object longValue(Object value, boolean signed) {
		var n = JnaUtil.asLong(value, signed);
		if (n != null) return n;
		throw new IllegalArgumentException("Unsupported field value: " + value);
	}

	private static String structDecl(String name, Class<?> cls, CAnnotations.CType.Value ctype) {
		if (ctype.typedef()) return name;
		if (Union.class.isAssignableFrom(cls)) return "union " + name;
		return "struct " + name;
	}

	private static int structSize(Class<? extends Structure> cls) {
		var t = Reflect.create(cls);
		if (t == null) t = Reflect.create(cls, Pointer.class, Pointer.NULL);
		if (t != null) return t.size();
		throw new IllegalArgumentException("Unable to determine size: " + Reflect.name(cls));
	}

	private static int intTypeSize(Class<? extends IntType<?>> cls) {
		var t = Reflect.create(cls);
		if (t == null) t = Reflect.create(cls, int.class, 0);
		if (t == null) t = Reflect.create(cls, long.class, 0L);
		if (t != null) return t.size;
		throw new IllegalArgumentException("Unable to determine size: " + Reflect.name(cls));
	}

	private static Path location(CAnnotations.CGen.Value cgen, Class<?> cls) {
		var location = cgen.location(LOCATION_DEF);
		if (location.endsWith("/")) location += TextUtil.camelToHyphenated(cls.getSimpleName());
		if (!location.endsWith(C_EXT)) location += C_EXT;
		return Path.of(location);
	}

	private static String header(String filename, JnaOs os) {
		return String.format("""
			/*
			 * Generated for %s by %s (%s) %s
			 *
			 * Build:  gcc %s.c -o %s; chmod a+x ./%s
			 *  Run:  ./%s
			 */
			""", os, Reflect.name(CSymbolGen.class), OsUtil.value(), DateUtil.nowSec(),
			filename, filename, filename, filename);
	}
}
