package ceri.common.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import ceri.common.function.Excepts;
import ceri.common.stream.Stream;

public class Maps {

	private Maps() {}

	/**
	 * Utility for building maps.
	 */
	public static class Builder<K, V> {
		public final Map<K, V> map;

		/**
		 * Builds a new linked hash map.
		 */
		public static <K, V> Builder<K, V> linked() {
			return of(new LinkedHashMap<>());
		}

		/**
		 * Builds a new hash map.
		 */
		public static <K, V> Builder<K, V> of() {
			return of(new HashMap<>());
		}

		/**
		 * Uses the given map for building.
		 */
		public static <K, V> Builder<K, V> of(Map<K, V> map) {
			return new Builder<>(map);
		}

		/**
		 * Puts keys and values in a new hash map.
		 */
		public static <K, V> Builder<K, V> of(K k0, V v0, K k1, V v1) {
			return Builder.<K, V>of().put(k0, v0).put(k1, v1);
		}

		/**
		 * Puts keys and values in a new hash map.
		 */
		public static <K, V> Builder<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2) {
			return of(k0, v0, k1, v1).put(k2, v2);
		}

		/**
		 * Puts keys and values in a new hash map.
		 */
		public static <K, V> Builder<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
			return of(k0, v0, k1, v1, k2, v2).put(k3, v3);
		}

		/**
		 * Puts keys and values in a new hash map.
		 */
		public static <K, V> Builder<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4,
			V v4) {
			return of(k0, v0, k1, v1, k2, v2, k3, v3).put(k4, v4);
		}

		private Builder(Map<K, V> map) {
			this.map = map;
		}

		/**
		 * Puts the key and value in the map.
		 */
		public Builder<K, V> put(K k, V v) {
			map.put(k, v);
			return this;
		}

		/**
		 * Puts keys and values in a new hash the map.
		 */
		public Builder<K, V> put(K k0, V v0, K k1, V v1) {
			return put(k0, v0).put(k1, v1);
		}

		/**
		 * Puts keys and values in a new hash the map.
		 */
		public Builder<K, V> put(K k0, V v0, K k1, V v1, K k2, V v2) {
			return put(k0, v0, k1, v1).put(k2, v2);
		}

		/**
		 * Puts keys and values in a new hash the map.
		 */
		public Builder<K, V> put(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
			return put(k0, v0, k1, v1, k2, v2).put(k3, v3);
		}

		/**
		 * Puts keys and values in a new hash the map.
		 */
		public Builder<K, V> put(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4,
			V v4) {
			return put(k0, v0, k1, v1, k2, v2, k3, v3).put(k4, v4);
		}

		/**
		 * Puts keys and values in the map.
		 */
		public Builder<K, V> putAll(Map<? extends K, ? extends V> m) {
			map.putAll(m);
			return this;
		}

		/**
		 * Returns an unmodifiable wrapper.
		 */
		public Map<K, V> unmodifiable() {
			return Collections.unmodifiableMap(map);
		}
	}

	/**
	 * Returns a stream of keys for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, K> keys(
		Excepts.BiPredicate<E, ? super K, ? super V> predicate, Map<? extends K, ? extends V> map) {
		return Stream.<E, Map.Entry<? extends K, ? extends V>>from(map.entrySet())
			.filter(e -> predicate.test(e.getKey(), e.getValue())).map(e -> e.getKey());
	}

	/**
	 * Returns a stream of values for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, V> values(
		Excepts.BiPredicate<E, ? super K, ? super V> predicate, Map<? extends K, ? extends V> map) {
		return Stream.<E, Map.Entry<? extends K, ? extends V>>from(map.entrySet())
			.filter(e -> predicate.test(e.getKey(), e.getValue())).map(e -> e.getValue());
	}
}
