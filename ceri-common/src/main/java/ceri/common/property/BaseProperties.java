package ceri.common.property;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * Abstract class for accessing properties with a common key prefix.
 * Useful when sharing one properties object across multiple components.
 * Extend the class to expose specific field accessors.
 */
public abstract class BaseProperties {
	private final String prefix;
	private final Properties properties;
	
	protected BaseProperties(Properties properties, String prefix) {
		this.prefix = prefix;
		this.properties = properties;
	}
	
	/**
	 * Creates a prefixed, dot-separated immutable key from key parts.
	 * e.g. ab, cd, ef => <prefix>.ab.cd.ef
	 */
	protected String key(String...keyParts) {
		return Key.createWithPrefix(prefix, keyParts).value;
	}
	
	/**
	 * Retrieves the String property from prefixed, dot-separated key.
	 * Returns null if no value exists for the key.
	 */
	protected String value(String...keyParts) {
		return properties.getProperty(key(keyParts));
	}
	
	/**
	 * Retrieves the Integer property from prefixed, dot-separated key.
	 * Returns null if no value exists for the key.
	 * Throws NumberFormatException if the value is not an integer.
	 */
	protected Integer intValue(String...keyParts) {
		String value = value(keyParts);
		if (value == null) return null;
		return Integer.valueOf(value);
	}
	
	/**
	 * Returns all the keys that start with prefix.
	 */
	protected Collection<String> keys() {
		Collection<String> keys = new LinkedHashSet<>();
		for (Object o : properties.keySet()) {
			String key = String.valueOf(o);
			if (prefix == null || prefix.isEmpty() || key.startsWith(prefix)) keys.add(key);
		}
		return keys;
	}
	
}
