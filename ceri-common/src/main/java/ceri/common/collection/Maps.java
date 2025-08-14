package ceri.common.collection;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import ceri.common.function.Excepts;
import ceri.common.stream.Stream;

/**
 * Support for mutable maps.
 */
public class Maps {
	private Maps() {}

	/**
	 * Map supplier.
	 */
	public static class Supplier {
		private Supplier() {}

		/**
		 * Returns a typed map instance.
		 */
		public static <K, V> Map<K, V> hash() {
			return new HashMap<>();
		}

		/**
		 * Returns a typed map instance, optimized for initial size.
		 */
		public static <K, V> Map<K, V> hash(int initialSize) {
			return new HashMap<>(initialSize);
		}

		/**
		 * Returns a typed map instance.
		 */
		public static <K, V> Map<K, V> linked() {
			return new LinkedHashMap<>();
		}

		/**
		 * Returns a typed map instance, optimized for initial size.
		 */
		public static <K, V> Map<K, V> linked(int initialSize) {
			return new LinkedHashMap<>(initialSize);
		}

		/**
		 * Returns a typed map instance.
		 */
		public static <K, V> NavigableMap<K, V> tree() {
			return new TreeMap<>();
		}
		
		/**
		 * Returns a typed identity hash map.
		 */
		public static <K, V> Map<K, V> identity() {
			return new IdentityHashMap<>();
		}
	}

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
		public static <K, V> Builder<K, V> of(K k0, V v0) {
			return Builder.<K, V>of().put(k0, v0);
		}

		/**
		 * Puts keys and values in a new hash map.
		 */
		public static <K, V> Builder<K, V> of(K k0, V v0, K k1, V v1) {
			return of(k0, v0).put(k1, v1);
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
		public Builder<K, V> put(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
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

	// construct

	/**
	 * Creates a mutable map by mapping iterable values to keys.
	 */
	public static <E extends Exception, T, K> Map<K, T> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return map(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys and values.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return mapPut(Supplier.hash(), keyMapper, valueMapper, iterable);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys, for absent keys only.
	 */
	public static <E extends Exception, T, K> Map<K, T> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return mapIfAbsent(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys and values, for absent keys only.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return mapPutIfAbsent(Supplier.hash(), keyMapper, valueMapper, iterable);
	}

	/**
	 * Creates a mutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> adapt(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper, Map<K, V> src) throws E {
		return adapt(keyMapper, v -> v, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> adapt(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		return adaptPut(Supplier.hash(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> biAdapt(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper, Map<K, V> src)
		throws E {
		return biAdapt(keyMapper, (_, v) -> v, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> biAdapt(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		return biAdaptPut(Supplier.hash(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by inverting keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> src) {
		return biAdapt((_, v) -> v, (k, _) -> k, src);
	}

	// filter

	/**
	 * Returns a stream of map keys for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, K> keys(
		Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate,
		Map<? extends K, ? extends V> map) {
		if (map == null) return Stream.empty();
		if (predicate == null) return Stream.from(map.keySet());
		return Stream.<E, Map.Entry<? extends K, ? extends V>>from(map.entrySet())
			.filter(e -> predicate.test(e.getKey(), e.getValue())).map(e -> e.getKey());
	}

	/**
	 * Returns a stream of map values for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, V> values(
		Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate,
		Map<? extends K, ? extends V> map) {
		if (map == null) return Stream.empty();
		if (predicate == null) return Stream.from(map.values());
		return Stream.<E, Map.Entry<? extends K, ? extends V>>from(map.entrySet())
			.filter(e -> predicate.test(e.getKey(), e.getValue())).map(e -> e.getValue());
	}

	// put

	/**
	 * Puts mapped iterable values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M mapPut(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		if (dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var t : iterable)
			dest.put(keyMapper.apply(t), valueMapper.apply(t));
		return dest;
	}

	/**
	 * Puts mapped iterable values into the map, for absent keys only.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M mapPutIfAbsent(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		if (dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var t : iterable)
			dest.putIfAbsent(keyMapper.apply(t), valueMapper.apply(t));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adaptPut(M dest,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet())
			dest.put(keyMapper.apply(e.getKey()), valueMapper.apply(e.getValue()));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdaptPut(M dest,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet()) {
			var k = e.getKey();
			var v = e.getValue();
			dest.put(keyMapper.apply(k, v), valueMapper.apply(k, v));
		}
		return dest;
	}

	/**
	 * Copies entries from source to destination map by inverting keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> src, Map<V, K> dest) {
		return biAdaptPut(dest, (_, v) -> v, (k, _) -> k, src);
	}
}
