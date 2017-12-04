package ceri.ent.service.fixer;

import java.util.LinkedHashMap;
import java.util.Map;
import ceri.common.collection.ImmutableUtil;

/**
 * Replaces values by key and sub-field. Useful for correcting fields retrieved by services.
 * {@link FixerPropertyParser} is used to load fixes from property files.
 */
public class Fixer<K, V> {
	public final Map<K, V> map;

	public static class Builder<K, V> {
		Map<K, V> map = new LinkedHashMap<>();

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

	public V value(K key) {
		return map.get(key);
	}

	public V fix(K key, V value) {
		V fixedValue = value(key);
		if (fixedValue != null) return fixedValue;
		return value;
	}

}
