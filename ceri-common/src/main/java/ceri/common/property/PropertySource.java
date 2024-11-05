package ceri.common.property;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.Lazy;
import ceri.common.function.FunctionUtil;
import ceri.common.io.IoUtil;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;

/**
 * Provides access to string keys and values.
 */
public interface PropertySource {
	/** A no-op,stateless instance. */
	PropertySource NULL = new Null() {};

	/**
	 * Provides the separator for key formatting.
	 */
	Separator separator();

	/**
	 * Lists direct sub-keys from the given key. Use empty string for the root key.
	 */
	Set<String> children(String key);

	/**
	 * Lists full sub-key paths below the given key. Use empty string for the root key.
	 */
	default Set<String> descendants(String key) {
		return PropertySource.descendantsFromChildren(this, key);
	}

	/**
	 * Returns true if the key exists as leaf or parent.
	 */
	boolean hasKey(String key);

	/**
	 * Returns the property value.
	 */
	String property(String key);

	/**
	 * Sets a property value. If the value is null, the property is removed (if supported).
	 */
	void property(String key, String value);

	/**
	 * Returns true if a property has been written or removed; clears the modified state.
	 */
	boolean modified();

	/**
	 * An implementation backed by properties.
	 */
	static class Properties implements PropertySource {
		public final java.util.Properties properties;
		private final Separator separator;
		private volatile Set<String> allKeys = null;
		private volatile boolean modified = false;

		/**
		 * Creates an instance with dot-separated keys.
		 */
		public static Properties of(java.util.Properties properties) {
			return of(properties, Separator.DOT);
		}

		/**
		 * Creates an instance with given key formatting.
		 */
		public static Properties of(java.util.Properties properties, Separator separator) {
			return new Properties(properties, separator);
		}

		private Properties(java.util.Properties properties, Separator separator) {
			this.properties = properties;
			this.separator = separator;
		}

		@Override
		public Separator separator() {
			return separator;
		}

		@Override
		public Set<String> children(String key) {
			return PropertySource.childrenFromKeys(separator(), allKeys(), key);
		}

		@Override
		public Set<String> descendants(String key) {
			return PropertySource.descendantsFromKeys(separator(), allKeys(), key);
		}

		@Override
		public boolean hasKey(String key) {
			return PropertySource.hasKey(separator(), allKeys(), key);
		}

		@Override
		public String property(String key) {
			return properties.getProperty(key);
		}

		@Override
		public void property(String key, String value) {
			if (value == null) {
				if (properties.remove(key) != null) {
					allKeys = null;
					modified = true;
				}
			} else {
				if (!value.equals(properties.put(key, value))) {
					allKeys = null;
					modified = true;
				}
			}
		}

		@Override
		public boolean modified() {
			var modified = this.modified;
			this.modified = false;
			return modified;
		}

		@Override
		public String toString() {
			return ToString.forClass(this);
		}

		private Set<String> allKeys() {
			var allKeys = this.allKeys;
			if (allKeys == null) {
				allKeys = properties.stringPropertyNames();
				this.allKeys = allKeys;
			}
			return allKeys;
		}
	}

	/**
	 * An implementation backed by a resource bundle. Read-only.
	 */
	static class Resource implements PropertySource {
		public final ResourceBundle bundle;
		private final Separator separator;
		private final Lazy.Supplier<RuntimeException, Set<String>> allKeys;

		/**
		 * Creates an instance with dot-separated keys.
		 */
		public static Resource of(ResourceBundle bundle) {
			return of(bundle, Separator.DOT);
		}

		/**
		 * Creates an instance with given key formatting.
		 */
		public static Resource of(ResourceBundle bundle, Separator separator) {
			return new Resource(bundle, separator);
		}

		private Resource(ResourceBundle bundle, Separator separator) {
			this.bundle = bundle;
			this.separator = separator;
			allKeys = Lazy.unsafe(() -> bundle.keySet());
		}

		@Override
		public Separator separator() {
			return separator;
		}

		@Override
		public Set<String> children(String key) {
			return PropertySource.childrenFromKeys(separator(), allKeys.get(), key);
		}

		@Override
		public Set<String> descendants(String key) {
			return PropertySource.descendantsFromKeys(separator(), allKeys.get(), key);
		}

		@Override
		public boolean hasKey(String key) {
			return PropertySource.hasKey(separator(), allKeys.get(), key);
		}

		@Override
		public String property(String key) {
			return FunctionUtil.getSilently(() -> bundle.getString(key));
		}

		@Override
		public void property(String key, String value) {
			if (Objects.equals(value, property(key))) return;
			throw new UnsupportedOperationException("Resource bundle is read-only");
		}

		@Override
		public boolean modified() {
			return false;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, bundle.getBaseBundleName());
		}
	}

	/**
	 * An implementation backed by (virtual) file system. Does not support removal of keys.
	 */
	static class File implements PropertySource {
		public final Path root;
		private volatile boolean modified = false;

		/**
		 * Creates an instance using the root path.
		 */
		public static File of(String root) {
			return of(Path.of(root));
		}

		/**
		 * Creates an instance using the root path.
		 */
		public static File of(Path root) {
			return new File(root);
		}

		private File(Path root) {
			this.root = root;
		}

		@Override
		public Separator separator() {
			return Separator.SLASH;
		}

		@Override
		public Set<String> children(String key) {
			var array = path(key).toFile().list();
			if (array == null) return Set.of();
			Arrays.sort(array);
			return ImmutableUtil.asSet(array);
		}

		@Override
		public boolean hasKey(String key) {
			return Files.exists(path(key));
		}

		@Override
		public String property(String key) {
			var path = path(key);
			if (!exists(path)) return null;
			return IoUtil.RUNTIME_IO_ADAPTER.get(() -> Files.readString(path(key)));
		}

		@Override
		public void property(String key, String value) {
			var path = path(key);
			if (value == null) {
				if (!exists(path)) return; // do nothing
				throw new UnsupportedOperationException("Key removal not permitted: " + key);
			}
			IoUtil.RUNTIME_IO_ADAPTER.run(() -> Files.writeString(path(key), value));
			modified = true;
		}

		@Override
		public boolean modified() {
			var modified = this.modified;
			this.modified = false;
			return modified;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, root);
		}

		private boolean exists(Path path) {
			return Files.isRegularFile(path);
		}

		private Path path(String key) {
			var path = root.resolve(key).normalize();
			if (path.startsWith(root)) return path;
			throw new IllegalArgumentException("Cannot traverse past root: " + key);
		}
	}

	/**
	 * A no-op, stateless implementation.
	 */
	static interface Null extends PropertySource {
		@Override
		default Separator separator() {
			return Separator.NULL;
		}

		@Override
		default Set<String> children(String key) {
			return Set.of();
		}

		@Override
		default Set<String> descendants(String key) {
			return Set.of();
		}

		@Override
		default boolean hasKey(String key) {
			return false;
		}

		@Override
		default String property(String key) {
			return null;
		}

		@Override
		default void property(String key, String value) {}

		@Override
		default boolean modified() {
			return false;
		}
	}

	/* Support for implementations */

	static boolean hasKey(Separator separator, Iterable<String> paths, String key) {
		for (var path : paths) {
			if (!path.startsWith(key)) continue;
			if (path.length() == key.length()) return true;
			if (separator.matches(path, key.length())) return true;
		}
		return false;
	}

	/**
	 * Provides child key names from full list of key paths.
	 */
	static Set<String> childrenFromKeys(Separator separator, Iterable<String> paths, String key) {
		Set<String> children = new LinkedHashSet<>();
		boolean root = StringUtil.empty(key);
		int prefixLen = root ? 0 : key.length() + separator.value().length();
		for (var path : paths) {
			if (!root && (path.length() <= prefixLen || !path.startsWith(key)
				|| !StringUtil.matchAt(path, key.length(), separator.value()))) continue;
			int i = path.indexOf(separator.value(), prefixLen);
			if (i == prefixLen) continue;
			if (i < 0) i = path.length();
			children.add(path.substring(prefixLen, i));
		}
		return Collections.unmodifiableSet(children);
	}

	/**
	 * Provides relative descendant paths from full list of key paths.
	 */
	static Set<String> descendantsFromKeys(Separator separator, Iterable<String> paths,
		String key) {
		Set<String> descendants = new LinkedHashSet<>();
		boolean root = StringUtil.empty(key);
		int prefixLen = root ? 0 : key.length() + separator.value().length();
		for (var path : paths) {
			if (!root && (path.length() <= prefixLen || !path.startsWith(key)
				|| !StringUtil.matchAt(path, key.length(), separator.value()))) continue;
			descendants.add(path.substring(prefixLen));
		}
		return Collections.unmodifiableSet(descendants);
	}

	/**
	 * Provides descendants by calling children
	 */
	static Set<String> descendantsFromChildren(PropertySource source, String key) {
		Set<String> descendants = new LinkedHashSet<>();
		int preLen = StringUtil.empty(key) ? 0 : key.length() + source.separator().value().length();
		appendDescendantsFromChildren(descendants, source, preLen, key);
		return Collections.unmodifiableSet(descendants);
	}

	/**
	 * Provides descendants by calling children
	 */
	private static void appendDescendantsFromChildren(Set<String> receiver, PropertySource source,
		int preLen, String key) {
		var children = source.children(key);
		if (children.isEmpty()) receiver.add(key.substring(preLen));
		else for (var child : children)
			appendDescendantsFromChildren(receiver, source, preLen,
				source.separator().join(key, child));
	}
}
