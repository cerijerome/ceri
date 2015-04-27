package ceri.ent.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import ceri.common.collection.ImmutableUtil;
import ceri.common.factory.Factory;

/**
 * Class to encapsulate values to be corrected based on key.
 */
public class Fixer<K, V> {
	private final Map<K, V> map;

	public static <K, V> Fixer<K, V> create(Properties properties, Factory<K, String> keyFactory,
		Factory<V, String> valueFactory) {
		Builder<K, V> builder = builder();
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key);
			K k = keyFactory.create(key);
			V v = valueFactory.create(value);
			builder.add(k, v);
		}
		return builder.build();
	}

	public static class Builder<K, V> {
		Map<K, V> map = new HashMap<>();

		Builder() {}

		public Builder<K, V> add(K key, V value) {
			map.put(key, value);
			return this;
		}

		public Fixer<K, V> build() {
			return new Fixer<>(this);
		}
	}

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	Fixer(Builder<K, V> builder) {
		map = ImmutableUtil.copyAsMap(builder.map);
	}

	public V fix(K key, V value) {
		V fixedValue = map.get(key);
		if (fixedValue != null) return fixedValue;
		return value;
	}

}
