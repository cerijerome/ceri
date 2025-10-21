package ceri.common.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import ceri.common.collect.Maps;
import ceri.common.function.Functional;
import ceri.common.function.Functions;
import ceri.common.property.Parser;
import ceri.common.stream.Streams;
import ceri.common.text.Strings;

/**
 * Provides access to environment variables and system properties, with support for overrides.
 */
public class SystemVars {
	private static final Pattern PATH_SEPARATOR_REGEX =
		Pattern.compile("\\s*\\Q" + File.pathSeparator + "\\E\\s*");
	private static final String TMP_DIR_PROPERTY = "java.io.tmpdir";
	private static final String USER_HOME_PROPERTY = "user.home";
	private static final String USER_DIR_PROPERTY = "user.dir";
	private static final Map<String, Optional<String>> vars = new ConcurrentHashMap<>();

	private SystemVars() {}

	/**
	 * Returns the system temp directory.
	 */
	public static Path tempDir() {
		return sysPath(TMP_DIR_PROPERTY);
	}

	/**
	 * Returns the user home path extended with given paths, based on system property 'user.home'.
	 * Returns null if property does not exist.
	 */
	public static Path userHome(String... paths) {
		return sysPath(USER_HOME_PROPERTY, paths);
	}

	/**
	 * Returns the current path extended with given paths, based on system property 'user.dir'.
	 * Returns null if property does not exist.
	 */
	public static Path userDir(String... paths) {
		return sysPath(USER_DIR_PROPERTY, paths);
	}

	/**
	 * Join paths using the system path separator.
	 */
	public static String pathVar(String... paths) {
		return Streams.of(paths).map(String::trim).filter(Strings.Filter.NON_EMPTY)
			.collect(Collectors.joining(File.pathSeparator));
	}

	/**
	 * Extract unique paths in order, using the system path separator.
	 */
	public static Set<String> varPaths(String variable) {
		if (variable == null) return Set.of();
		return Parser.string(variable).split(PATH_SEPARATOR_REGEX).filter(Strings::nonEmpty)
			.toSet();
	}

	/**
	 * Gets an override value or environment variable if not set.
	 */
	public static String env(String name) {
		return env(name, null);
	}

	/**
	 * Gets an override value or environment variable if not set.
	 */
	public static String env(String name, String def) {
		var optional = vars.get(name);
		if (optional != null) return optional.orElse(def);
		String value = System.getenv(name);
		return value == null ? def : value;
	}

	/**
	 * Gets all environment variables including override values.
	 */
	public static Map<String, String> env() {
		return addVars(Maps.copy(System.getenv()));
	}

	/**
	 * Returns the path extending from a given environment variable. Returns null if the variable
	 * does not exist.
	 */
	public static Path envPath(String name, String... paths) {
		return Functional.apply(p -> Path.of(p, paths), SystemVars.env(name));
	}

	/**
	 * Gets an override value or system property if not set.
	 */
	public static String sys(String name) {
		return sys(name, null);
	}

	/**
	 * Gets an override value or system property if not set.
	 */
	public static String sys(String name, String def) {
		var optional = vars.get(name);
		if (optional != null) return optional.orElse(def);
		String value = System.getProperty(name);
		return value == null ? def : value;
	}

	/**
	 * Gets all system properties including override values.
	 */
	public static Map<String, String> sys() {
		var sys = Maps.<String, String>of();
		System.getProperties().forEach((k, v) -> sys.put((String) k, (String) v));
		return addVars(sys);
	}

	/**
	 * Returns the path extending from a given system property path. Returns null if the system
	 * property does not exist.
	 */
	public static Path sysPath(String name, String... paths) {
		return Functional.apply(p -> Path.of(p, paths), SystemVars.sys(name));
	}

	/**
	 * Sets a system property and returns the previous value, or null if not set. A null value
	 * passed in will clear the property.
	 */
	public static String setProperty(String name, String value) {
		if (Strings.isBlank(name)) return null;
		return value != null ? System.setProperty(name, value) : System.clearProperty(name);
	}

	/**
	 * Returns a closeable instance that sets a system property, then reverts it on close.
	 */
	public static Functions.Closeable removableProperty(String name, String value) {
		var orig = setProperty(name, value); // null if property not set
		return () -> setProperty(name, orig);
	}

	/**
	 * Sets an override value. A null value will hide an environment variable or system property.
	 */
	public static String set(String name, String value) {
		if (name == null) return null;
		var old = vars.put(name, Optional.ofNullable(value));
		return old == null ? null : old.orElse(null);
	}

	/**
	 * Removes an override. Returns the current value.
	 */
	public static String remove(String name) {
		if (name == null) return null;
		var old = vars.remove(name);
		return old == null ? null : old.orElse(null);
	}

	/**
	 * Removes all overrides.
	 */
	public static void clear() {
		vars.clear();
	}

	/**
	 * Returns a closeable instance that sets an override, then removes it on close.
	 */
	public static Functions.Closeable removable(String name, String value) {
		set(name, value);
		return () -> remove(name);
	}

	// support

	private static Map<String, String> addVars(Map<String, String> map) {
		vars.forEach((k, v) -> {
			if (v.isEmpty()) map.remove(k);
			else map.put(k, v.get());
		});
		return map;
	}
}
