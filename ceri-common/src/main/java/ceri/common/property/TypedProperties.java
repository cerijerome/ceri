package ceri.common.property;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import ceri.common.array.Array;
import ceri.common.collect.Sets;
import ceri.common.function.Excepts;
import ceri.common.stream.Collect;
import ceri.common.stream.Streams;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.common.util.Basics;

/**
 * Class for accessing typed properties with a common key prefix. Useful when sharing one properties
 * source across multiple components.
 */
public class TypedProperties {
	/** A no-op, stateless instance. */
	public static final TypedProperties NULL = new TypedProperties(PropertySource.NULL) {};
	private static final Excepts.Predicate<RuntimeException, String> BY_ID =
		Regex.Filter.match("\\d+");
	public final String prefix;
	private final PropertySource properties;

	/**
	 * Base ref class.
	 */
	public static class Ref extends Basics.Ref<TypedProperties> {
		protected Ref(TypedProperties ref, String... groups) {
			super(ref.sub(groups));
		}

		protected Parser.String parse(String... keyParts) {
			return ref.parse(keyParts);
		}
	}

	/**
	 * Merges properties to one with no prefix. Keys and values are copied into a new properties
	 * object, accessed with dot-separated keys.
	 */
	public static TypedProperties merge(TypedProperties... properties) {
		return merge(Arrays.asList(properties));
	}

	/**
	 * Merges properties with new absolute key prefix. Keys and values are copied into a new
	 * properties object, accessed with dot-separated keys.
	 */
	public static TypedProperties merge(Collection<TypedProperties> sources, String... prefix) {
		Properties properties = new Properties();
		for (var source : sources) {
			for (var key : source.descendants(prefix)) {
				var value = source.get(key);
				var fullKey = source.separator().join(prefix, key);
				if (value != null) properties.setProperty(fullKey, value);
			}
		}
		return from(properties, prefix);
	}

	/**
	 * Loads properties from resource file. Location is same package as the class, the name is the
	 * hyphenated lower-case simple class name.
	 */
	public static TypedProperties load(Class<?> cls) throws IOException {
		return load(cls, null);
	}

	/**
	 * Loads properties from resource file. Location is same package as the class. The hyphenated
	 * lower-case simple class name is used if the given name is null.
	 */
	public static TypedProperties load(Class<?> cls, String name, String... prefix)
		throws IOException {
		return from(Property.load(cls, name), prefix);
	}

	/**
	 * Creates typed properties with key prefix from given properties.
	 */
	public static TypedProperties from(Properties properties, String... prefix) {
		return of(PropertySource.Properties.of(properties), prefix);
	}

	/**
	 * Creates typed properties with key prefix from given resource bundle.
	 */
	public static TypedProperties from(ResourceBundle bundle, String... prefix) {
		return of(PropertySource.Resource.of(bundle), prefix);
	}

	/**
	 * Creates typed properties with key prefix from given file path source.
	 */
	public static TypedProperties from(Path path, String... prefix) {
		return of(PropertySource.File.of(path), prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	public static TypedProperties of(PropertySource properties, String... prefix) {
		return new TypedProperties(properties, prefix);
	}

	/**
	 * Constructor for properties with given prefix keys.
	 */
	private TypedProperties(PropertySource properties, String... prefix) {
		this.properties = properties;
		if (prefix == null) prefix = Array.STRING.empty;
		this.prefix = separator().join(prefix);
	}

	/**
	 * Provides the separator for key formatting.
	 */
	public Separator separator() {
		return properties.separator();
	}

	/**
	 * Retrieves the String property from prefixed, dot-separated key. Returns null if no value
	 * exists for the key.
	 */
	public String get(String... keyParts) {
		String value = Strings.trim(properties.property(key(keyParts)));
		return Strings.isEmpty(value) ? null : value;
	}

	/**
	 * Retrieves a parser for the property with prefixed, dot-separated key, which may be null.
	 */
	public Parser.String parse(String... keyParts) {
		return Parser.string(get(keyParts));
	}

	/**
	 * Set the key value, or remove it if the given value is null. Throws
	 * UnsupportedOperationException for read-only properties.
	 */
	public void set(Object value, String... keyParts) {
		properties.property(key(keyParts), value == null ? null : String.valueOf(value));
	}

	/**
	 * Return typed properties with addition key prefix.
	 */
	public TypedProperties sub(String... prefix) {
		var key = key(prefix);
		if (this.prefix.equals(key)) return this;
		return new TypedProperties(properties, key);
	}

	/**
	 * Returns the full keys that start with prefix.
	 */
	public Set<String> children(String... keyParts) {
		return properties.children(key(keyParts));
	}

	/**
	 * Returns the full keys that start with prefix.
	 */
	public Set<String> descendants(String... keyParts) {
		return properties.descendants(key(keyParts));
	}

	/**
	 * Returns true if the key exists as a leaf or parent.
	 */
	public boolean hasKey(String... keyParts) {
		return properties.hasKey(key(keyParts));
	}

	/**
	 * Returns all the integer ids that are children of the given key, as an immutable sorted set.
	 */
	public Set<Integer> childIds(String... keyParts) {
		return Streams.from(children(keyParts)).filter(BY_ID).map(Integer::parseInt)
			.collect(Collect.set(Sets::tree));
	}

	@Override
	public String toString() {
		return properties.toString() + "[" + prefix + "]";
	}

	private String key(String... keyParts) {
		return separator().join(prefix, keyParts);
	}
}
