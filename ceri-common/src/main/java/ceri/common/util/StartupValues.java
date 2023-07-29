package ceri.common.util;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.property.PathFactory;
import ceri.common.text.NumberParser;
import ceri.common.text.StringUtil;

/**
 * Utility to read parameters from args[], system properties, and environment variables.
 */
public class StartupValues {
	private final String[] args;
	private Class<?> pkgPrefix = null;
	private int index = 0;
	private Consumer<String> notifier = s -> {};

	/**
	 * Lookup system property or environment variable by name
	 */
	public static Value lookup(String name) {
		return of().value(name);
	}

	/**
	 * Lookup system property or environment variable by name
	 */
	public static Value lookup(String sysProp, String envVar) {
		return of().value(sysProp, envVar);
	}

	/**
	 * Creates an instance that prints value assignments to stdout.
	 */
	public static StartupValues sysOut(String... args) {
		return of(args).notifier(System.out::println);
	}

	/**
	 * Creates an instance with no notification.
	 */
	public static StartupValues of(String... args) {
		return new StartupValues(args);
	}

	/**
	 * A wrapper to perform value assignment, and provide typed access.
	 */
	public static class Value {
		public final String name;
		public final Integer argIndex;
		public final String sysProp;
		public final String envVar;
		private String[] args;
		private Consumer<String> notifier;

		Value(String name, Integer argIndex, String sysProp, String envVar) {
			this.name = name;
			this.argIndex = argIndex;
			this.sysProp = sysProp;
			this.envVar = envVar;
		}

		Value args(String... args) {
			this.args = args;
			return this;
		}

		Value notifier(Consumer<String> notifier) {
			this.notifier = notifier;
			return this;
		}

		public Boolean asBool() {
			return asBool(null);
		}

		public Boolean asBool(Boolean def) {
			return asBoolFrom(() -> def);
		}

		public <E extends Exception> Boolean asBoolFrom(ExceptionSupplier<E, Boolean> defSupplier)
			throws E {
			return applyFrom(Boolean::parseBoolean, defSupplier);
		}

		public Integer asInt() {
			return asInt(null);
		}

		public Integer asInt(Integer def) {
			return asIntFrom(() -> def);
		}

		public <E extends Exception> Integer asIntFrom(ExceptionSupplier<E, Integer> defSupplier)
			throws E {
			return applyFrom(NumberParser::decodeInt, defSupplier);
		}

		public Long asLong() {
			return asLong(null);
		}

		public Long asLong(Long def) {
			return asLongFrom(() -> def);
		}

		public <E extends Exception> Long asLongFrom(ExceptionSupplier<E, Long> defSupplier)
			throws E {
			return applyFrom(NumberParser::decodeLong, defSupplier);
		}

		public Double asDouble() {
			return asDouble(null);
		}

		public Double asDouble(Double def) {
			return asDoubleFrom(() -> def);
		}

		public <E extends Exception> Double asDoubleFrom(ExceptionSupplier<E, Double> defSupplier)
			throws E {
			return applyFrom(Double::parseDouble, defSupplier);
		}

		public int[] asIntArray() {
			return asIntArray(ArrayUtil.EMPTY_INT);
		}

		public int[] asIntArray(int... def) {
			return asIntArrayFrom(() -> def);
		}

		public <E extends Exception> int[] asIntArrayFrom(ExceptionSupplier<E, int[]> defSupplier)
			throws E {
			return applyFromStream(s -> s.mapToInt(NumberParser::decodeInt).toArray(),
				defSupplier);
		}

		public long[] asLongArray() {
			return asLongArray(ArrayUtil.EMPTY_LONG);
		}

		public long[] asLongArray(long... def) {
			return asLongArrayFrom(() -> def);
		}

		public <E extends Exception> long[]
			asLongArrayFrom(ExceptionSupplier<E, long[]> defSupplier) throws E {
			return applyFromStream(s -> s.mapToLong(NumberParser::decodeLong).toArray(),
				defSupplier);
		}

		public <T extends Enum<T>> T asEnum(T def) {
			Objects.requireNonNull(def);
			Class<T> cls = BasicUtil.uncheckedCast(def.getClass());
			return applyFrom(s -> Enum.valueOf(cls, s), () -> def);
		}

		public Path asPath() {
			return asPath(null);
		}

		public Path asPath(Path def) {
			return asPathFrom(() -> def);
		}

		public <E extends Exception> Path asPathFrom(ExceptionSupplier<E, Path> defSupplier)
			throws E {
			return applyFrom(Path::of, defSupplier);
		}

		public <E extends Exception, T> T apply(ExceptionFunction<E, String, T> fn) throws E {
			return apply(fn, null);
		}

		public <E extends Exception, T> T apply(ExceptionFunction<E, String, T> fn, T def)
			throws E {
			return applyFrom(fn, () -> def);
		}

		public <E extends Exception, T> T applyFromStream(
			ExceptionFunction<E, Stream<String>, T> streamFn, ExceptionSupplier<E, T> defSupplier)
			throws E {
			if (streamFn == null) return applyFrom(null, defSupplier);
			return applyFrom(s -> streamFn.apply(StringUtil.commaSplit(s).stream()), defSupplier);
		}

		public <E extends Exception, T> T applyFrom(ExceptionFunction<E, String, T> fn,
			ExceptionSupplier<E, T> defSupplier) throws E {
			String value = get();
			if (value != null && fn != null) return fn.apply(value);
			if (defSupplier == null) return null;
			T t = defSupplier.get();
			return notify(t, "%s = %s (default)", name(), ArrayUtil.deepToString(t));
		}

		public String get() {
			return get(null);
		}

		public String get(String def) {
			String value = arg();
			if (value == null) value = sysProp();
			if (value == null) value = envVar();
			if (value == null) value = defaultValue(def);
			return value;
		}

		private String arg() {
			if (args == null || argIndex == null || argIndex < 0 || argIndex >= args.length)
				return null;
			String s = args[argIndex];
			return notify(s, "%s = %s (from args[%d])", name(), s, argIndex);
		}

		private String sysProp() {
			if (sysProp == null || sysProp.isEmpty()) return null;
			String s = SystemVars.sys(sysProp);
			return notify(s, "%s = %s (from system '%s')", name(), s, sysProp);
		}

		private String envVar() {
			if (envVar == null || envVar.isEmpty()) return null;
			String s = SystemVars.env(envVar);
			return notify(s, "%s = %s (from env '%s')", name(), s, envVar);
		}

		private String defaultValue(String def) {
			return notify(def, "%s = %s (default)", name, def);
		}

		private <T> T notify(T t, String format, Object... params) {
			if (t == null || notifier == null) return t;
			notifier.accept(String.format(format, params));
			return t;
		}

		private String name() {
			return name == null ? "value" : name;
		}
	}

	private StartupValues(String[] args) {
		this.args = args;
	}

	/**
	 * Set a notifier Creates an instance that prints value assignments to the given notifier.
	 */
	public StartupValues notifier(Consumer<String> notifier) {
		this.notifier = notifier;
		return this;
	}

	public StartupValues prefix(Class<?> pkgPrefix) {
		this.pkgPrefix = pkgPrefix;
		return this;
	}

	public StartupValues skip() {
		nextArg();
		return this;
	}

	public Value next() {
		return next(null);
	}

	public Value next(String name) {
		String sysProp = sysProp(pkgPrefix, name);
		String envVar = envVar(sysProp);
		return next(name, sysProp, envVar);
	}

	public Value next(String name, String sysProp, String envVar) {
		return createValue(name, nextArg(), sysProp, envVar);
	}

	public Value value(int index) {
		return value(null, index);
	}

	public Value value(String name) {
		String sysProp = sysProp(pkgPrefix, name);
		return createValue(name, null, sysProp, envVar(sysProp));
	}

	public Value value(String name, int index) {
		String sysProp = sysProp(pkgPrefix, name);
		return createValue(name, index, sysProp, envVar(sysProp));
	}

	public Value value(String sysProp, String envVar) {
		return createValue(null, null, sysProp, envVar);
	}

	public Value value(int index, String sysProp, String envVar) {
		return createValue(null, index, sysProp, envVar);
	}

	private Value createValue(String name, Integer argIndex, String sysProp, String envVar) {
		return new Value(name, argIndex, sysProp, envVar).args(args).notifier(notifier);
	}

	private String envVar(String sysProp) {
		if (sysProp == null) return null;
		return sysProp.toUpperCase().replace('.', '_');
	}

	private String sysProp(Class<?> cls, String suffix) {
		suffix = StringUtil.trim(suffix);
		if (cls == null) return suffix;
		if (suffix == null || suffix.isEmpty()) return null;
		return PathFactory.dot.path(cls.getPackageName(), suffix).value;
	}

	private int nextArg() {
		return index++;
	}

}
