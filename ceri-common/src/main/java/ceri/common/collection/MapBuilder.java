package ceri.common.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to create an immutable map.
 */
public class MapBuilder<K, V> {
	private final Map<K, V> map;

	private MapBuilder(Map<K, V> map) {
		this.map = map;
	}

	public static <K, V> MapBuilder<K, V> of(K k, V v) {
		return new MapBuilder<K, V>(new LinkedHashMap<>()).put(k, v);
	}

	public static <K, V> MapBuilder<K, V> of(K k1, V v1, K k2, V v2) {
		return of(k1, v1).put(k2,  v2);
	}

	public static <K, V> MapBuilder<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
		return of(k1, v1, k2, v2).put(k3,  v3);
	}

	public static <K, V> MapBuilder<K, V> create() {
		return new MapBuilder<>(new HashMap<K, V>());
	}

	public static <K, V> MapBuilder<K, V> create(Map<K, V> map) {
		return new MapBuilder<>(map);
	}

	public MapBuilder<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
		return this;
	}

	public Map<K, V> build() {
		return Collections.unmodifiableMap(map);
	}

}
