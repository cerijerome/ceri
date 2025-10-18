package ceri.common.collect;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import ceri.common.array.RawArray;
import ceri.common.function.Compares;
import ceri.common.function.Excepts;
import ceri.common.function.Excepts.BiPredicate;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Stream;

/**
 * Support for mutable maps.
 */
public class Maps {
	private Maps() {}

	/**
	 * Map predicate support.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Transforms a bi-predicate into a map entry predicate.
		 */
		public static <E extends Exception, K, V> Excepts.Predicate<E, Map.Entry<K, V>>
			entry(Excepts.BiPredicate<E, ? super K, ? super V> predicate) {
			if (predicate == null) return _ -> false;
			return Filters.biTesting(Map.Entry::getKey, Map.Entry::getValue, predicate);
		}
	}

	/**
	 * Map comparator support.
	 */
	public static class Compare {
		private static Comparator<Map.Entry<?, ?>> KEY =
			Reflect.unchecked(key(Comparator.naturalOrder()));
		private static Comparator<Map.Entry<?, ?>> VALUE =
			Reflect.unchecked(value(Comparator.naturalOrder()));

		private Compare() {}

		/**
		 * Provide a map entry comparator by key.
		 */
		public static <K extends Comparable<? super K>, V> Comparator<Map.Entry<K, V>> key() {
			return Reflect.unchecked(KEY);
		}

		/**
		 * Provide a map entry comparator by key.
		 */
		public static <K, V> Comparator<Map.Entry<K, V>> key(Comparator<? super K> comparator) {
			return Comparator.comparing(Map.Entry::getKey, comparator);
		}

		/**
		 * Provide a map entry comparator by value.
		 */
		public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> value() {
			return Reflect.unchecked(VALUE);
		}

		/**
		 * Provide a map entry comparator by value.
		 */
		public static <K, V> Comparator<Map.Entry<K, V>> value(Comparator<? super V> comparator) {
			return Comparator.comparing(Map.Entry::getValue, comparator);
		}
	}

	/**
	 * Map key/value put methods.
	 */
	public enum Put {
		/** Preserve the first value for each key. */
		first,
		/** Preserve the last value for each key. */
		last,
		/** Fail if the key already has a value. */
		unique;

		public static final Put def = last;

		/**
		 * Put the key/value in the map according to the put method.
		 */
		public static <K, V> V put(Put put, Map<K, V> map, K key, V value) {
			if (put == null || map == null) return null;
			return switch (put) {
				case first -> map.putIfAbsent(key, value);
				case unique -> putUnique(map, key, value);
				default -> map.put(key, value);
			};
		}

		/**
		 * Put the key/value in the map according to the put method.
		 */
		public <K, V> V put(Map<K, V> map, K key, V value) {
			return put(this, map, key, value);
		}

		private static <K, V> V putUnique(Map<K, V> map, K key, V value) {
			if (!map.containsKey(key)) return map.put(key, value);
			throw new IllegalStateException("Key already exists: " + key);
		}
	}

	/**
	 * Two-way unmodifiable map of keys and values.
	 */
	public static class Bi<K, V> {
		private static final Bi<Object, Object> EMPTY = new Bi<>(Immutable.map(), Immutable.map());
		public final Map<K, V> keys;
		public final Map<V, K> values;

		public static <K, V> Bi<K, V> empty() {
			return Reflect.unchecked(EMPTY);
		}

		public static <K, V> Bi<K, V> of(Map<K, V> map) {
			if (Maps.isEmpty(map)) return empty();
			return new Bi<>(Immutable.wrap(map), Immutable.wrap(Maps.invert(map)));
		}

		private Bi(Map<K, V> keys, Map<V, K> values) {
			this.keys = keys;
			this.values = values;
		}

		/**
		 * Gets the values for the key.
		 */
		public V value(K key) {
			return keys.get(key);
		}

		public K key(V value) {
			return values.get(value);
		}
	}

	/**
	 * Utility for building maps.
	 */
	public static class Builder<K, V, M extends Map<K, V>> {
		private final M map;
		private Put put = Put.def;

		/**
		 * Start building a new map.
		 */
		public static <K, V, M extends Map<K, V>> Builder<K, V, M> of(M map) {
			return new Builder<>(map);
		}

		private Builder(M map) {
			this.map = map;
		}

		/**
		 * Puts the key and value in the map.
		 */
		public Builder<K, V, M> put(K key, V value) {
			Put.put(put, map, key, value);
			return this;
		}

		/**
		 * Puts the keys and value in the map.
		 */
		@SafeVarargs
		public final Builder<K, V, M> putKeys(V value, K... keys) {
			return putKeys(value, Arrays.asList(keys));
		}

		/**
		 * Puts the keys and value in the map.
		 */
		public final Builder<K, V, M> putKeys(V value, Iterable<? extends K> keys) {
			for (var key : keys)
				put(key, value);
			return this;
		}

		/**
		 * Puts keys and values in the map.
		 */
		public Builder<K, V, M> put(Map<? extends K, ? extends V> map) {
			if (map != null) map.forEach(this::put);
			return this;
		}

		/**
		 * Update the put method.
		 */
		public Builder<K, V, M> put(Put put) {
			if (put != null) this.put = put;
			return this;
		}

		/**
		 * Apply the populator function to the map.
		 */
		public <E extends Exception> Builder<K, V, M>
			apply(Excepts.Consumer<E, ? super M> populator) throws E {
			if (populator != null) populator.accept(get());
			return this;
		}

		/**
		 * Returns the underlying map.
		 */
		public M get() {
			return map;
		}

		/**
		 * Returns the map wrapped as unmodifiable.
		 */
		public Map<K, V> wrap() {
			return Immutable.wrap(map);
		}
	}

	/**
	 * Start building a map.
	 */
	public static <K, V> Builder<K, V, Map<K, V>> build(K key, V value) {
		return build(Maps::of, key, value);
	}

	/**
	 * Start building a map.
	 */
	public static <K, V, M extends Map<K, V>> Builder<K, V, M> build(Functions.Supplier<M> supplier,
		K key, V value) {
		return Builder.of(supplier.get()).put(key, value);
	}

	// create

	/**
	 * Creates an empty mutable linked hash map.
	 */
	public static <K, V> LinkedHashMap<K, V> link() {
		return new LinkedHashMap<>();
	}

	/**
	 * Creates an empty mutable tree map with null-first natural comparator.
	 */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> tree() {
		return new TreeMap<>(Compares.comparable());
	}

	/**
	 * Creates a mutable tree map with null-first natural comparator from map.
	 */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V>
		tree(Map<? extends K, ? extends V> map) {
		var tree = Maps.<K, V>tree();
		tree.putAll(map);
		return tree;
	}

	/**
	 * Creates an empty, mutable, identity hash map.
	 */
	public static <K, V> Map<K, V> id() {
		return new IdentityHashMap<>();
	}

	/**
	 * Creates an empty, mutable, concurrent hash map.
	 */
	public static <K, V> Map<K, V> concurrent() {
		return new ConcurrentHashMap<>();
	}

	/**
	 * Creates an empty, mutable, weak hash map.
	 */
	public static <K, V> Map<K, V> weak() {
		return new WeakHashMap<>();
	}

	/**
	 * Creates an empty, mutable, concurrent, weak hash map.
	 */
	public static <K, V> Map<K, V> syncWeak() {
		return Collections.synchronizedMap(weak());
	}

	/**
	 * Returns a map that removes older entries to ensure a maximum size.
	 */
	public static <K, V> LinkedHashMap<K, V> cache(int size) {
		return new LinkedHashMap<>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > size;
			}
		};
	}

	/**
	 * Creates an empty mutable map.
	 */
	public static <K, V> Map<K, V> of() {
		return new HashMap<>();
	}

	/**
	 * Creates a mutable map from the key and value.
	 */
	public static <K, V> Map<K, V> of(K k, V v) {
		return put(of(), k, v);
	}

	/**
	 * Creates a mutable map from keys and values.
	 */
	public static <K, V> Map<K, V> of(K k0, V v0, K k1, V v1) {
		return put(of(), k0, v0, k1, v1);
	}

	/**
	 * Creates a mutable map from keys and values.
	 */
	public static <K, V> Map<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2) {
		return put(of(), k0, v0, k1, v1, k2, v2);
	}

	/**
	 * Creates a mutable map from keys and values.
	 */
	public static <K, V> Map<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		return put(of(), k0, v0, k1, v1, k2, v2, k3, v3);
	}

	/**
	 * Creates a mutable map from keys and values.
	 */
	public static <K, V> Map<K, V> of(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
		return put(of(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
	}

	/**
	 * Creates a mutable map copying the keys and values.
	 */
	public static <K, V> Map<K, V> copy(Map<K, V> src) {
		return copy(Maps::of, src);
	}

	/**
	 * Creates a mutable map copying the keys and values.
	 */
	public static <K, V, M extends Map<K, V>> M copy(Functions.Supplier<M> supplier,
		Map<? extends K, ? extends V> src) {
		if (supplier == null) return null;
		return put(supplier.get(), src);
	}

	/**
	 * Creates a mutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> adapt(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper, Map<K, ? extends V> src)
		throws E {
		return adapt(keyMapper, v -> v, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> adapt(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		return adapt(Maps::of, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adapt(
		Functions.Supplier<M> supplier,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		return adapt(Put.def, supplier, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adapt(Put put,
		Functions.Supplier<M> supplier,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		if (supplier == null) return null;
		return adaptPut(put, supplier.get(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> biAdapt(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Map<K, ? extends V> src) throws E {
		return biAdapt(keyMapper, (_, v) -> v, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> biAdapt(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		return biAdapt(Maps::of, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdapt(
		Functions.Supplier<M> supplier,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		return biAdapt(Put.def, supplier, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdapt(Put put,
		Functions.Supplier<M> supplier,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		if (supplier == null) return null;
		return biAdaptPut(put, supplier.get(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by inverting keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> src) {
		return invert(Maps::of, src);
	}

	/**
	 * Creates a mutable map by inverting keys and values.
	 */
	public static <K, V, M extends Map<V, K>> M invert(Functions.Supplier<M> supplier,
		Map<? extends K, ? extends V> src) {
		return invert(Put.def, supplier, src);
	}

	/**
	 * Creates a mutable map by inverting keys and values.
	 */
	public static <K, V, M extends Map<V, K>> M invert(Put put, Functions.Supplier<M> supplier,
		Map<? extends K, ? extends V> src) {
		if (supplier == null) return null;
		return invertPut(put, supplier.get(), src);
	}

	/**
	 * Sorts and copies map entries into a linked hash map.
	 */
	public static <K, V> Map<K, V> sort(Comparator<? super Map.Entry<K, V>> comparator,
		Map<K, V> src) {
		if (isEmpty(src)) return link();
		var list = Lists.sort(Collectable.add(Lists.of(), src.entrySet()), comparator);
		return convert(Maps::link, Map.Entry::getKey, Map.Entry::getValue, list);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys.
	 */
	public static <E extends Exception, T, K> Map<K, T> convert(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper, Iterable<? extends T> src)
		throws E {
		return convert(keyMapper, t -> t, src);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys and values.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> convert(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> src) throws E {
		return convert(Maps::of, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys and values.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(
		Functions.Supplier<M> supplier,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> src) throws E {
		return convert(Put.def, supplier, keyMapper, valueMapper, src);
	}

	/**
	 * Creates a mutable map by mapping iterable values to keys and values.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convert(Put put,
		Functions.Supplier<M> supplier,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> src) throws E {
		return convertPut(put, supplier.get(), keyMapper, valueMapper, src);
	}

	// access

	/**
	 * Returns true if the map is null or has no elements.
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Returns true if the map is non-null and has elements.
	 */
	public static boolean nonEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}

	/**
	 * Returns the size of the map, or 0 if null.
	 */
	public static int size(Map<?, ?> map) {
		return map == null ? 0 : map.size();
	}

	/**
	 * Returns the mapped value for the key; returns null if no mapping, or the map is null.
	 */
	public static <K, V> V get(Map<K, V> map, K key) {
		return get(map, key, null);
	}

	/**
	 * Returns the mapped value for the key; returns default if no mapping, or the map is null.
	 */
	public static <K, V> V get(Map<K, V> map, K key, V def) {
		return isEmpty(map) ? def : map.getOrDefault(key, def);
	}

	/**
	 * Returns the last key, or null if null or empty.
	 */
	public static <T> T firstKey(SortedMap<T, ?> map) {
		return firstKey(map, null);
	}

	/**
	 * Returns the last key, or default if null or empty.
	 */
	public static <T> T firstKey(SortedMap<T, ?> map, T def) {
		return nonEmpty(map) ? map.firstKey() : def;
	}

	/**
	 * Returns the last key, or null if null or empty.
	 */
	public static <T> T lastKey(SortedMap<T, ?> map) {
		return lastKey(map, null);
	}

	/**
	 * Returns the last key, or default if null or empty.
	 */
	public static <T> T lastKey(SortedMap<T, ?> map, T def) {
		return nonEmpty(map) ? map.lastKey() : def;
	}

	/**
	 * Iterates over map entries, allowing checked exceptions.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		Excepts.BiConsumer<E, ? super K, ? super V> consumer) throws E {
		if (isEmpty(map) || consumer == null) return;
		for (var entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

	/**
	 * Removes map entries that match the predicate. Returns the removed entry count.
	 */
	public static <E extends Exception, K, V, M extends Map<K, V>> M removeIf(M map,
		BiPredicate<? extends E, ? super K, ? super V> predicate) throws E {
		if (!isEmpty(map) && predicate != null)
			Iterables.removeIf(map.entrySet(), Filter.entry(predicate));
		return map;
	}

	/**
	 * Returns a stream of map keys for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, K>
		keys(Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate, Map<K, V> map) {
		return Maps.<E, K, V>entries(predicate, map).map(Map.Entry::getKey);
	}

	/**
	 * Returns a stream of map values for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, V>
		values(Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate, Map<K, V> map) {
		return Maps.<E, K, V>entries(predicate, map).map(Map.Entry::getValue);
	}

	/**
	 * Returns a stream of map values for entries that match the predicate.
	 */
	public static <E extends Exception, K, V> Stream<E, Map.Entry<K, V>>
		entries(Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate, Map<K, V> map) {
		return Maps.<E, K, V>stream(map).filter(Filter.entry(predicate));
	}

	/**
	 * Returns a stream of map entries.
	 */
	public static <E extends Exception, K, V> Stream<E, Map.Entry<K, V>> stream(Map<K, V> map) {
		if (isEmpty(map)) return Stream.empty();
		return Stream.from(map.entrySet());
	}

	/**
	 * Computes the map value if absent. Computation may execute and overwrite the mapping more than
	 * once. Suitable for concurrent map caches.
	 */
	public static <E extends Exception, K, V> V lazyCompute(Map<K, V> map, K key,
		Excepts.Function<E, ? super K, ? extends V> valueFn) throws E {
		V value = map.get(key);
		if (value != null) return value;
		value = valueFn.apply(key);
		map.put(key, value);
		return value;
	}

	// put

	/**
	 * Puts the key and value in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, K k, V v) {
		if (dest != null) dest.put(k, v);
		return dest;
	}

	/**
	 * Puts the keys and values in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, K k0, V v0, K k1, V v1) {
		return put(put(dest, k0, v0), k1, v1);
	}

	/**
	 * Puts the keys and values in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, K k0, V v0, K k1, V v1, K k2, V v2) {
		return put(put(dest, k0, v0, k1, v1), k2, v2);
	}

	/**
	 * Puts the keys and values in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, K k0, V v0, K k1, V v1, K k2, V v2,
		K k3, V v3) {
		return put(put(dest, k0, v0, k1, v1, k2, v2), k3, v3);
	}

	/**
	 * Puts the keys and values in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, K k0, V v0, K k1, V v1, K k2, V v2,
		K k3, V v3, K k4, V v4) {
		return put(put(dest, k0, v0, k1, v1, k2, v2, k3, v3), k4, v4);
	}

	/**
	 * Puts the key and value in the map based on put method.
	 */
	public static <K, V, M extends Map<K, V>> M put(Put put, M dest, K key, V value) {
		Put.put(put, dest, key, value);
		return dest;
	}

	/**
	 * Puts the keys and values in the map.
	 */
	public static <K, V, M extends Map<K, V>> M put(M dest, Map<? extends K, ? extends V> src) {
		if (dest != null && src != null) dest.putAll(src);
		return dest;
	}

	/**
	 * Puts the keys and values in the map based on put method.
	 */
	public static <K, V, M extends Map<K, V>> M put(Put put, M dest,
		Map<? extends K, ? extends V> src) {
		if (dest != null && src != null) src.forEach((k, v) -> put(put, dest, k, v));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adaptPut(M dest,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper,
		Map<? extends K, ? extends V> src) throws E {
		return adaptPut(Put.def, dest, keyMapper, valueMapper, src);
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values, if absent.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adaptPut(Put put, M dest,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet())
			put(put, dest, keyMapper.apply(e.getKey()), valueMapper.apply(e.getValue()));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdaptPut(M dest,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		return biAdaptPut(Put.def, dest, keyMapper, valueMapper, src);
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values, if absent.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdaptPut(Put put,
		M dest, Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet()) {
			var k = e.getKey();
			var v = e.getValue();
			put(put, dest, keyMapper.apply(k, v), valueMapper.apply(k, v));
		}
		return dest;
	}

	/**
	 * Puts inverted keys and values into the map.
	 */
	public static <K, V, M extends Map<V, K>> M invertPut(M dest,
		Map<? extends K, ? extends V> src) {
		return invertPut(Put.def, dest, src);
	}

	/**
	 * Puts inverted keys and values into the map.
	 */
	public static <K, V, M extends Map<V, K>> M invertPut(Put put, M dest,
		Map<? extends K, ? extends V> src) {
		return biAdaptPut(put, dest, (_, v) -> v, (k, _) -> k, src);
	}

	/**
	 * Puts mapped array values into the map.
	 */
	@SafeVarargs
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutAll(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T... values) throws E {
		return convertPut(dest, keyMapper, valueMapper, values, 0);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset)
		throws E {
		return convertPut(dest, keyMapper, valueMapper, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset,
		int length) throws E {
		return convertPut(Put.def, dest, keyMapper, valueMapper, src, offset, length);
	}

	/**
	 * Puts mapped array values into the map.
	 */
	@SafeVarargs
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutAll(Put put,
		M dest, Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T... values) throws E {
		return convertPut(put, dest, keyMapper, valueMapper, values, 0);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(Put put, M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset)
		throws E {
		return convertPut(put, dest, keyMapper, valueMapper, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(Put put, M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset,
		int length) throws E {
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		RawArray.acceptSlice(src, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				put(put, dest, keyMapper.apply(src[o + i]), valueMapper.apply(src[o + i]));
		});
		return dest;
	}

	/**
	 * Puts mapped iterable values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> src)
		throws E {
		return convertPut(Put.def, dest, keyMapper, valueMapper, src);
	}

	/**
	 * Puts mapped iterable values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPut(Put put, M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> src)
		throws E {
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		for (var t : src)
			put(put, dest, keyMapper.apply(t), valueMapper.apply(t));
		return dest;
	}

	/**
	 * Puts expanded keys and mapped values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M expandKeyPut(M dest,
		Excepts.Function<E, ? super T, ? extends Iterable<K>> keyExpander,
		Excepts.Function<E, ? super T, ? extends V> valueMapper, Iterable<? extends T> src)
		throws E {
		return expandKeyPut(Put.def, dest, keyExpander, valueMapper, src);
	}

	/**
	 * Puts expanded keys and mapped values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M expandKeyPut(Put put,
		M dest, Excepts.Function<E, ? super T, ? extends Iterable<K>> keyExpander,
		Excepts.Function<E, ? super T, ? extends V> valueMapper, Iterable<? extends T> src)
		throws E {
		for (var t : src) {
			var v = valueMapper.apply(t);
			for (var k : keyExpander.apply(t))
				put(put, dest, k, v);
		}
		return dest;
	}
}
