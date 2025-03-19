package ceri.common.util;

import java.util.function.Consumer;
import java.util.function.Function;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.property.Parser;
import ceri.common.property.Separator;
import ceri.common.text.StringUtil;

/**
 * Utility to read parameters from args[], system properties, and environment variables.
 */
public class StartupValues {
	private final String[] args;
	private String prefix = null;
	private int index = 0;
	private Consumer<String> notifier = _ -> {};
	private Function<Object, String> renderer = ArrayUtil::deepToString;

	/**
	 * Parse system property or environment variable by name, without notification.
	 */
	public static Parser.String lookup(String name) {
		return of().value(name);
	}

	/**
	 * Parse system property or environment variable by name, without notification
	 */
	public static Parser.String lookup(String sysProp, String envVar) {
		return of().value(sysProp, envVar);
	}

	/**
	 * Parse system property or environment variable by name
	 */
	public static <E extends Exception, T> T lookup(String name,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		return of().value(name, fn);
	}

	/**
	 * Parse system property or environment variable by name
	 */
	public static <E extends Exception, T> T lookup(String sysProp, String envVar,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		return of().value(sysProp, envVar, fn);
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
	 * Identifies where to find the value.
	 */
	private record Id(String name, Integer argIndex, String sysProp, String envVar) {

		private String arg(String[] args) {
			if (args == null || argIndex() == null || argIndex() < 0 || argIndex() >= args.length)
				return null;
			return args[argIndex()];
		}

		private String sys() {
			if (StringUtil.empty(sysProp())) return null;
			return SystemVars.sys(sysProp());
		}

		private String env() {
			if (StringUtil.empty(envVar())) return null;
			return SystemVars.env(envVar());
		}
	}

	/**
	 * The value source.
	 */
	private class Source {
		public final Id id;
		public final String value;
		private final String desc;

		private Source(Id id, String value, String desc) {
			this.id = id;
			this.value = value;
			this.desc = desc;
		}

		public <E extends Exception, T> T parse(ExceptionFunction<E, Parser.String, T> parseFn)
			throws E {
			var result = parseFn.apply(parser());
			if (notifier != null) notifier.accept(desc(renderer.apply(result)));
			return result;
		}

		public Parser.String parser() {
			return Parser.string(value);
		}
		
		private String desc(String value) {
			var desc = String.format("%s = %s (%s)", BasicUtil.defaultValue(id.name(), "value"),
				value, this.desc);
			return id.argIndex == null ? desc : id.argIndex + ") " + desc;
		}
	}

	private StartupValues(String[] args) {
		this.args = args;
	}

	/**
	 * Set a notifier to receive value assignment messages.
	 */
	public StartupValues notifier(Consumer<String> notifier) {
		this.notifier = notifier;
		return this;
	}

	/**
	 * Specify the package prefix for system property names.
	 */
	public StartupValues prefix(Class<?> pkgClass) {
		return prefix(pkgClass.getPackageName());
	}

	/**
	 * Specify the package prefix for system property names.
	 */
	public StartupValues prefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * Specify the value renderer.
	 */
	public StartupValues renderer(Function<Object, String> renderer) {
		this.renderer = renderer;
		return this;
	}

	/**
	 * Skip the next argument.
	 */
	public StartupValues skip() {
		nextArg();
		return this;
	}

	/**
	 * Parse the next value without notifications, using args only. No notifications.
	 */
	public Parser.String next() {
		return next((String) null);
	}

	/**
	 * Parse the next value without notifications, from args, or name-based environment variable or
	 * system property.
	 */
	public Parser.String next(String name) {
		String sysProp = sysProp(name);
		return next(name, sysProp, envVarFrom(sysProp));
	}

	/**
	 * Parse the next value without notifications, from args, or explicitly named environment
	 * variable or system property.
	 */
	public Parser.String next(String name, String sysProp, String envVar) {
		return source(name, nextArg(), sysProp, envVar).parser();
	}

	/**
	 * Parse the next value, using args only.
	 */
	public <E extends Exception, T> T next(ExceptionFunction<E, Parser.String, T> fn) throws E {
		return next(null, fn);
	}

	/**
	 * Parse the next value, from args, or name-based environment variable or system property.
	 */
	public <E extends Exception, T> T next(String name, ExceptionFunction<E, Parser.String, T> fn)
		throws E {
		String sysProp = sysProp(name);
		return next(name, sysProp, envVarFrom(sysProp), fn);
	}

	/**
	 * Parse the next value, from args, or explicitly named environment variable or system property.
	 */
	public <E extends Exception, T> T next(String name, String sysProp, String envVar,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		return source(name, nextArg(), sysProp, envVar).parse(fn);
	}

	/**
	 * Parse the value from arg index.
	 */
	public Parser.String value(int index) {
		return value((String) null, index);
	}

	/**
	 * Parse the value from name-based environment variable or system property.
	 */
	public Parser.String value(String name) {
		String sysProp = sysProp(name);
		return source(name, null, sysProp, envVarFrom(sysProp)).parser();
	}

	/**
	 * Parse the value from arg index, or name-based environment variable or system property.
	 */
	public Parser.String value(String name, int index) {
		String sysProp = sysProp(name);
		return source(name, index, sysProp, envVarFrom(sysProp)).parser();
	}

	/**
	 * Parse the value from explicitly named environment variable or system property.
	 */
	public Parser.String value(String sysProp, String envVar) {
		return source(null, null, sysProp, envVar).parser();
	}

	/**
	 * Parse the value from arg index, or explicitly named environment variable or system property.
	 */
	public Parser.String value(int index, String sysProp, String envVar) {
		return source(null, index, sysProp, envVar).parser();
	}

	/**
	 * Parse the value from arg index.
	 */
	public <E extends Exception, T> T value(int index, ExceptionFunction<E, Parser.String, T> fn)
		throws E {
		return value(null, index, fn);
	}

	/**
	 * Parse the value from name-based environment variable or system property.
	 */
	public <E extends Exception, T> T value(String name, ExceptionFunction<E, Parser.String, T> fn)
		throws E {
		String sysProp = sysProp(name);
		return source(name, null, sysProp, envVarFrom(sysProp)).parse(fn);
	}

	/**
	 * Parse the value from arg index, or name-based environment variable or system property.
	 */
	public <E extends Exception, T> T value(String name, int index,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		String sysProp = sysProp(name);
		return source(name, index, sysProp, envVarFrom(sysProp)).parse(fn);
	}

	/**
	 * Parse the value from explicitly named environment variable or system property.
	 */
	public <E extends Exception, T> T value(String sysProp, String envVar,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		return source(null, null, sysProp, envVar).parse(fn);
	}

	/**
	 * Parse the value from arg index, or explicitly named environment variable or system property.
	 */
	public <E extends Exception, T> T value(int index, String sysProp, String envVar,
		ExceptionFunction<E, Parser.String, T> fn) throws E {
		return source(null, index, sysProp, envVar).parse(fn);
	}

	/**
	 * Determines the system property name.
	 */
	public String sysProp(String name) {
		return sysProp(prefix, name);
	}

	/**
	 * Determines the environment variable name.
	 */
	public String envVar(String name) {
		return envVarFrom(sysProp(name));
	}

	/**
	 * Provides the named source from arg index, or explicitly named environment variable or system
	 * property.
	 */
	private Source source(String name, Integer argIndex, String sysProp, String envVar) {
		var id = new Id(name, argIndex, sysProp, envVar);
		var value = arg(id, args);
		if (value == null) value = sysProp(id);
		if (value == null) value = envVar(id);
		if (value == null) value = defaultValue(id);
		return value;
	}

	private static String envVarFrom(String sysProp) {
		if (sysProp == null) return null;
		return sysProp.toUpperCase().replace('.', '_');
	}

	private static String sysProp(String prefix, String suffix) {
		suffix = StringUtil.trim(suffix);
		if (StringUtil.empty(suffix)) return null;
		return Separator.DOT.join(prefix, suffix);
	}

	private int nextArg() {
		return index++;
	}

	private Source source(Id id, String value, String format, Object... args) {
		return new Source(id, value, StringUtil.format(format, args));
	}

	private Source arg(Id id, String[] args) {
		var s = id.arg(args);
		if (s == null) return null;
		return source(id, s, "'%s' from args[%d]", s, id.argIndex());
	}

	private Source sysProp(Id id) {
		String s = id.sys();
		if (s == null) return null;
		return source(id, s, "'%s' from system %s", s, id.sysProp());
	}

	private Source envVar(Id id) {
		String s = id.env();
		if (s == null) return null;
		return source(id, s, "'%s' from env %s", s, id.envVar());
	}

	private Source defaultValue(Id id) {
		return source(id, null, "default");
	}
}
