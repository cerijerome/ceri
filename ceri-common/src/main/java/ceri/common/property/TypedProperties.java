package ceri.common.property;

import static ceri.common.collection.StreamUtil.toList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

/**
 * Class for accessing typed properties with a common key prefix. Useful when sharing one properties
 * source across multiple components.
 */
public class TypedProperties {
	/** A no-op, stateless instance. */
	public static final TypedProperties NULL =
		new TypedProperties(PropertyAccessor.NULL, PathFactory.dot) {};
	private static final String CHILD_KEY_PATTERN = "\\w+";
	private static final String CHILD_ID_PATTERN = "\\d+";
	public final String prefix;
	private final PathFactory paths;
	private final PropertyAccessor properties;

	public static class Ref extends ceri.common.util.Ref<TypedProperties> {
		protected Ref(TypedProperties ref, String... groups) {
			super(TypedProperties.from(ref, groups));
		}

		protected Parser.String parse(String... keyParts) {
			return ref.parse(keyParts);
		}
	}

	/**
	 * Creates typed properties with key prefix from given properties.
	 */
	public static TypedProperties from(Properties properties, String... prefix) {
		return of(PropertyAccessor.from(properties), PathFactory.dot, prefix);
	}

	/**
	 * Creates typed properties with key prefix from given resource bundle.
	 */
	public static TypedProperties from(ResourceBundle bundle, String... prefix) {
		return of(PropertyAccessor.from(bundle), PathFactory.dot, prefix);
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
	public static TypedProperties of(PropertyAccessor properties, PathFactory paths,
		String... prefix) {
		return new TypedProperties(properties, paths, prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	protected TypedProperties(PropertyAccessor properties, PathFactory paths, String... prefix) {
		this.paths = paths;
		this.prefix = paths.path(prefix).value;
		this.properties = properties;
	}

	/**
	 * Return typed properties with addition key prefix.
	 */
	public TypedProperties sub(String... prefix) {
		if (prefix.length == 0) return this;
		return new TypedProperties(properties, paths, paths.path(this.prefix, prefix).value);
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
	 * Retrieves a parser for the property with prefixed, dot-separated key, which may be null.
	 */
	public Parser.String parse(String... keyParts) {
		return Parser.String.of(get(keyParts));
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public String get(String... keyParts) {
		String value = StringUtil.trim(properties.property(key(keyParts)));
		return StringUtil.empty(value) ? null : value;
	}

	/**
	 * Returns all the integer ids that are children of the given key.
	 */
	public Set<Integer> childIds(String... keyParts) {
		String key = paths.path(keyParts).value;
		return childKeyStream(key, CHILD_ID_PATTERN).map(Integer::parseInt)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Returns all the children of the given key.
	 */
	public List<String> children(String... keyParts) {
		String key = paths.path(keyParts).value;
		return toList(childKeyStream(key, CHILD_KEY_PATTERN));
	}

	/**
	 * Returns true if child key exists.
	 */
	public boolean hasChild(String... keyParts) {
		String key = key(keyParts);
		if (properties.property(key) != null) return true;
		String prefix = key + paths.separator;
		return properties.keys().stream().anyMatch(k -> k.startsWith(prefix));
	}

	/**
	 * Returns all the descendants of the given key.
	 */
	public List<String> descendants(String... keyParts) {
		String key = paths.path(keyParts).value;
		return toList(childKeyStream(key, "[\\w" + paths.splitRegex + "]+"));
	}

	@Override
	public String toString() {
		return properties.toString();
	}

	/**
	 * Creates a prefixed, dot-separated immutable key from key parts. e.g. ab, cd, ef =>
	 * [prefix].ab.cd.ef
	 */
	protected String key(String... keyParts) {
		return paths.path(prefix, keyParts).value;
	}

	private Stream<String> childKeyStream(String key, String capturePattern) {
		String s = key(key);
		if (!s.isEmpty()) s += paths.separator;
		Pattern pattern = RegexUtil.compile("^\\Q%s\\E(%s)", s, capturePattern);
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

}
