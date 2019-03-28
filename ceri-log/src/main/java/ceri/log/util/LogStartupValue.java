package ceri.log.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;
import ceri.common.util.StartupValue;

/**
 * A startup value that may be specified by main argument, system property, or environment variable.
 */
public class LogStartupValue {
	private static final Logger logger = LogManager.getLogger();
	private final StartupValue value;
	private final String name;

	public static LogStartupValue of(StartupValue value) {
		return of(value, value.sysPropertyName);
	}

	public static LogStartupValue of(StartupValue value, String name) {
		return new LogStartupValue(value, name);
	}

	private LogStartupValue(StartupValue value, String name) {
		this.value = value;
		this.name = name == null ? "Value" : name;
	}

	public <E extends Exception, T> T apply(String[] args, ExceptionFunction<E, String, T> fn)
		throws E {
		return apply(args, fn, null);
	}

	public <E extends Exception, T> T apply(String[] args, ExceptionFunction<E, String, T> fn,
		T def) throws E {
		return applyFrom(args, fn, () -> def);
	}

	public <E extends Exception, T> T applyFrom(String[] args, ExceptionFunction<E, String, T> fn,
		ExceptionSupplier<E, T> def) throws E {
		String value = value(args);
		if (value != null && fn != null) return fn.apply(value);
		if (def == null) return null;
		logger.info("{}: applying default supplier", name);
		return def.get();
	}

	public String value(String... args) {
		return value(args, null);
	}

	public String value(String[] args, String defaultValue) {
		String s = arg(args);
		if (s == null) s = sysProperty();
		if (s == null) s = envVariable();
		if (s == null) s = defaultValue(defaultValue);
		return s;
	}

	private String arg(String[] args) {
		String s = value.arg(args);
		if (s != null) logger.info("{} = {} (from args[{}])", name, s, value.argIndex);
		return s;
	}

	private String sysProperty() {
		String s = value.sysProperty();
		if (s != null) logger.info("{} = {} (from system '{}')", name, s, value.sysPropertyName);
		return s;
	}

	private String envVariable() {
		String s = value.envVariable();
		if (s != null) logger.info("{} = {} (from env '{}')", name, s, value.envVariableName);
		return s;
	}

	private String defaultValue(String s) {
		if (s != null) logger.info("{} = {} (default value)", name, s);
		return s;
	}

}
