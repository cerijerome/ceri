package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import ceri.common.array.ArrayUtil;
import ceri.common.array.RawArray;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.math.MathUtil;
import ceri.common.stream.Stream;

/**
 * Mutable collection support.
 */
public class Mutable {
	private Mutable() {}

	/**
	 * Utility for building maps.
	 */
	public static class MapBuilder<K, V, M extends Map<K, V>> {
		public final M map;

		protected MapBuilder(M map) {
			this.map = map;
		}

		public <E extends Exception> MapBuilder<K, V, M>
			apply(Excepts.Consumer<E, ? super M> populator) throws E {
			if (populator != null) populator.accept(map);
			return this;
		}

		public MapBuilder<K, V, M> put(K key, V value) {
			map.put(key, value);
			return this;
		}
	}

	// lists

	/**
	 * Creates a list from values.
	 */
	@SafeVarargs
	public static <T> List<T> listOf(T... values) {
		return Mutable.addAll(list(), values);
	}

	/**
	 * Creates an empty list.
	 */
	public static <T> List<T> list() {
		return new ArrayList<>();
	}

	/**
	 * Creates a list from array values.
	 */
	public static <T> List<T> list(T[] array, int offset) {
		return list(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a list from array values.
	 */
	public static <T> List<T> list(T[] array, int offset, int length) {
		return Mutable.add(list(), array, offset, length);
	}

	/**
	 * Creates a list from iterable values.
	 */
	public static <T> List<T> list(Iterable<? extends T> values) {
		return Mutable.add(list(), values);
	}

	/**
	 * Creates a list from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> List<U> adaptListOf(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adaptAddAll(list(), mapper, values);
	}

	/**
	 * Creates a list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
		throws E {
		return adaptList(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return adaptAdd(list(), mapper, array, offset, length);
	}

	/**
	 * Creates a list from transformed iterable values.
	 */
	public static <E extends Exception, T, U> List<U> adaptList(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return adaptAdd(list(), mapper, values);
	}

	/**
	 * Creates a list from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> List<T> convertList(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return convertAdd(list(), unmapper, map);
	}

	/**
	 * Gets the element at index, or null.
	 */
	public static <T> T at(List<? extends T> list, int index) {
		return at(list, index, null);
	}

	/**
	 * Gets the element at index, or default.
	 */
	public static <T> T at(List<? extends T> list, int index, T def) {
		if (list == null || index < 0 || list.isEmpty() || index >= list.size()) return def;
		return list.get(index);
	}

	/**
	 * Returns the last element, or default.
	 */
	public static <T> T last(List<? extends T> list) {
		return last(list, null);
	}

	/**
	 * Returns the last element, or default.
	 */
	public static <T> T last(List<? extends T> list, T def) {
		if (list == null || list.isEmpty()) return def;
		return list.getLast();
	}

	/**
	 * Inserts values into the list at index.
	 */
	@SafeVarargs
	public static <T> List<T> insertAll(List<T> dest, int index, T... src) {
		return insert(dest, index, src, 0);
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, T[] src, int offset) {
		return insert(dest, index, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, T[] src, int offset, int length) {
		if (src == null || dest == null) return dest;
		return ArrayUtil.applySlice(src.length, offset, length, (o, l) -> {
			int i = MathUtil.limit(index, 0, dest.size() - 1);
			dest.addAll(i, Arrays.asList(src).subList(o, o + l));
			return dest;
		});
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, Collection<T> src) {
		if (src == null || dest == null) return dest;
		int i = MathUtil.limit(index, 0, dest.size() - 1);
		dest.addAll(i, src);
		return dest;
	}

	// sets

	/**
	 * Creates a set from values.
	 */
	@SafeVarargs
	public static <T> Set<T> setOf(T... values) {
		return addAll(set(), values);
	}

	/**
	 * Creates an empty set.
	 */
	public static <T> Set<T> set() {
		return new HashSet<>();
	}

	/**
	 * Creates an empty identity hash set.
	 */
	public static <T> Set<T> idSet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	/**
	 * Creates an empty tree set with null-first natural comparator.
	 */
	public static <T extends Comparable<? super T>> TreeSet<T> treeSet() {
		return new TreeSet<>(Comparators.nullsFirst());
	}

	/**
	 * Creates a set from array values.
	 */
	public static <T> Set<T> set(T[] array, int offset) {
		return set(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a set from array values.
	 */
	public static <T> Set<T> set(T[] array, int offset, int length) {
		return add(set(), array, offset, length);
	}

	/**
	 * Creates a set from iterable values.
	 */
	public static <T> Set<T> set(Iterable<? extends T> values) {
		return add(set(), values);
	}

	/**
	 * Creates a set from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Set<U> adaptSetOf(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adaptAddAll(set(), mapper, values);
	}

	/**
	 * Creates a set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
		throws E {
		return adaptSet(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return adaptAdd(set(), mapper, array, offset, length);
	}

	/**
	 * Creates a set from transformed iterable values.
	 */
	public static <E extends Exception, T, U> Set<U> adaptSet(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return adaptAdd(set(), mapper, values);
	}

	/**
	 * Creates a set from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> Set<T> convertSet(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return convertAdd(set(), unmapper, map);
	}

	// collections

	/**
	 * Adds values to the collection.
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C addAll(C dest, T... src) {
		if (dest == null || src == null) return dest;
		Collections.addAll(dest, src);
		return dest;
	}

	/**
	 * Adds the array region to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, T[] src, int offset) {
		return add(dest, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Adds the array region to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, T[] src, int offset, int length) {
		if (dest == null || src == null) return dest;
		RawArray.acceptSlice(src, offset, length,
			(o, l) -> dest.addAll(Arrays.asList(src).subList(o, o + l)));
		return dest;
	}

	/**
	 * Adds iterable values to the collection.
	 */
	public static <T, C extends Collection<T>> C add(C dest, Iterable<? extends T> src) {
		if (dest == null || src == null) return dest;
		for (var value : src)
			dest.add(value);
		return dest;
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAddAll(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... src) throws E {
		return adaptAdd(dest, mapper, src, 0);
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] src, int offset)
		throws E {
		return adaptAdd(dest, mapper, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Adds transformed array region values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] src, int offset,
		int length) throws E {
		if (dest == null || src == null || mapper == null) return dest;
		RawArray.acceptSlice(src, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				dest.add(mapper.apply(src[o + i]));
		});
		return dest;
	}

	/**
	 * Adds transformed iterable values to the collection.
	 */
	public static <E extends Exception, T, U, C extends Collection<U>> C adaptAdd(C dest,
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> src)
		throws E {
		if (dest == null || src == null || mapper == null) return dest;
		for (var value : src)
			dest.add(mapper.apply(value));
		return dest;
	}

	/**
	 * Adds transformed map entries to the collection.
	 */
	public static <E extends Exception, K, V, T, C extends Collection<T>> C convertAdd(C dest,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> src)
		throws E {
		if (dest == null || src == null || unmapper == null) return dest;
		for (var e : src.entrySet())
			dest.add(unmapper.apply(e.getKey(), e.getValue()));
		return dest;
	}

	// maps

	/**
	 * Returns a new map builder.
	 */
	public static <K, V> MapBuilder<K, V, Map<K, V>> builder(K key, V value) {
		return new MapBuilder<>(put(map(), key, value));
	}

	/**
	 * Returns a new map builder.
	 */
	public static <K, V, M extends Map<K, V>> MapBuilder<K, V, M> builder(M map) {
		return new MapBuilder<>(map);
	}

	/**
	 * Creates an empty map.
	 */
	public static <K, V> Map<K, V> map() {
		return new HashMap<>();
	}

	/**
	 * Creates an empty tree map with null-first natural comparator.
	 */
	public static <K extends Comparable<? super K>, V> TreeMap<K, V> treeMap() {
		return new TreeMap<>(Comparators.nullsFirst());
	}

	/**
	 * Creates a map from the key and value.
	 */
	public static <K, V> Map<K, V> mapOf(K k, V v) {
		return put(map(), k, v);
	}

	/**
	 * Creates a map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1) {
		return put(map(), k0, v0, k1, v1);
	}

	/**
	 * Creates a map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2) {
		return put(map(), k0, v0, k1, v1, k2, v2);
	}

	/**
	 * Creates a map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		return put(map(), k0, v0, k1, v1, k2, v2, k3, v3);
	}

	/**
	 * Creates a map from keys and values.
	 */
	public static <K, V> Map<K, V> mapOf(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4,
		V v4) {
		return put(map(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
	}

	/**
	 * Creates a map copying the keys and values.
	 */
	public static <K, V> Map<K, V> map(Map<K, V> src) {
		return put(map(), src);
	}

	/**
	 * Creates a map by mapping iterable values to keys.
	 */
	public static <E extends Exception, T, K> Map<K, T> convertMap(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return convertMap(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates a map by mapping iterable values to keys and values.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> convertMap(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return convertPut(map(), keyMapper, valueMapper, iterable);
	}

	/**
	 * Creates a map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> adaptMap(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper, Map<K, V> src) throws E {
		return adaptMap(keyMapper, v -> v, src);
	}

	/**
	 * Creates a map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> adaptMap(
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		return adaptPut(map(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a map by transforming keys.
	 */
	public static <E extends Exception, K, V, T> Map<T, V> biAdaptMap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper, Map<K, V> src)
		throws E {
		return biAdaptMap(keyMapper, (_, v) -> v, src);
	}

	/**
	 * Creates a map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U> Map<T, U> biAdaptMap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		return biAdaptPut(map(), keyMapper, valueMapper, src);
	}

	/**
	 * Creates a map by inverting keys and values.
	 */
	public static <K, V> Map<V, K> invertMap(Map<K, V> src) {
		return putInvert(map(), src);
	}

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
	 * Puts the key and value in the map if absent.
	 */
	public static <K, V, M extends Map<K, V>> M putIfAbsent(M dest, K key, V value) {
		if (dest != null) dest.putIfAbsent(key, value);
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
	 * Puts the keys and values in the map if absent.
	 */
	public static <K, V, M extends Map<K, V>> M putIfAbsent(M dest,
		Map<? extends K, ? extends V> src) {
		if (dest != null && src != null) src.forEach(dest::putIfAbsent);
		return dest;
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
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		RawArray.acceptSlice(src, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				dest.put(keyMapper.apply(src[o + i]), valueMapper.apply(src[o + i]));
		});
		return dest;
	}

	/**
	 * Puts mapped array values into the map.
	 */
	@SafeVarargs
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutIfAbsentAll(
		M dest, Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T... values) throws E {
		return convertPutIfAbsent(dest, keyMapper, valueMapper, values, 0);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutIfAbsent(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset)
		throws E {
		return convertPutIfAbsent(dest, keyMapper, valueMapper, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Puts mapped array region values into the map.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutIfAbsent(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, T[] src, int offset,
		int length) throws E {
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		RawArray.acceptSlice(src, offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				dest.putIfAbsent(keyMapper.apply(src[o + i]), valueMapper.apply(src[o + i]));
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
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		for (var t : src)
			dest.put(keyMapper.apply(t), valueMapper.apply(t));
		return dest;
	}

	/**
	 * Puts mapped iterable values into the map, for absent keys only.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M convertPutIfAbsent(M dest,
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, Iterable<T> src)
		throws E {
		if (dest == null || keyMapper == null || valueMapper == null || src == null) return dest;
		for (var t : src)
			dest.putIfAbsent(keyMapper.apply(t), valueMapper.apply(t));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adaptPut(M dest,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper,
		Map<? extends K, ? extends V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet())
			dest.put(keyMapper.apply(e.getKey()), valueMapper.apply(e.getValue()));
		return dest;
	}

	/**
	 * Copies entries from source to destination map by transforming keys and values, if absent.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M adaptPutIfAbsent(M dest,
		Excepts.Function<? extends E, ? super K, ? extends T> keyMapper,
		Excepts.Function<? extends E, ? super V, ? extends U> valueMapper, Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet())
			dest.putIfAbsent(keyMapper.apply(e.getKey()), valueMapper.apply(e.getValue()));
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
	 * Copies entries from source to destination map by transforming keys and values, if absent.
	 */
	public static <E extends Exception, K, V, T, U, M extends Map<T, U>> M biAdaptPutIfAbsent(
		M dest, Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> keyMapper,
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends U> valueMapper,
		Map<K, V> src) throws E {
		if (src == null || dest == null || keyMapper == null || valueMapper == null) return dest;
		for (var e : src.entrySet()) {
			var k = e.getKey();
			var v = e.getValue();
			dest.putIfAbsent(keyMapper.apply(k, v), valueMapper.apply(k, v));
		}
		return dest;
	}

	/**
	 * Puts inverted keys and values into the map.
	 */
	public static <K, V, M extends Map<V, K>> M putInvert(M dest,
		Map<? extends K, ? extends V> src) {
		return biAdaptPut(dest, (_, v) -> v, (k, _) -> k, src);
	}
}
