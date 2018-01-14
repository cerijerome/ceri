package ceri.common.collection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility to create an mutable map.
 */
public class MapPopulator<K, V> {
	public final Map<K, V> map;

	private MapPopulator(Map<K, V> map) {
		this.map = map;
	}

	public static <K, V> MapPopulator<K, V> of(K k, V v) {
		return new MapPopulator<K, V>(new LinkedHashMap<>()).put(k, v);
	}

	public static <K, V> MapPopulator<K, V> of(K k1, V v1, K k2, V v2) {
		return of(k1, v1).put(k2, v2);
	}

	public static <K, V> MapPopulator<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
		return of(k1, v1, k2, v2).put(k3, v3);
	}

	public static <K, V> MapPopulator<K, V> wrap(Map<K, V> map) {
		return new MapPopulator<>(map);
	}

	public MapPopulator<K, V> put(K key, V value) {
		map.put(key, value);
		return this;
	}

	public MapPopulator<K, V> putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
		return this;
	}

}
