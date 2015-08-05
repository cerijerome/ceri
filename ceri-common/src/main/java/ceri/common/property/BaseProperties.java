package ceri.common.property;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Abstract class for accessing properties with a common key prefix. Useful when sharing one
 * properties object across multiple components. Extend the class to expose specific field
 * accessors.
 */
public abstract class BaseProperties {
	private final String prefix;
	private final Properties properties;

	public static BaseProperties from(Properties properties) {
		return new BaseProperties(properties) {};
	}
	
	@Override
	public String toString() {
		return properties.toString();
	}

	/**
	 * Constructor for
	 */
	protected BaseProperties(BaseProperties properties, String... prefix) {
		this.prefix = Key.createWithPrefix(properties.prefix, prefix).value;
		this.properties = properties.properties;
	}

	protected BaseProperties(Properties properties, String... prefix) {
		this.prefix = Key.createWithPrefix(null, prefix).value;
		this.properties = properties;
	}

	/**
	 * Creates a prefixed, dot-separated immutable key from key parts. e.g. ab, cd, ef =>
	 * <prefix>.ab.cd.ef
	 */
	protected String key(String... keyParts) {
		return Key.createWithPrefix(prefix, keyParts).value;
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected String value(String... keyParts) {
		String value = properties.getProperty(key(keyParts));
		return BasicUtil.isEmpty(value) ? null : value.trim();
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected String stringValue(String def, String... keyParts) {
		String value = value(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves a collection of comma-separated Strings from prefixed, dot-separated key. Returns
	 * null if no values exist for the key.
	 */
	protected Collection<String> stringValues(String... keyParts) {
		String value = value(keyParts);
		if (value == null) return null;
		return StringUtil.commaSplit(value);
	}

	/**
	 * Retrieves a collection of comma-separated Strings from prefixed, dot-separated key. Returns
	 * default values if no values exist for the key.
	 */
	protected Collection<String> stringValues(Collection<String> def, String... keyParts) {
		String value = value(keyParts);
		Collection<String> values = StringUtil.commaSplit(value);
		return values.isEmpty() ? def : values;
	}

	/**
	 * Retrieves the Boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Boolean booleanValue(String... keyParts) {
		String value = value(keyParts);
		return value == null ? null : Boolean.valueOf(value);
	}

	/**
	 * Retrieves the boolean property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected boolean booleanValue(boolean def, String... keyParts) {
		String value = value(keyParts);
		return value == null ? def : Boolean.valueOf(value);
	}

	/**
	 * Retrieves the Character property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Character charValue(String... keyParts) {
		String value = value(keyParts);
		return value == null ? null : value.charAt(0);
	}

	/**
	 * Retrieves the char property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected char charValue(char def, String... keyParts) {
		String value = value(keyParts);
		return value == null ? def : value.charAt(0);
	}

	/**
	 * Retrieves the Byte property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected Byte byteValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Byte.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the byte property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected byte byteValue(byte def, String... keyParts) {
		Byte value = byteValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the Short property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Short shortValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Short.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the short property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected short shortValue(short def, String... keyParts) {
		Short value = shortValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the Integer property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Integer intValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the int property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected int intValue(int def, String... keyParts) {
		Integer value = intValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the Long property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected Long longValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Long.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the long property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected long longValue(long def, String... keyParts) {
		Long value = longValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the Float property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Float floatValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Float.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the float property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected float floatValue(float def, String... keyParts) {
		Float value = floatValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the Double property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Double doubleValue(String... keyParts) {
		String value = value(keyParts);
		try {
			return value == null ? null : Double.valueOf(value);
		} catch (NumberFormatException e) {
			throw BasicUtil.initCause(new NumberFormatException("Invalid format for " +
				key(keyParts) + ": " + value), e);
		}
	}

	/**
	 * Retrieves the double property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected double doubleValue(double def, String... keyParts) {
		Double value = doubleValue(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected File fileValue(String...keyParts) {
		String name = value(keyParts);
		if (name == null) return null;
		return new File(name);
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected File fileValue(File def, String...keyParts) {
		File file = fileValue(keyParts);
		return file == null ? def : file;
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
