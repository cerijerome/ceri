package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.addAll;
import static ceri.common.collection.CollectionUtil.listSupplier;
import static ceri.common.collection.CollectionUtil.mapSupplier;
import static ceri.common.collection.CollectionUtil.navigableMapSupplier;
import static ceri.common.collection.CollectionUtil.putAll;
import static ceri.common.collection.CollectionUtil.setSupplier;
import static java.util.function.Function.identity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.util.BasicUtil;
import ceri.common.util.PrimitiveUtil;

/**
 * Utility methods for creating immutable objects.
 */
public class ImmutableUtil {

	private ImmutableUtil() {}

	/**
	 * Creates an immutable iterable wrapper that returns an immutable iterator.
	 */
	public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
		return () -> iterator(iterable.iterator());
	}

	/**
	 * Creates an immutable iterator wrapper.
	 */
	public static <T> Iterator<T> iterator(final Iterator<T> iterator) {
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Iterator is immutable.");
			}
		};
	}

	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Integer> intSet(int... array) {
		return Set.of(PrimitiveUtil.convertInts(array));
	}
	
	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Long> longSet(long... array) {
		return Set.of(PrimitiveUtil.convertLongs(array));
	}
	
	/**
	 * Creates an immutable set from primitives.
	 */
	public static Set<Double> doubleSet(double... array) {
		return Set.of(PrimitiveUtil.convertDoubles(array));
	}
	
	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Integer> intList(int... array) {
		return List.of(PrimitiveUtil.convertInts(array));
	}
	
	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Long> longList(long... array) {
		return List.of(PrimitiveUtil.convertLongs(array));
	}
	
	/**
	 * Creates an immutable list from primitives.
	 */
	public static List<Double> doubleList(double... array) {
		return List.of(PrimitiveUtil.convertDoubles(array));
	}
	
	/**
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		return copyAsSet(set, setSupplier());
	}

	/**
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set, Supplier<Set<T>> supplier) {
		if (set.isEmpty()) return Collections.emptySet();
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
		return copyAsMap(map, mapSupplier());
	}

	/**
	 * Copies a map of objects into an immutable Map.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map,
		Supplier<Map<K, V>> supplier) {
		if (map.isEmpty()) return Collections.emptyMap();
		Map<K, V> copy = supplier.get();
		copy.putAll(map);
		return Collections.unmodifiableMap(copy);
	}

	/**
	 * Copies a map of objects into an immutable Map.
	 */
	public static <K, V> NavigableMap<K, V> copyAsNavigableMap(Map<? extends K, ? extends V> map) {
		return copyAsNavigableMap(map, navigableMapSupplier());
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
		return copyAsMapOfSets(map, mapSupplier(), setSupplier());
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, Set<V>> copyAsMapOfSets(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, Set<V>>> mapSupplier, Supplier<Set<V>> listSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Collections.emptyMap();
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
		return copyAsMapOfLists(map, mapSupplier(), listSupplier());
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, List<V>> copyAsMapOfLists(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, List<V>>> mapSupplier, Supplier<List<V>> listSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Collections.emptyMap();
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
		return copyAsMapOfMaps(map, mapSupplier(), mapSupplier());
	}

	/**
	 * Copies a map of maps into an immutable map.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> copyAsMapOfMaps(
		Map<? extends K1, ? extends Map<? extends K2, ? extends V>> map,
		Supplier<Map<K1, Map<K2, V>>> mapSupplier, Supplier<Map<K2, V>> subMapSupplier) {
		if (map == null) return null;
		if (map.isEmpty()) return Collections.emptyMap();
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
	public static <T> List<T> copyAsList(Collection<? extends T> list) {
		return copyAsList(list, listSupplier());
	}

	/**
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> list, Supplier<List<T>> supplier) {
		if (list == null) return null;
		if (list.isEmpty()) return Collections.emptyList();
		List<T> copy = supplier.get();
		copy.addAll(list);
		return Collections.unmodifiableList(copy);
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, Set<V>> asMapOfSets(Map<K, ? extends Set<V>> map) {
		return asMapOfSets(map, mapSupplier());
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, Set<V>> asMapOfSets(Map<K, ? extends Set<V>> map,
		Supplier<Map<K, Set<V>>> mapSupplier) {
		if (map.isEmpty()) return Collections.emptyMap();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> Collections.unmodifiableSet(c), mapSupplier, map));
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, List<V>> asMapOfLists(Map<K, ? extends List<V>> map) {
		return asMapOfLists(map, mapSupplier());
	}

	/**
	 * Copies a map of collections into an immutable map, wrapping each collection as unmodifiable.
	 */
	public static <K, V> Map<K, List<V>> asMapOfLists(Map<K, ? extends List<V>> map,
		Supplier<Map<K, List<V>>> mapSupplier) {
		if (map.isEmpty()) return Collections.emptyMap();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			c -> Collections.unmodifiableList(c), mapSupplier, map));
	}

	/**
	 * Copies a map of sub-maps into an immutable map, wrapping each sub-map as unmodifiable.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> asMapOfMaps(Map<K1, ? extends Map<K2, V>> map) {
		return asMapOfMaps(map, mapSupplier());
	}

	/**
	 * Copies a map of sub-maps into an immutable map, wrapping each sub-map as unmodifiable.
	 */
	public static <K1, K2, V> Map<K1, Map<K2, V>> asMapOfMaps(Map<K1, ? extends Map<K2, V>> map,
		Supplier<Map<K1, Map<K2, V>>> mapSupplier) {
		if (map.isEmpty()) return Collections.emptyMap();
		return Collections.unmodifiableMap(CollectionUtil.transformValues( //
			m -> Collections.unmodifiableMap(m), mapSupplier, map));
	}

	/**
	 * Collects a stream of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> collectAsSet(Stream<? extends T> stream) {
		Collector<T, ?, Set<T>> collector = Collectors.toCollection(setSupplier());
		return Collections.unmodifiableSet(stream.collect(collector));
	}

	/**
	 * Collects a stream of objects into an immutable NavigableSet.
	 */
	public static <T> NavigableSet<T> collectAsNavigableSet(Stream<? extends T> stream) {
		Collector<T, ?, TreeSet<T>> collector = Collectors.toCollection(TreeSet::new);
		return Collections.unmodifiableNavigableSet(stream.collect(collector));
	}

	/**
	 * Collects a stream of objects into an immutable ArrayList.
	 */
	public static <T> List<T> collectAsList(Stream<? extends T> stream) {
		Collector<T, ?, List<T>> collector = Collectors.toList();
		return Collections.unmodifiableList(stream.collect(collector));
	}

	/**
	 * Collects a stream of objects into an immutable LinkedHashMap.
	 */
	public static <K, V> Map<K, V> collectAsMap(Stream<Map.Entry<K, V>> stream) {
		return Collections.unmodifiableMap(StreamUtil.toEntryMap(stream));
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
		return Collections.unmodifiableList(ArrayUtil.asFixedList(array, start, end));
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
		return asList(listSupplier(), array);
	}

	/**
	 * Copies an array of objects into an immutable list.
	 */
	@SafeVarargs
	public static <T> List<T> asList(Supplier<List<T>> supplier, T... array) {
		if (array.length == 0) return Collections.emptyList();
		List<T> list = supplier.get();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Copies an array of objects into an immutable LinkedHashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return asSet(setSupplier(), array);
	}

	/**
	 * Copies an array of objects into an immutable set.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(Supplier<Set<T>> supplier, T... array) {
		if (array.length == 0) return Collections.emptySet();
		Set<T> set = supplier.get();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Creates an immutable map. Unlike Map.of, keys and values may be null, and keys are not
	 * checked for duplicates.
	 */
	public static <K, V> Map<K, V> asMap(K key, V value) {
		return asMap(mapSupplier(), key, value);
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
		return asMap(mapSupplier(), k0, v0, k1, v1);
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
		return asMap(mapSupplier(), k0, v0, k1, v1, k2, v2);
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
		return asMap(mapSupplier(), k0, v0, k1, v1, k2, v2, k3, v3);
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
		return asMap(mapSupplier(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4);
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
		return asMap(mapSupplier(), k0, v0, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
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
		return convertAsNavigableSet(fn, fs, CollectionUtil.navigableSetSupplier());
	}

	public static <F, T> NavigableSet<T> convertAsNavigableSet(Function<? super F, ? extends T> fn,
		Iterable<F> fs, Supplier<? extends NavigableSet<T>> supplier) {
		NavigableSet<T> ts = supplier.get();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableNavigableSet(ts);
	}

	@SafeVarargs
	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn, T... ts) {
		return convertAsMap(keyFn, mapSupplier(), ts);
	}

	@SafeVarargs
	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Supplier<Map<K, T>> mapSupplier, T... ts) {
		return convertAsMap(keyFn, Arrays.asList(ts), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Collection<T> ts) {
		return convertAsMap(keyFn, ts, mapSupplier());
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Collection<T> ts, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, ts.stream(), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Stream<T> stream) {
		return convertAsMap(keyFn, stream, mapSupplier());
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> keyFn,
		Stream<T> stream, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(keyFn, identity(), stream, mapSupplier);
	}

	@SafeVarargs
	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, T... ts) {
		return convertAsMap(keyFn, valueFn, mapSupplier(), ts);
	}

	@SafeVarargs
	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Supplier<Map<K, V>> mapSupplier, T... ts) {
		return convertAsMap(keyFn, valueFn, Arrays.asList(ts), mapSupplier);
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Collection<T> collection) {
		return convertAsMap(keyFn, valueFn, collection.stream());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Collection<T> collection,
		Supplier<Map<K, V>> mapSupplier) {
		return convertAsMap(keyFn, valueFn, collection.stream(), mapSupplier);
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Stream<T> stream) {
		return convertAsMap(keyFn, valueFn, stream, mapSupplier());
	}

	public static <K, V, T> Map<K, V> convertAsMap(Function<? super T, ? extends K> keyFn,
		Function<? super T, ? extends V> valueFn, Stream<T> stream,
		Supplier<Map<K, V>> mapSupplier) {
		return Collections.unmodifiableMap(stream.collect(Collectors.toMap( //
			keyFn, valueFn, StreamUtil.mergeError(), mapSupplier)));
	}

	public static <K, V> Map<V, K> invert(Map<K, V> map) {
		return invert(map, mapSupplier());
	}

	public static <K, V> Map<V, K> invert(Map<K, V> map, Supplier<Map<V, K>> mapSupplier) {
		return Collections.unmodifiableMap(CollectionUtil.invert(map, mapSupplier));
	}

	public static <K, T extends Enum<T>> Map<K, T> enumMap(Function<T, K> fn, Class<T> cls) {
		return convertAsMap(fn, EnumSet.allOf(cls));
	}

	public static <K, T extends Enum<T>> Map<K, T> enumsMap(Function<T, Collection<K>> fn,
		Class<T> cls) {
		return collectAsMap(StreamUtil.flatInvert(StreamUtil.stream(cls), fn));
	}

	public static <T extends Enum<T>> Set<T> enumSet(T one) {
		return Collections.unmodifiableSet(EnumSet.of(one));
	}

	@SafeVarargs
	public static <T extends Enum<T>> Set<T> enumSet(T first, T... rest) {
		return Collections.unmodifiableSet(EnumSet.of(first, rest));
	}

	public static <T extends Enum<T>> Set<T> enumRange(T first, T last) {
		if (first == null && last == null) return Set.of();
		Class<T> cls = BasicUtil.uncheckedCast(BasicUtil.defaultValue(first, last).getClass());
		T[] ts = cls.getEnumConstants();
		int start = first == null ? 0 : first.ordinal();
		int end = last == null ? ts.length : last.ordinal() + 1;
		return Set.of(Arrays.copyOfRange(ts, start, end));
	}

}
