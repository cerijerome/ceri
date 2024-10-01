package ceri.common.collection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable bidirectional maps.
 */
public class BiMap<K, V> {
	public final Map<K, V> keys;
	public final Map<V, K> values;

	public static <K, V> BiMap<K, V> of() {
		return new Builder<K, V>().build();
	}

	public static <K, V> BiMap<K, V> of(K k0, V v0) {
		return BiMap.builder(k0, v0).build();
	}

	public static <K, V> BiMap<K, V> of(K k0, V v0, K k1, V v1) {
		return BiMap.builder(k0, v0).put(k1, v1).build();
	}

	public static <K, V> BiMap<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2) {
		return BiMap.builder(k0, v0).put(k1, v1).put(k2, v2).build();
	}

	public static <K, V> BiMap<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		return BiMap.builder(k0, v0).put(k1, v1).put(k2, v2).put(k3, v3).build();
	}

	public static class Builder<K, V> {
		final Map<K, V> keys = new LinkedHashMap<>();
		final Map<V, K> values = new LinkedHashMap<>();

		Builder() {}

		public Builder<K, V> put(K key, V value) {
			keys.put(key, value);
			values.put(value, key);
			return this;
		}

		public Builder<K, V> putAll(Map<K, V> keys) {
			this.keys.putAll(keys);
			keys.forEach((k, v) -> values.put(v, k));
			return this;
		}

		public BiMap<K, V> build() {
			return new BiMap<>(this);
		}
	}

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	public static <K, V> Builder<K, V> builder(K k, V v) {
		return BiMap.<K, V>builder().put(k,  v);
	}

	BiMap(Builder<K, V> builder) {
		keys = ImmutableUtil.copyAsMap(builder.keys);
		values = ImmutableUtil.copyAsMap(builder.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keys);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BiMap<?, ?> other)) return false;
		return Objects.equals(keys, other.keys);
	}

	@Override
	public String toString() {
		return keys.toString();
	}
}
