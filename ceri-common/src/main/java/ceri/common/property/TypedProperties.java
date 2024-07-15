package ceri.common.property;

import static ceri.common.collection.StreamUtil.toList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.BooleanFunction;
import ceri.common.text.StringUtil;

/**
 * Class for accessing typed properties with a common key prefix. Useful when sharing one properties
 * source across multiple components.
 */
public class TypedProperties {
	/** A no-op, stateless instance. */
	public static final TypedProperties NULL = new TypedProperties(PropertyAccessor.NULL) {};
	private static final String CHILD_KEY_PATTERN = "\\w+";
	private static final String DESCENDENT_KEY_PATTERN = "[\\w\\.]+";
	private static final String CHILD_ID_PATTERN = "\\d+";
	public final String prefix;
	private final PropertyAccessor properties;

	/**
	 * Creates typed properties with key prefix from given properties.
	 */
	public static TypedProperties from(Properties properties, String... prefix) {
		return of(PropertyAccessor.from(properties), prefix);
	}

	/**
	 * Creates typed properties with key prefix from given resource bundle.
	 */
	public static TypedProperties from(ResourceBundle bundle, String... prefix) {
		return of(PropertyAccessor.from(bundle), prefix);
	}

	/**
	 * Creates from typed properties with additional key path.
	 */
	public static TypedProperties from(TypedProperties properties, String... prefix) {
		return properties == null ? from(prefix) : properties.sub(prefix);
	}

	/**
	 * Creates empty typed properties with key path.
	 */
	public static TypedProperties from(String... prefix) {
		return from(new Properties(), prefix);
	}

	/**
	 * Merges properties to one with no prefix.
	 */
	public static TypedProperties merge(TypedProperties... properties) {
		return merge(Arrays.asList(properties));
	}

	/**
	 * Merges properties with new absolute key prefix.
	 */
	public static TypedProperties merge(Collection<TypedProperties> collection, String... prefix) {
		Properties properties = new Properties();
		collection.forEach(bp -> bp.properties.forEach(properties::put));
		return from(properties, prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	public static TypedProperties of(PropertyAccessor properties, String... prefix) {
		return new TypedProperties(properties, prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	protected TypedProperties(PropertyAccessor properties, String... prefix) {
		this.prefix = PathFactory.dot.path(prefix).value;
		this.properties = properties;
	}

	/**
	 * Return typed properties with addition key prefix.
	 */
	public TypedProperties sub(String... prefix) {
		if (prefix.length == 0) return this;
		return new TypedProperties(properties, PathFactory.dot.path(this.prefix, prefix).value);
	}

	/**
	 * Returns the full keys that start with prefix.
	 */
	public List<String> keys() {
		return toList(properties.keys().stream().filter(this::hasPrefix).distinct());
	}

	/**
	 * Set the key value, or remove it if the given value is null. Throws
	 * UnsupportedOperationException for read-only properties.
	 */
	public void setValue(Object value, String... keyParts) {
		properties.property(key(keyParts), value == null ? null : value.toString());
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public String value(String... keyParts) {
		String value = properties.property(key(keyParts));
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> T value(Function<String, T> constructor, String... keyParts) {
		return value(null, constructor, keyParts);
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T value(T def, Function<String, T> constructor, String... keyParts) {
		return value(null, def, constructor, keyParts);
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T value(Class<T> cls, T def, Function<String, T> constructor, String... keyParts) {
		return parseValue(cls, def, value(keyParts), constructor, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> T valueFromBoolean(BooleanFunction<T> constructor, String... keyParts) {
		return valueFromBoolean(null, constructor, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> T valueFromBoolean(T trueVal, T falseVal, String... keyParts) {
		return valueFromBoolean(b -> b ? trueVal : falseVal, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T valueFromBoolean(T def, BooleanFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Boolean.parseBoolean(s)), keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T valueFromBoolean(T def, T trueVal, T falseVal, String... keyParts) {
		return valueFromBoolean(def, b -> b ? trueVal : falseVal, keyParts);
	}

	/**
	 * Converts the integer property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> T valueFromInt(IntFunction<T> constructor, String... keyParts) {
		return valueFromInt(null, constructor, keyParts);
	}

	/**
	 * Converts the integer property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T valueFromInt(T def, IntFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Integer.decode(s)), keyParts);
	}

	/**
	 * Converts the long property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	public <T> T valueFromLong(LongFunction<T> constructor, String... keyParts) {
		return valueFromLong(null, constructor, keyParts);
	}

	/**
	 * Converts the long property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T valueFromLong(T def, LongFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Long.decode(s)), keyParts);
	}

	/**
	 * Converts the double property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> T valueFromDouble(DoubleFunction<T> constructor, String... keyParts) {
		return valueFromDouble(null, constructor, keyParts);
	}

	/**
	 * Converts the double property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> T valueFromDouble(T def, DoubleFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Double.parseDouble(s)), keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * null if no values exist for the key. The constructor converts from each string to the desired
	 * type.
	 */
	public <T> List<T> values(Function<String, T> constructor, String... keyParts) {
		return parseValues(null, value -> parseValue(null, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * default values if no values exist for the key. The constructor converts from each string to
	 * the desired type.
	 */
	public <T> List<T> values(List<T> def, Function<String, T> constructor, String... keyParts) {
		return parseValues(def, value -> parseValue(null, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * default values if no values exist for the key. The constructor converts from each string to
	 * the desired type.
	 */
	public <T> List<T> values(Class<T> cls, List<T> def, Function<String, T> constructor,
		String... keyParts) {
		return parseValues(def, value -> parseValue(cls, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Converts the boolean list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> List<T> valuesFromBoolean(BooleanFunction<T> constructor, String... keyParts) {
		return valuesFromBoolean(null, constructor, keyParts);
	}

	/**
	 * Converts the boolean list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	public <T> List<T> valuesFromBoolean(List<T> def, BooleanFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Boolean.parseBoolean(s)), keyParts);
	}

	/**
	 * Converts the integer list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> List<T> valuesFromInt(IntFunction<T> constructor, String... keyParts) {
		return valuesFromInt(null, constructor, keyParts);
	}

	/**
	 * Converts the integer list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	public <T> List<T> valuesFromInt(List<T> def, IntFunction<T> constructor, String... keyParts) {
		return values(def, s -> constructor.apply(Integer.decode(s)), keyParts);
	}

	/**
	 * Converts the long list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> List<T> valuesFromLong(LongFunction<T> constructor, String... keyParts) {
		return valuesFromLong(null, constructor, keyParts);
	}

	/**
	 * Converts the long list property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	public <T> List<T> valuesFromLong(List<T> def, LongFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Long.decode(s)), keyParts);
	}

	/**
	 * Converts the double list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public <T> List<T> valuesFromDouble(DoubleFunction<T> constructor, String... keyParts) {
		return valuesFromDouble(null, constructor, keyParts);
	}

	/**
	 * Converts the double list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	public <T> List<T> valuesFromDouble(List<T> def, DoubleFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Double.parseDouble(s)), keyParts);
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public String stringValue(String def, String... keyParts) {
		String value = value(keyParts);
		return value == null ? def : value;
	}

	/**
	 * Retrieves a collection of comma-separated Strings from prefixed, dot-separated key. Returns
	 * null if the key does not exist.
	 */
	public List<String> stringValues(String... keyParts) {
		return stringValues(null, keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated Strings from prefixed, dot-separated key. Returns
	 * default values if the key doesn not exist.
	 */
	public List<String> stringValues(List<String> def, String... keyParts) {
		String value = value(keyParts);
		if (value == null) return def;
		List<String> values = StringUtil.commaSplit(value);
		return values.isEmpty() ? Collections.emptyList() : values;
	}

	/**
	 * Retrieves enum type from prefixed, dot-separated key. Returns null if no value exists for the
	 * key. Throws IllegalArgumentException if the type cannot be evaluated.
	 */
	public <T extends Enum<T>> T enumValue(Class<T> cls, String... keyParts) {
		return enumValue(cls, null, keyParts);
	}

	/**
	 * Retrieves enum type from prefixed, dot-separated key. Returns the given default value if no
	 * value exists for the key. Throws IllegalArgumentException if the type cannot be evaluated.
	 */
	public <T extends Enum<T>> T enumValue(Class<T> cls, T def, String... keyParts) {
		return value(cls, def, value -> Enum.valueOf(cls, value), keyParts);
	}

	/**
	 * Retrieves list of enum types from prefixed, dot-separated key. Returns the given default
	 * values if no value exists for the key. Throws IllegalArgumentException if the type cannot be
	 * evaluated.
	 */
	public <T extends Enum<T>> List<T> enumValues(Class<T> cls, String... keyParts) {
		return enumValues(cls, null, keyParts);
	}

	/**
	 * Retrieves list of enum types from prefixed, dot-separated key. Returns the given default
	 * values if no value exists for the key. Throws IllegalArgumentException if the type cannot be
	 * evaluated.
	 */
	public <T extends Enum<T>> List<T> enumValues(Class<T> cls, List<T> def, String... keyParts) {
		return values(cls, def, value -> Enum.valueOf(cls, value), keyParts);
	}

	/**
	 * Retrieves the Boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Boolean booleanValue(String... keyParts) {
		return value(Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the boolean property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public boolean booleanValue(boolean def, String... keyParts) {
		return value(def, Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Boolean> booleanValues(String... keyParts) {
		return booleanValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Boolean> booleanValues(List<Boolean> def, String... keyParts) {
		return values(def, Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the Character property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Character charValue(String... keyParts) {
		return value(value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the char property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public char charValue(char def, String... keyParts) {
		return value(def, value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Character> charValues(String... keyParts) {
		return charValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Character> charValues(List<Character> def, String... keyParts) {
		return values(def, value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the Byte property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	public Byte byteValue(String... keyParts) {
		return value(Byte::decode, keyParts);
	}

	/**
	 * Retrieves the byte property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public byte byteValue(byte def, String... keyParts) {
		return value(def, Byte::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Byte> byteValues(String... keyParts) {
		return byteValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Byte> byteValues(List<Byte> def, String... keyParts) {
		return values(def, Byte::decode, keyParts);
	}

	/**
	 * Retrieves the Short property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Short shortValue(String... keyParts) {
		return value(Short::decode, keyParts);
	}

	/**
	 * Retrieves the short property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public short shortValue(short def, String... keyParts) {
		return value(def, Short::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Short> shortValues(String... keyParts) {
		return shortValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Short> shortValues(List<Short> def, String... keyParts) {
		return values(def, Short::decode, keyParts);
	}

	/**
	 * Retrieves the Integer property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Integer intValue(String... keyParts) {
		return value(Integer::decode, keyParts);
	}

	/**
	 * Retrieves the int property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public int intValue(int def, String... keyParts) {
		return value(def, Integer::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Integer> intValues(String... keyParts) {
		return intValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Integer> intValues(List<Integer> def, String... keyParts) {
		return values(def, Integer::decode, keyParts);
	}

	/**
	 * Retrieves the Long property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	public Long longValue(String... keyParts) {
		return value(Long::decode, keyParts);
	}

	/**
	 * Retrieves the long property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public long longValue(long def, String... keyParts) {
		return value(def, Long::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Long> longValues(String... keyParts) {
		return longValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Long> longValues(List<Long> def, String... keyParts) {
		return values(def, Long::decode, keyParts);
	}

	/**
	 * Retrieves the Float property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Float floatValue(String... keyParts) {
		return value(Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the float property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public float floatValue(float def, String... keyParts) {
		return value(def, Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Float> floatValues(String... keyParts) {
		return floatValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Float> floatValues(List<Float> def, String... keyParts) {
		return values(def, Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the Double property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public Double doubleValue(String... keyParts) {
		return value(Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the double property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public double doubleValue(double def, String... keyParts) {
		return value(def, Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Double> doubleValues(String... keyParts) {
		return doubleValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	public List<Double> doubleValues(List<Double> def, String... keyParts) {
		return values(def, Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	public java.nio.file.Path pathValue(String... keyParts) {
		return pathValue(null, keyParts);
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	public java.nio.file.Path pathValue(java.nio.file.Path def, String... keyParts) {
		return value(def, java.nio.file.Path::of, keyParts);
	}

	/**
	 * Returns all the integer ids that are children of the given key.
	 */
	public Set<Integer> childIds(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return childKeyStream(key, CHILD_ID_PATTERN).map(Integer::parseInt)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Returns all the children of the given key.
	 */
	public List<String> children(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return toList(childKeyStream(key, CHILD_KEY_PATTERN));
	}

	/**
	 * Returns true if child key exists.
	 */
	public boolean hasChild(String... keyParts) {
		String key = key(keyParts);
		if (properties.property(key) != null) return true;
		String prefix = key + ".";
		return properties.keys().stream().anyMatch(k -> k.startsWith(prefix));
	}

	/**
	 * Returns all the descendants of the given key.
	 */
	public List<String> descendants(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return toList(childKeyStream(key, DESCENDENT_KEY_PATTERN));
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	/**
	 * Creates a prefixed, dot-separated immutable key from key parts. e.g. ab, cd, ef =>
	 * <prefix>.ab.cd.ef
	 */
	protected String key(String... keyParts) {
		return PathFactory.dot.path(prefix, keyParts).value;
	}

	private Stream<String> childKeyStream(String key, String capturePattern) {
		String s = key(key);
		if (!s.isEmpty()) s += ".";
		Pattern pattern = Pattern.compile(String.format("^\\Q%s\\E(%s)", s, capturePattern));
		return properties.keys().stream().map(k -> keyMatch(k, pattern)).filter(Objects::nonNull)
			.distinct();
	}

	private String keyMatch(String key, Pattern pattern) {
		Matcher m = pattern.matcher(key);
		if (!m.find()) return null;
		return m.group(1);
	}

	private boolean hasPrefix(String key) {
		return key.startsWith(prefix);
	}

	private <T> T parseValue(Class<T> cls, T def, String value, Function<String, T> constructor,
		String... keyParts) {
		try {
			return value == null ? def : constructor.apply(value);
		} catch (RuntimeException e) {
			String typeName = cls == null ? "format" : cls.getSimpleName();
			throw ExceptionUtil.initCause(new NumberFormatException(
				"Invalid " + typeName + " for " + key(keyParts) + ": " + value), e);
		}
	}

	private <T> List<T> parseValues(List<T> def, Function<String, T> constructor,
		String... keyParts) {
		List<String> stringValues = stringValues(keyParts);
		if (stringValues == null) return def;
		return toList(stringValues.stream().map(constructor));
	}
}
