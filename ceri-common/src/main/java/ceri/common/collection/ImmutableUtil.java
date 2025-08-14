package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.addAll;
import static ceri.common.collection.CollectionUtil.putAll;
import static java.util.function.Function.identity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.array.ArrayUtil;
import ceri.common.stream.StreamUtil;

/**
 * Utility methods for creating immutable objects.
 */
public class ImmutableUtil {
	private static final CollectionSupplier supplier = CollectionSupplier.DEFAULT;

	private ImmutableUtil() {}

	/**
	 * Creates an immutable iterable wrapper that returns an immutable iterator.
	 */
	public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
		return Iterables.of(iterator(iterable.iterator()));
	}

	/**
	 * Creates an immutable iterator wrapper.
	 */
	public static <T> Iterator<T> iterator(final Iterator<T> iterator) {
		return new Iterator<>() { // remove throws an exception by default
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next();
			}
		};
	}

	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Integer> intSet(int... array) {
		return Set.of(ArrayUtil.ints.boxed(array));
	}

	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Long> longSet(long... array) {
		return Set.of(ArrayUtil.longs.boxed(array));
	}

	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Double> doubleSet(double... array) {
		return Set.of(ArrayUtil.doubles.boxed(array));
	}

	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Integer> intList(int... array) {
		return wrapAsList(ArrayUtil.ints.boxed(array));
	}

	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Long> longList(long... array) {
		return wrapAsList(ArrayUtil.longs.boxed(array));
	}

	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Double> doubleList(double... array) {
		return wrapAsList(ArrayUtil.doubles.boxed(array));
	}

	/**
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		return copyAsSet(set, supplier.<T>set());
	}

	/**
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set, Supplier<Set<T>> supplier) {
		if (set.isEmpty()) return Set.of();
		Set<T> copy = supplier.get();
		copy.addAll(set);
		return Collections.unmodifiableSet(copy);
	}

	/**
	 * Copies a collection of objects into an immutable TreeSet.
	 */
	public static <T> NavigableSet<T> copyAsNavigableSet(Collection<? extends T> set) {
		return copyAsNavigableSet(set, TreeSet::new);
	}

	/**
	 * Copies a collection of objects into an immutable NavigableSet.
	 */
	public static <T> NavigableSet<T> copyAsNavigableSet(Collection<? extends T> set,
		Supplier<NavigableSet<T>> supplier) {
		if (set.isEmpty()) return Collections.emptyNavigableSet();
		NavigableSet<T> copy = supplier.get();
		copy.addAll(set);
		return Collections.unmodifiableNavigableSet(copy);
	}

	/**
	 * Copies a map of objects into an immutable LinkedHashMap.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map) {
		return copyAsMap(map, supplier.map());
	}

	/**
	 * Copies a map of objects into an immutable Map.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map,
		Supplier<Map<K, V>> supplier) {
		if (map.isEmpty()) return Map.of();
		Map<K, V> copy = supplier.get();
		copy.putAll(map);
		return Collections.unmodifiableMap(copy);
	}

	/**
	 * Copies a map of objects into an immutable Map.
	 */
	public static <K, V> NavigableMap<K, V> copyAsNavigableMap(Map<? extends K, ? extends V> map) {
		return copyAsNavigableMap(map, supplier.navMap());
	}

	/**
	 * Copies a map of objects into an immutable Map.
	 */
	public static <K, V> NavigableMap<K, V> copyAsNavigableMap(Map<? extends K, ? extends V> map,
		Supplier<NavigableMap<K, V>> supplier) {
		if (map.isEmpty()) return Collections.emptyNavigableMap();
		NavigableMap<K, V> copy = supplier.get();
		copy.putAll(map);
		return Collections.unmodifiableNavigableMap(copy);
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, Set<V>>
		copyAsMapOfSets(Map<? extends K, ? extends Collection<? extends V>> map) {
		return copyAsMapOfSets(map, supplier.map(), supplier.set());
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, Set<V>> copyAsMapOfSets(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, Set<V>>> mapSupplier, Supplier<Set<V>> listSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> set(c, listSupplier), mapSupplier, map));
	}

	private static <T> Set<T> set(Collection<? extends T> collection, Supplier<Set<T>> supplier) {
		if (collection == null) return null;
		return Collections.unmodifiableSet(addAll(supplier.get(), collection));
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, List<V>>
		copyAsMapOfLists(Map<? extends K, ? extends Collection<? extends V>> map) {
		return copyAsMapOfLists(map, supplier.map(), supplier.list());
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, List<V>> copyAsMapOfLists(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, List<V>>> mapSupplier, Supplier<List<V>> listSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> list(c, listSupplier), mapSupplier, map));
	}

	private static <T> List<T> list(Collection<? extends T> collection,
		Supplier<List<T>> supplier) {
		if (collection == null) return null;
		return Collections.unmodifiableList(addAll(supplier.get(), collection));
	}

	/**
	 * Copies a map of maps into an immutable map.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>>
		copyAsMapOfMaps(Map<? extends K1, ? extends Map<? extends K2, ? extends V>> map) {
		return copyAsMapOfMaps(map, supplier.map(), supplier.map());
	}

	/**
	 * Copies a map of maps into an immutable map.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> copyAsMapOfMaps(
		Map<? extends K1, ? extends Map<? extends K2, ? extends V>> map,
		Supplier<Map<K1, Map<K2, V>>> mapSupplier, Supplier<Map<K2, V>> subMapSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			m -> map(m, subMapSupplier), mapSupplier, map));
	}

	private static <K, V> Map<K, V> map(Map<? extends K, ? extends V> map,
		Supplier<Map<K, V>> supplier) {
		if (map == null) return null;
		return Collections.unmodifiableMap(putAll(supplier.get(), map));
	}

	/**
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> collection) {
		return copyAsList(collection, supplier.list());
	}

	/**
	 * Copies a collection of objects into an immutable List.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> collection,
		Supplier<List<T>> supplier) {
		if (collection == null) return null;
		if (collection.isEmpty()) return List.of();
		List<T> copy = supplier.get();
		copy.addAll(collection);
		return Collections.unmodifiableList(copy);
	}

	/**
	 * Reverses a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> reverseAsList(Collection<? extends T> collection) {
		return reverseAsList(collection, supplier.list());
	}

	/**
	 * Reverses a collection of objects into an immutable List.
	 */
	public static <T> List<T> reverseAsList(Collection<? extends T> collection,
		Supplier<List<T>> supplier) {
		if (collection == null) return null;
		if (collection.isEmpty()) return List.of();
		List<T> copy = supplier.get();
		copy.addAll(collection);
		Collections.reverse(copy);
		return Collections.unmodifiableList(copy);
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, Set<V>> asMapOfSets(Map<K, ? extends Set<V>> map) {
		return asMapOfSets(map, supplier.map());
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, Set<V>> asMapOfSets(Map<K, ? extends Set<V>> map,
		Supplier<Map<K, Set<V>>> mapSupplier) {
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> Collections.unmodifiableSet(c), mapSupplier, map));
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, List<V>> asMapOfLists(Map<K, ? extends List<V>> map) {
		return asMapOfLists(map, supplier.map());
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, List<V>> asMapOfLists(Map<K, ? extends List<V>> map,
		Supplier<Map<K, List<V>>> mapSupplier) {
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> Collections.unmodifiableList(c), mapSupplier, map));
	}

	/**
	 * Copies a map of sub-maps into an immutable map, wrapping each sub-map as unmodifiable.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> asMapOfMaps(Map<K1, ? extends Map<K2, V>> map) {
		return asMapOfMaps(map, supplier.map());
	}

	/**
	 * Copies a map of sub-maps into an immutable map, wrapping each sub-map as unmodifiable.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> asMapOfMaps(Map<K1, ? extends Map<K2, V>> map,
		Supplier<Map<K1, Map<K2, V>>> mapSupplier) {
		if (map.isEmpty()) return Map.of();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			m -> Collections.unmodifiableMap(m), mapSupplier, map));
	}

	/**
	 * Collects a objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> collectAsSet(Iterable<T> iterable) {
		return collectAsSet(iterable, supplier.set());
	}

	/**
	 * Collects a objects into an immutable set.
	 */
	public static <T> Set<T> collectAsSet(Iterable<? extends T> iterable,
		Supplier<Set<T>> supplier) {
		return Collections.unmodifiableSet(CollectionUtil.collect(iterable, supplier));
	}

	/**
	 * Collects a objects into an immutable ArrayList.
	 */
	public static <T> List<T> collectAsList(Iterable<T> iterable) {
		return collectAsList(iterable, supplier.list());
	}

	/**
	 * Collects a objects into an immutable list.
	 */
	public static <T> List<T> collectAsList(Iterable<? extends T> iterable,
		Supplier<List<T>> supplier) {
		return Collections.unmodifiableList(CollectionUtil.collect(iterable, supplier));
	}

	/**
	 * Collects a stream of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> collectAsSet(Stream<T> stream) {
		Collector<T, ?, Set<T>> collector = Collectors.toCollection(supplier.set());
		return Collections.unmodifiableSet(stream.collect(collector));
	}

	/**
	 * Collects a stream of objects into an immutable NavigableSet.
	 */
	public static <T> NavigableSet<T> collectAsNavigableSet(Stream<T> stream) {
		Collector<T, ?, TreeSet<T>> collector = Collectors.toCollection(TreeSet::new);
		return Collections.unmodifiableNavigableSet(stream.collect(collector));
	}

	/**
	 * Collects a stream of objects into an immutable ArrayList.
	 */
	public static <T> List<T> collectAsList(Stream<T> stream) {
		Collector<T, ?, List<T>> collector = Collectors.toList();
		return Collections.unmodifiableList(stream.collect(collector));
	}

	/**
	 * Joins collection elements into a single collection.
	 */
	@SafeVarargs
	public static <T> List<T> joinAsList(Collection<? extends T>... collections) {
		return Collections.unmodifiableList(CollectionUtil.joinAsList(collections));
	}

	/**
	 * Joins collection elements into a single collection.
	 */
	@SafeVarargs
	public static <T> Set<T> joinAsSet(Collection<? extends T>... collections) {
		return Collections.unmodifiableSet(CollectionUtil.joinAsSet(collections));
	}

	/**
	 * Wraps a map as unmodifiable, or Map.of() if empty.
	 */
	public static <K, V> Map<K, V> wrapMap(Map<K, V> map) {
		return map.isEmpty() ? Map.of() : Collections.unmodifiableMap(map);
	}

	/**
	 * Wraps a sub-array from start (inclusive) to end (exclusive) as an unmodifiable list.
	 */
	public static <T> List<T> wrapAsList(T[] array, int start, int end) {
		return Collections.unmodifiableList(Arrays.asList(array).subList(start, end));
	}

	/**
	 * Wraps an array as an unmodifiable list.
	 */
	@SafeVarargs
	public static <T> List<T> wrapAsList(T... array) {
		return Collections.unmodifiableList(Arrays.asList(array));
	}

	/**
	 * Copies an array of objects into an immutable ArrayList.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		return asList(supplier.list(), array);
	}

	/**
	 * Copies an array of objects into an immutable list.
	 */
	@SafeVarargs
	public static <T> List<T> asList(Supplier<List<T>> supplier, T... array) {
		if (array.length == 0) return List.of();
		List<T> list = supplier.get();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Copies an array of objects into an immutable LinkedHashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return asSet(supplier.set(), array);
	}

	/**
	 * Copies an array of objects into an immutable set.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(Supplier<Set<T>> supplier, T... array) {
		if (array.length == 0) return Set.of();
		Set<T> set = supplier.get();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K key, V value) {
		return asMap(supplier.map(), key, value);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K key, V value) {
		Map<K, V> map = supplier.get();
		map.put(key, value);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1) {
		return asMap(supplier.map(), k0, v0, k1, v1);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K k0, V v0, K k1, V v1) {
		Map<K, V> map = supplier.get();
		map.put(k0, v0);
		map.put(k1, v1);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2) {
		return asMap(supplier.map(), k0, v0, k1, v1, k2, v2);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K k0, V v0, K k1, V v1, K k2,
		V v2) {
		Map<K, V> map = supplier.get();
		map.put(k0, v0);
		map.put(k1, v1);
		map.put(k2, v2);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3) {
		return asMap(supplier.map(), k0, v0, k1, v1, k2, v2, k3, v3);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K k0, V v0, K k1, V v1, K k2,
		V v2, K k3, V v3) {
		Map<K, V> map = supplier.get();
		map.put(k0, v0);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4,
		V v4) {
		return asMap(supplier.map(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K k0, V v0, K k1, V v1, K k2,
		V v2, K k3, V v3, K k4, V v4) {
		Map<K, V> map = supplier.get();
		map.put(k0, v0);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K k0, V v0, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4,
		K k5, V v5) {
		return asMap(supplier.map(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(Supplier<Map<K, V>> supplier, K k0, V v0, K k1, V v1, K k2,
		V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
		Map<K, V> map = supplier.get();
		map.put(k0, v0);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		return Collections.unmodifiableMap(map);
	}

	@SafeVarargs
	public static <F, T> List<T> convertAsList(Function<? super F, ? extends T> fn, F... fs) {
		return convertAsList(fn, Arrays.asList(fs));
	}

	public static <F, T> List<T> convertAsList(Function<? super F, ? extends T> fn,
		Iterable<F> fs) {
		List<T> ts = new ArrayList<>();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableList(ts);
	}

	@SafeVarargs
	public static <F, T> Set<T> convertAsSet(Function<? super F, ? extends T> fn, F... fs) {
		return convertAsSet(fn, Arrays.asList(fs));
	}

	public static <F, T> Set<T> convertAsSet(Function<? super F, ? extends T> fn, Iterable<F> fs) {
		Set<T> ts = new LinkedHashSet<>();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableSet(ts);
	}

	@SafeVarargs
	public static <F, T> NavigableSet<T> convertAsNavigableSet(Function<? super F, ? extends T> fn,
		F... fs) {
		return convertAsNavigableSet(fn, Arrays.asList(fs));
	}

	public static <F, T> NavigableSet<T> convertAsNavigableSet(Function<? super F, ? extends T> fn,
		Iterable<F> fs) {
		return convertAsNavigableSet(fn, fs, supplier.navSet());
	}

	public static <F, T> NavigableSet<T> convertAsNavigableSet(Function<? super F, ? extends T> fn,
		Iterable<F> fs, Supplier<? extends NavigableSet<T>> supplier) {
		NavigableSet<T> ts = supplier.get();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableNavigableSet(ts);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Collection<T> ts) {
		return convertAsMap(keyFn, ts, supplier.map());
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		BinaryOperator<T> merge, Collection<T> ts) {
		return convertAsMap(keyFn, merge, ts, supplier.map());
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Collection<T> ts, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, ts.stream(), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		BinaryOperator<T> merge, Collection<T> ts, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, merge, ts.stream(), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		BinaryOperator<T> merge, Stream<T> stream) {
		return convertAsMap(keyFn, merge, stream, supplier.map());
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Stream<T> stream, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, identity(), stream, mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		BinaryOperator<T> merge, Stream<T> stream, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, identity(), merge, stream, mapSupplier);
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Collection<T> collection) {
		return convertAsMap(keyFn, valueFn, collection.stream());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, BinaryOperator<V> merge,
		Collection<T> collection) {
		return convertAsMap(keyFn, valueFn, merge, collection, supplier.map());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, BinaryOperator<V> merge, Collection<T> collection,
		Supplier<Map<K, V>> mapSupplier) {
		return convertAsMap(keyFn, valueFn, merge, collection.stream(), mapSupplier);
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Stream<T> stream) {
		return convertAsMap(keyFn, valueFn, stream, supplier.map());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, BinaryOperator<V> merge, Stream<T> stream) {
		return convertAsMap(keyFn, valueFn, merge, stream, supplier.map());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Stream<T> stream,
		Supplier<Map<K, V>> mapSupplier) {
		return convertAsMap(keyFn, valueFn, StreamUtil.mergeError(), stream, mapSupplier);
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, BinaryOperator<V> merge, Stream<T> stream,
		Supplier<Map<K, V>> mapSupplier) {
		return Collections.unmodifiableMap(stream.collect(Collectors.toMap( //
			keyFn, valueFn, merge, mapSupplier)));
	}

	public static <K, V> Map<V, K> invert(Map<K, V> map) {
		return wrapMap(Maps.invert(map));
	}
}
