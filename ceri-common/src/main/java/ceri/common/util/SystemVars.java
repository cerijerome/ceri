package ceri.common.util;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.text.Strings;

/**
 * A wrapper for environment variables and system properties, that allows overriding of values.
 */
public class SystemVars {
	private static final Map<String, Optional<String>> vars = new ConcurrentHashMap<>();

	private SystemVars() {}

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
		String orig = setProperty(name, value); // null if property not set
		return () -> setProperty(name, orig);
	}

	/**
	 * Sets an override value. A null value will hide an environment variable or system property.
	 */
	public static String set(String name, String value) {
		if (name == null) return null;
		Optional<String> old = vars.put(name, Optional.ofNullable(value));
		return old == null ? null : old.orElse(null);
	}

	/**
	 * Removes an override. Returns the current value.
	 */
	public static String remove(String name) {
		if (name == null) return null;
		Optional<String> old = vars.remove(name);
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

	private static Map<String, String> addVars(Map<String, String> map) {
		vars.forEach((k, v) -> {
			if (v.isEmpty()) map.remove(k);
			else map.put(k, v.get());
		});
		return map;
	}

}
