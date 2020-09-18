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
 * Abstract class for accessing properties with a common key prefix. Useful when sharing one
 * properties object across multiple components. Extend the class to expose specific field
 * accessors.
 */
public abstract class BaseProperties {
	private static final String CHILD_KEY_PATTERN = "\\w+";
	private static final String DESCENDENT_KEY_PATTERN = "[\\w\\.]+";
	private static final String CHILD_ID_PATTERN = "\\d+";
	protected final String prefix;
	private final PropertyAccessor properties;

	/**
	 * Creates base properties with no prefix from given properties.
	 */
	public static BaseProperties from(Properties properties) {
		return new BaseProperties(properties) {};
	}

	/**
	 * Creates base properties with no prefix from given resource bundle.
	 */
	public static BaseProperties from(ResourceBundle bundle) {
		return new BaseProperties(bundle) {};
	}

	/**
	 * Merges base properties to one with no prefix.
	 */
	public static BaseProperties merge(BaseProperties... properties) {
		return merge(Arrays.asList(properties));
	}

	/**
	 * Merges base properties to one with no prefix.
	 */
	public static BaseProperties merge(Collection<BaseProperties> collection) {
		Properties properties = new Properties();
		collection.forEach(bp -> properties.putAll(bp.properties.properties()));
		return from(properties);
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	protected BaseProperties(BaseProperties properties, String... prefix) {
		if (properties == null) properties = from(new Properties());
		this.prefix = PathFactory.dot.path(properties.prefix, prefix).value;
		this.properties = properties.properties;
	}

	/**
	 * Constructor for properties file with given prefix keys.
	 */
	protected BaseProperties(Properties properties, String... prefix) {
		this(PropertyAccessor.from(properties), prefix);
	}

	/**
	 * Constructor for resource bundle with given prefix keys.
	 */
	protected BaseProperties(ResourceBundle bundle, String... prefix) {
		this(PropertyAccessor.from(bundle), prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	protected BaseProperties(PropertyAccessor properties, String... prefix) {
		this.prefix = PathFactory.dot.path(prefix).value;
		this.properties = properties;
	}

	/**
	 * Creates a prefixed, dot-separated immutable key from key parts. e.g. ab, cd, ef =>
	 * <prefix>.ab.cd.ef
	 */
	protected String key(String... keyParts) {
		return PathFactory.dot.path(prefix, keyParts).value;
	}

	/**
	 * Returns the full keys that start with prefix.
	 */
	protected List<String> keys() {
		return toList(properties.keys().stream().filter(this::hasPrefix).distinct());
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected String value(String... keyParts) {
		String value = properties.property(key(keyParts));
		if (value == null) return null;
		value = value.trim();
		return value.isEmpty() ? null : value;
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> T value(Function<String, T> constructor, String... keyParts) {
		return value(null, constructor, keyParts);
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T value(T def, Function<String, T> constructor, String... keyParts) {
		return value(null, def, constructor, keyParts);
	}

	/**
	 * Retrieves the typed property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T value(Class<T> cls, T def, Function<String, T> constructor,
		String... keyParts) {
		return parseValue(cls, def, value(keyParts), constructor, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> T valueFromBoolean(BooleanFunction<T> constructor, String... keyParts) {
		return valueFromBoolean(null, constructor, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> T valueFromBoolean(T trueVal, T falseVal, String... keyParts) {
		return valueFromBoolean(b -> b ? trueVal : falseVal, keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T valueFromBoolean(T def, BooleanFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Boolean.parseBoolean(s)), keyParts);
	}

	/**
	 * Converts the boolean property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T valueFromBoolean(T def, T trueVal, T falseVal, String... keyParts) {
		return valueFromBoolean(def, b -> b ? trueVal : falseVal, keyParts);
	}

	/**
	 * Converts the integer property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> T valueFromInt(IntFunction<T> constructor, String... keyParts) {
		return valueFromInt(null, constructor, keyParts);
	}

	/**
	 * Converts the integer property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T valueFromInt(T def, IntFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Integer.decode(s)), keyParts);
	}

	/**
	 * Converts the long property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected <T> T valueFromLong(LongFunction<T> constructor, String... keyParts) {
		return valueFromLong(null, constructor, keyParts);
	}

	/**
	 * Converts the long property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T valueFromLong(T def, LongFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Long.decode(s)), keyParts);
	}

	/**
	 * Converts the double property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> T valueFromDouble(DoubleFunction<T> constructor, String... keyParts) {
		return valueFromDouble(null, constructor, keyParts);
	}

	/**
	 * Converts the double property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> T valueFromDouble(T def, DoubleFunction<T> constructor, String... keyParts) {
		return value(def, s -> constructor.apply(Double.parseDouble(s)), keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * null if no values exist for the key. The constructor converts from each string to the desired
	 * type.
	 */
	protected <T> List<T> values(Function<String, T> constructor, String... keyParts) {
		return parseValues(null, value -> parseValue(null, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * default values if no values exist for the key. The constructor converts from each string to
	 * the desired type.
	 */
	protected <T> List<T> values(List<T> def, Function<String, T> constructor, String... keyParts) {
		return parseValues(def, value -> parseValue(null, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated values from prefixed, dot-separated key. Returns
	 * default values if no values exist for the key. The constructor converts from each string to
	 * the desired type.
	 */
	protected <T> List<T> values(Class<T> cls, List<T> def, Function<String, T> constructor,
		String... keyParts) {
		return parseValues(def, value -> parseValue(cls, null, value, constructor, keyParts),
			keyParts);
	}

	/**
	 * Converts the boolean list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> List<T> valuesFromBoolean(BooleanFunction<T> constructor, String... keyParts) {
		return valuesFromBoolean(null, constructor, keyParts);
	}

	/**
	 * Converts the boolean list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	protected <T> List<T> valuesFromBoolean(List<T> def, BooleanFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Boolean.parseBoolean(s)), keyParts);
	}

	/**
	 * Converts the integer list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> List<T> valuesFromInt(IntFunction<T> constructor, String... keyParts) {
		return valuesFromInt(null, constructor, keyParts);
	}

	/**
	 * Converts the integer list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	protected <T> List<T> valuesFromInt(List<T> def, IntFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Integer.decode(s)), keyParts);
	}

	/**
	 * Converts the long list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> List<T> valuesFromLong(LongFunction<T> constructor, String... keyParts) {
		return valuesFromLong(null, constructor, keyParts);
	}

	/**
	 * Converts the long list property from prefixed, dot-separated key. Returns default if no value
	 * exists for the key.
	 */
	protected <T> List<T> valuesFromLong(List<T> def, LongFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Long.decode(s)), keyParts);
	}

	/**
	 * Converts the double list property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected <T> List<T> valuesFromDouble(DoubleFunction<T> constructor, String... keyParts) {
		return valuesFromDouble(null, constructor, keyParts);
	}

	/**
	 * Converts the double list property from prefixed, dot-separated key. Returns default if no
	 * value exists for the key.
	 */
	protected <T> List<T> valuesFromDouble(List<T> def, DoubleFunction<T> constructor,
		String... keyParts) {
		return values(def, s -> constructor.apply(Double.parseDouble(s)), keyParts);
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
	 * null if the key does not exist.
	 */
	protected List<String> stringValues(String... keyParts) {
		return stringValues(null, keyParts);
	}

	/**
	 * Retrieves a collection of comma-separated Strings from prefixed, dot-separated key. Returns
	 * default values if the key doesn not exist.
	 */
	protected List<String> stringValues(List<String> def, String... keyParts) {
		String value = value(keyParts);
		if (value == null) return def;
		List<String> values = StringUtil.commaSplit(value);
		return values.isEmpty() ? Collections.emptyList() : values;
	}

	/**
	 * Retrieves enum type from prefixed, dot-separated key. Returns null if no value exists for the
	 * key. Throws IllegalArgumentException if the type cannot be evaluated.
	 */
	protected <T extends Enum<T>> T enumValue(Class<T> cls, String... keyParts) {
		return enumValue(cls, null, keyParts);
	}

	/**
	 * Retrieves enum type from prefixed, dot-separated key. Returns the given default value if no
	 * value exists for the key. Throws IllegalArgumentException if the type cannot be evaluated.
	 */
	protected <T extends Enum<T>> T enumValue(Class<T> cls, T def, String... keyParts) {
		return value(cls, def, value -> Enum.valueOf(cls, value), keyParts);
	}

	/**
	 * Retrieves list of enum types from prefixed, dot-separated key. Returns the given default
	 * values if no value exists for the key. Throws IllegalArgumentException if the type cannot be
	 * evaluated.
	 */
	protected <T extends Enum<T>> List<T> enumValues(Class<T> cls, String... keyParts) {
		return enumValues(cls, null, keyParts);
	}

	/**
	 * Retrieves list of enum types from prefixed, dot-separated key. Returns the given default
	 * values if no value exists for the key. Throws IllegalArgumentException if the type cannot be
	 * evaluated.
	 */
	protected <T extends Enum<T>> List<T> enumValues(Class<T> cls, List<T> def,
		String... keyParts) {
		return values(cls, def, value -> Enum.valueOf(cls, value), keyParts);
	}

	/**
	 * Retrieves the Boolean property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Boolean booleanValue(String... keyParts) {
		return value(Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the boolean property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected boolean booleanValue(boolean def, String... keyParts) {
		return value(def, Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Boolean> booleanValues(String... keyParts) {
		return booleanValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Boolean> booleanValues(List<Boolean> def, String... keyParts) {
		return values(def, Boolean::valueOf, keyParts);
	}

	/**
	 * Retrieves the Character property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Character charValue(String... keyParts) {
		return value(value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the char property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected char charValue(char def, String... keyParts) {
		return value(def, value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Character> charValues(String... keyParts) {
		return charValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Character> charValues(List<Character> def, String... keyParts) {
		return values(def, value -> value.charAt(0), keyParts);
	}

	/**
	 * Retrieves the Byte property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected Byte byteValue(String... keyParts) {
		return value(Byte::decode, keyParts);
	}

	/**
	 * Retrieves the byte property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected byte byteValue(byte def, String... keyParts) {
		return value(def, Byte::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Byte> byteValues(String... keyParts) {
		return byteValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Byte> byteValues(List<Byte> def, String... keyParts) {
		return values(def, Byte::decode, keyParts);
	}

	/**
	 * Retrieves the Short property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Short shortValue(String... keyParts) {
		return value(Short::decode, keyParts);
	}

	/**
	 * Retrieves the short property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected short shortValue(short def, String... keyParts) {
		return value(def, Short::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Short> shortValues(String... keyParts) {
		return shortValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Short> shortValues(List<Short> def, String... keyParts) {
		return values(def, Short::decode, keyParts);
	}

	/**
	 * Retrieves the Integer property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Integer intValue(String... keyParts) {
		return value(Integer::decode, keyParts);
	}

	/**
	 * Retrieves the int property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected int intValue(int def, String... keyParts) {
		return value(def, Integer::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Integer> intValues(String... keyParts) {
		return intValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Integer> intValues(List<Integer> def, String... keyParts) {
		return values(def, Integer::decode, keyParts);
	}

	/**
	 * Retrieves the Long property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected Long longValue(String... keyParts) {
		return value(Long::decode, keyParts);
	}

	/**
	 * Retrieves the long property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected long longValue(long def, String... keyParts) {
		return value(def, Long::decode, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Long> longValues(String... keyParts) {
		return longValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Long> longValues(List<Long> def, String... keyParts) {
		return values(def, Long::decode, keyParts);
	}

	/**
	 * Retrieves the Float property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Float floatValue(String... keyParts) {
		return value(Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the float property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected float floatValue(float def, String... keyParts) {
		return value(def, Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Float> floatValues(String... keyParts) {
		return floatValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Float> floatValues(List<Float> def, String... keyParts) {
		return values(def, Float::valueOf, keyParts);
	}

	/**
	 * Retrieves the Double property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	protected Double doubleValue(String... keyParts) {
		return value(Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the double property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected double doubleValue(double def, String... keyParts) {
		return value(def, Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Double> doubleValues(String... keyParts) {
		return doubleValues(null, keyParts);
	}

	/**
	 * Retrieves the list of property from prefixed, dot-separated key.
	 */
	protected List<Double> doubleValues(List<Double> def, String... keyParts) {
		return values(def, Double::valueOf, keyParts);
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns null if no value exists
	 * for the key.
	 */
	protected java.nio.file.Path pathValue(String... keyParts) {
		return pathValue(null, keyParts);
	}

	/**
	 * Retrieves the File property from prefixed, dot-separated key. Returns default value if no
	 * value exists for the key.
	 */
	protected java.nio.file.Path pathValue(java.nio.file.Path def, String... keyParts) {
		return value(def, java.nio.file.Path::of, keyParts);
	}

	/**
	 * Returns all the integer ids that are children of the given key.
	 */
	protected Set<Integer> childIds(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return childKeyStream(key, CHILD_ID_PATTERN).map(Integer::parseInt)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Returns all the children of the given key.
	 */
	protected List<String> children(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return toList(childKeyStream(key, CHILD_KEY_PATTERN));
	}

	/**
	 * Returns true if child key exists.
	 */
	protected boolean hasChild(String... keyParts) {
		String key = key(keyParts);
		if (properties.property(key) != null) return true;
		String prefix = key + ".";
		return properties.keys().stream().anyMatch(k -> k.startsWith(prefix));
	}

	/**
	 * Returns all the descendants of the given key.
	 */
	protected List<String> descendants(String... keyParts) {
		String key = PathFactory.dot.path(keyParts).value;
		return toList(childKeyStream(key, DESCENDENT_KEY_PATTERN));
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
