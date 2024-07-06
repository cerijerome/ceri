package ceri.common.property;

import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.FunctionUtil;

/**
 * An interface for accessing key-value string properties.
 */
public interface PropertyAccessor {
	/** No-op, stateless instance. */
	static Null NULL = new Null() {};
	
	/**
	 * Get all property keys.
	 */
	Set<String> keys();

	/**
	 * Get the property value. Returns null if not present.
	 */
	String property(String key);

	/**
	 * Set the property value, or remove it if the given value is null. Throws
	 * UnsupportedOperationException for read-only properties.
	 */
	void property(String key, String value);

	/**
	 * Iterate over each key and value.
	 */
	default void forEach(BiConsumer<String, String> consumer) {
		for (var key : keys())
			consumer.accept(key, property(key));
	}

	/**
	 * Copy properties into a mutable map.
	 */
	default Map<String, String> properties() {
		return CollectionUtil.toMap(Function.identity(), this::property, keys());
	}

	/**
	 * Create an instance backed by properties.
	 */
	static PropertyAccessor from(Properties properties) {
		return new PropertyAccessor() {
			@Override
			public Set<String> keys() {
				return properties.stringPropertyNames();
			}

			@Override
			public String property(String key) {
				return properties.getProperty(key);
			}

			@Override
			public void property(String key, String value) {
				if (value == null) properties.remove(key);
				else properties.put(key, value);
			}

			@Override
			public String toString() {
				return properties.toString();
			}
		};
	}

	/**
	 * Create an instance backed by a resource bundle.
	 */
	static PropertyAccessor from(ResourceBundle bundle) {
		return new PropertyAccessor() {
			@Override
			public Set<String> keys() {
				return bundle.keySet();
			}

			@Override
			public String property(String key) {
				return FunctionUtil.getSilently(() -> bundle.getString(key));
			}

			@Override
			public void property(String key, String value) {
				throw new UnsupportedOperationException("Resource bundle is read-only");
			}

			@Override
			public Map<String, String> properties() {
				return CollectionUtil.toMap(Function.identity(), this::property, keys());
			}

			@Override
			public String toString() {
				return bundle.toString();
			}
		};
	}

	/**
	 * No-op, stateless implementation.
	 */
	interface Null extends PropertyAccessor {
		@Override
		default Set<String> keys() {
			return Set.of();
		}

		@Override
		default String property(String key) {
			return null;
		}

		@Override
		default void property(String key, String value) {}
	}
}
