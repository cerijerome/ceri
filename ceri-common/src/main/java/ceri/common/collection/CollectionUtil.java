package ceri.common.collection;

import static java.util.function.Function.identity;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import ceri.common.comparator.Comparators;
import ceri.common.function.Excepts;
import ceri.common.function.Excepts.BiPredicate;
import ceri.common.function.FunctionWrapper;
import ceri.common.function.Functions.ObjIntFunction;
import ceri.common.function.Predicates;
import ceri.common.stream.StreamUtil;

/**
 * Utility methods to test and manipulate collections.
 */
public class CollectionUtil {
	public static final CollectionSupplier supplier = CollectionSupplier.of();
	
	private CollectionUtil() {}

	/**
	 * Checks if the given map is null or empty.
	 */
	public static boolean empty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Checks if the given map is null or empty.
	 */
	public static boolean nonEmpty(Map<?, ?> map) {
		return !empty(map);
	}

	/**
	 * Checks the given collection is null or empty.
	 */
	public static boolean empty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Checks the given collection is not null and not empty.
	 */
	public static boolean nonEmpty(Collection<?> collection) {
		return !empty(collection);
	}

	/**
	 * Iterates over map entries; allows checked exceptions.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		Excepts.BiConsumer<E, ? super K, ? super V> consumer) throws E {
		for (var entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

	/**
	 * Iterates over entries; allows checked exceptions.
	 */
	public static <E extends Exception, T> void forEach(Iterable<T> iterable,
		Excepts.Consumer<E, T> consumer) throws E {
		for (var entry : iterable)
			consumer.accept(entry);
	}

	/**
	 * Computes and (re-)maps the value for the key.
	 */
	public static <E extends Exception, K, V> V compute(Map<K, V> map, K key,
		Excepts.BiFunction<E, ? super K, ? super V, ? extends V> remapper) throws E {
		var w = FunctionWrapper.<E>of();
		return w.unwrap.get(() -> map.compute(key, wrapBiFn(w, remapper)));
	}

	/**
	 * Computes and adds the value if the key is not mapped.
	 */
	public static <E extends Exception, K, V> V computeIfAbsent(Map<K, V> map, K key,
		Excepts.Function<E, ? super K, ? extends V> mapper) throws E {
		var w = FunctionWrapper.<E>of();
		return w.unwrap.get(() -> map.computeIfAbsent(key, wrapFn(w, mapper)));
	}

	/**
	 * Computes and re-maps the value if the key is not present.
	 */
	public static <E extends Exception, K, V> V computeIfPresent(Map<K, V> map, K key,
		Excepts.BiFunction<E, ? super K, ? super V, ? extends V> remapper) throws E {
		var w = FunctionWrapper.<E>of();
		return w.unwrap.get(() -> map.computeIfPresent(key, wrapBiFn(w, remapper)));
	}

	/**
	 * Applies the function to each map entry.
	 */
	public static <E extends Exception, K, V> void replaceAll(Map<K, V> map,
		Excepts.BiFunction<E, ? super K, ? super V, ? extends V> function) throws E {
		var w = FunctionWrapper.<E>of();
		w.unwrap.run(() -> map.replaceAll(wrapBiFn(w, function)));
	}

	/**
	 * Map the value, or merge the currently mapped value.
	 */
	public static <E extends Exception, K, V> V merge(Map<K, V> map, K key, V value,
		Excepts.BiFunction<E, ? super V, ? super V, ? extends V> remapper) throws E {
		var w = FunctionWrapper.<E>of();
		return w.unwrap.get(() -> map.merge(key, value, wrapBiFn(w, remapper)));
	}

	@SafeVarargs
	public static <T> boolean containsAll(Collection<T> collection, T... allOf) {
		if (collection == null) return false;
		if (allOf == null) return false;
		return collection.containsAll(Arrays.asList(allOf));
	}

	@SafeVarargs
	public static <T> boolean containsAny(Collection<T> collection, T... anyOf) {
		return containsAny(collection, Arrays.asList(anyOf));
	}

	public static <T> boolean containsAny(Collection<T> collection, Collection<T> anyOf) {
		if (collection == null || anyOf == null) return false;
		if (collection.isEmpty() || anyOf.isEmpty()) return false;
		return anyOf.stream().anyMatch(collection::contains);
	}

	/**
	 * Gets element of a list or default value if the index is out of range.
	 */
	public static <T> T getOrDefault(List<? extends T> list, int index, T def) {
		if (list.isEmpty() || index < 0 || index >= list.size()) return def;
		return list.get(index);
	}

	/**
	 * Retrieves and adapts a value for the given key. Returns null if key is not present, or the
	 * value is null.
	 */
	public static <K, V, T> T getAdapted(Map<K, V> map, K key, BiFunction<K, V, T> adapter) {
		V value = map.get(key);
		return value == null ? null : adapter.apply(key, value);
	}

	/**
	 * Copies an array of objects into a mutable LinkedHashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return addAll(supplier.<T>set().get(), array);
	}

	/**
	 * Fills a list with a given number of elements from a given offset. Returns the offset after
	 * the filled elements.
	 */
	public static <T> int fill(List<? super T> list, int offset, int length, T fill) {
		for (int i = 0; i < length; i++)
			list.set(offset + i, fill);
		return offset + length;
	}

	/**
	 * Inserts elements from one list to another. Returns the destination offset after the copied
	 * elements.
	 */
	public static <T> int insert(List<? extends T> from, int srcOffset, List<? super T> to,
		int destOffset, int length) {
		for (int i = 0; i < length; i++)
			to.add(destOffset + i, from.get(srcOffset + i));
		return destOffset + length;
	}

	/**
	 * Copies elements from one list to another. Returns the destination offset after the copied
	 * elements.
	 */
	public static <T> int copy(List<? extends T> from, int srcOffset, List<? super T> to,
		int destOffset, int length) {
		for (int i = 0; i < length; i++)
			to.set(destOffset + i, from.get(srcOffset + i));
		return destOffset + length;
	}

	/**
	 * Inverts keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> map) {
		return invert(map, supplier.map());
	}

	/**
	 * Inverts keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> map, Supplier<Map<V, K>> supplier) {
		return transform((_, v) -> v, (k, _) -> k, supplier, map);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, T> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper,
		Collection<T> collection) {
		return toMap(keyMapper, supplier.map(), collection);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, T> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper,
		Supplier<Map<K, T>> mapSupplier, Collection<T> collection) {
		return toMap(keyMapper, identity(), mapSupplier, collection);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, V, T> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
		Function<? super T, ? extends V> valueMapper, Collection<T> collection) {
		return toMap(keyMapper, valueMapper, supplier.map(), collection);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, V, T> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
		Function<? super T, ? extends V> valueMapper, Supplier<Map<K, V>> mapSupplier,
		Collection<T> collection) {
		return StreamUtil.toMap(collection.stream(), keyMapper, valueMapper, mapSupplier);
	}

	/**
	 * Converts a collection to a map by index.
	 */
	public static <T> Map<Integer, T> toIndexMap(Iterable<T> collection) {
		return toIndexMap(supplier.map(), collection);
	}

	/**
	 * Converts a collection to a map by index.
	 */
	public static <T> Map<Integer, T> toIndexMap(Supplier<Map<Integer, T>> mapSupplier,
		Iterable<T> collection) {
		return toIndexMap((t, _) -> t, mapSupplier, collection);
	}

	/**
	 * Converts a collection to a map by index.
	 */
	public static <V, T> Map<Integer, V>
		toIndexMap(ObjIntFunction<? super T, ? extends V> valueMapper, Iterable<T> collection) {
		return toIndexMap(valueMapper, supplier.map(), collection);
	}

	/**
	 * Converts a collection to a map by index.
	 */
	public static <V, T> Map<Integer, V> toIndexMap(
		ObjIntFunction<? super T, ? extends V> valueMapper, Supplier<Map<Integer, V>> mapSupplier,
		Iterable<T> collection) {
		var map = mapSupplier.get();
		var iter = collection.iterator();
		for (int i = 0; iter.hasNext(); i++)
			map.put(i, valueMapper.apply(iter.next(), i));
		return map;
	}

	/**
	 * Transforms a map.
	 */
	public static <K, V, T, U> Map<K, V> transform(Function<? super T, ? extends K> keyMapper,
		Function<? super U, ? extends V> valueMapper, Map<T, U> map) {
		return transform(keyMapper, valueMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map.
	 */
	public static <K, V, T, U> Map<K, V> transform(Function<? super T, ? extends K> keyMapper,
		Function<? super U, ? extends V> valueMapper, Supplier<Map<K, V>> mapSupplier,
		Map<T, U> map) {
		return StreamUtil.toMap(map.entrySet().stream(), e -> keyMapper.apply(e.getKey()),
			e -> valueMapper.apply(e.getValue()), mapSupplier);
	}

	/**
	 * Transforms a map.
	 */
	public static <K, V, T, U> Map<K, V> transform(
		BiFunction<? super T, ? super U, ? extends K> keyMapper,
		BiFunction<? super T, ? super U, ? extends V> valueMapper, Map<T, U> map) {
		return transform(keyMapper, valueMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map.
	 */
	public static <K, V, T, U> Map<K, V> transform(
		BiFunction<? super T, ? super U, ? extends K> keyMapper,
		BiFunction<? super T, ? super U, ? extends V> valueMapper, Supplier<Map<K, V>> mapSupplier,
		Map<T, U> map) {
		return StreamUtil.toMap(map.entrySet().stream(),
			e -> keyMapper.apply(e.getKey(), e.getValue()),
			e -> valueMapper.apply(e.getKey(), e.getValue()), mapSupplier);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, T, U> Map<K, U> transformKeys(Function<? super T, ? extends K> keyMapper,
		Map<T, U> map) {
		return transformKeys(keyMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, T, U> Map<K, U> transformKeys(Function<? super T, ? extends K> keyMapper,
		Supplier<Map<K, U>> mapSupplier, Map<T, U> map) {
		return transform(keyMapper, v -> v, mapSupplier, map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, T, U> Map<K, U>
		transformKeys(BiFunction<? super T, ? super U, ? extends K> keyMapper, Map<T, U> map) {
		return transformKeys(keyMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, T, U> Map<K, U> transformKeys(
		BiFunction<? super T, ? super U, ? extends K> keyMapper, Supplier<Map<K, U>> mapSupplier,
		Map<T, ? extends U> map) {
		return transform(keyMapper, (_, v) -> v, mapSupplier, map);
	}

	/**
	 * Transforms a map's values.
	 */
	public static <K, V, U> Map<K, V> transformValues(Function<? super U, ? extends V> valueMapper,
		Map<? extends K, U> map) {
		return transformValues(valueMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map's values.
	 */
	public static <K, V, U> Map<K, V> transformValues(Function<? super U, ? extends V> valueMapper,
		Supplier<Map<K, V>> mapSupplier, Map<? extends K, U> map) {
		return transform(k -> k, valueMapper, mapSupplier, map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, V, U> Map<K, V> transformValues(
		BiFunction<? super K, ? super U, ? extends V> valueMapper, Map<? extends K, U> map) {
		return transformValues(valueMapper, supplier.map(), map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, V, U> Map<K, V> transformValues(
		BiFunction<? super K, ? super U, ? extends V> valueMapper, Supplier<Map<K, V>> mapSupplier,
		Map<? extends K, U> map) {
		return transform((k, _) -> k, valueMapper, mapSupplier, map);
	}

	/**
	 * Converts a collection to a new list by mapping elements from the original collection.
	 */
	public static <F, T> List<T> toList(Function<? super F, ? extends T> mapper,
		Collection<? extends F> collection) {
		return StreamUtil.toList(collection.stream().map(mapper));
	}

	/**
	 * Converts a map to a list by mapping entry elements from the original map.
	 */
	public static <K, V, T> List<T> toList(BiFunction<? super K, ? super V, ? extends T> mapper,
		Map<? extends K, ? extends V> map) {
		return StreamUtil.toList(StreamUtil.stream(map, mapper));
	}

	/**
	 * Creates a list joining the given elements.
	 */
	public static <T> List<T> joinAsList(T t, Collection<? extends T> collection) {
		return joinAs(t, collection, supplier.list());
	}

	/**
	 * Creates a set joining the given elements.
	 */
	public static <T> Set<T> joinAsSet(T t, Collection<? extends T> collection) {
		return joinAs(t, collection, supplier.set());
	}

	/**
	 * Creates a collection joining the given elements.
	 */
	public static <T, C extends Collection<T>> C joinAs(T t, Collection<? extends T> collection,
		Supplier<C> supplier) {
		return joinAs(supplier, Arrays.asList(t), collection);
	}

	/**
	 * Joins collection elements into a single collection.
	 */
	@SafeVarargs
	public static <T> List<T> joinAsList(Collection<? extends T>... collections) {
		return joinAs(supplier.list(), collections);
	}

	/**
	 * Joins collection elements into a single collection.
	 */
	@SafeVarargs
	public static <T> Set<T> joinAsSet(Collection<? extends T>... collections) {
		return joinAs(supplier.set(), collections);
	}

	/**
	 * Joins collection elements into a single collection.
	 */
	@SafeVarargs
	public static <T, C extends Collection<T>> C joinAs(Supplier<C> supplier,
		Collection<? extends T>... collections) {
		return collect(StreamUtil.streamAll(collections), supplier);
	}

	/**
	 * Returns a linked hash map copy with entries sorted by value.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		var result = supplier.<K, V>map().get();
		Stream<Entry<K, V>> stream = map.entrySet().stream();
		Comparator<V> comparator = Comparators.comparable();
		Comparator<Entry<K, V>> entryComparator =
			(e1, e2) -> comparator.compare(e1.getValue(), e2.getValue());
		stream.sorted(entryComparator).forEach(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	/**
	 * Reverses the items in a mutable list and returns the given reference.
	 */
	public static <T> List<T> reverse(List<T> ts) {
		if (ts != null) Collections.reverse(ts);
		return ts;
	}

	/**
	 * Collects objects using the given collection supplier.
	 */
	public static <T, C extends Collection<? super T>> C collect(Iterable<? extends T> iterable,
		Supplier<C> supplier) {
		var collection = supplier.get();
		iterable.forEach(collection::add);
		return collection;
	}

	/**
	 * Collects objects using the given collection supplier.
	 */
	public static <T, C extends Collection<? super T>> C collect(Stream<? extends T> stream,
		Supplier<C> supplier) {
		var collection = supplier.get();
		stream.forEach(collection::add);
		return collection;
	}

	/**
	 * Variation of Collection.addAll that returns the collection type.
	 */
	@SafeVarargs
	public static <T, C extends Collection<? super T>> C addAll(C collection, T... items) {
		Collections.addAll(collection, items);
		return collection;
	}

	/**
	 * Variation of Collection.addAll that returns the collection type.
	 */
	public static <T, C extends Collection<? super T>> C addAll(C collection,
		Collection<? extends T> items) {
		collection.addAll(items);
		return collection;
	}

	/**
	 * Variation of Map.putAll that returns the map type.
	 */
	public static <K, V, M extends Map<? super K, ? super V>> M putAll(M map,
		Map<? extends K, ? extends V> items) {
		map.putAll(items);
		return map;
	}

	/**
	 * Returns the last key, or null if empty.
	 */
	public static <T> T lastKey(SortedMap<T, ?> map) {
		return lastKey(map, null);
	}

	/**
	 * Returns the last key, or def if empty.
	 */
	public static <T> T lastKey(SortedMap<T, ?> map, T def) {
		return map.isEmpty() ? def : map.lastKey();
	}

	/**
	 * Returns the last element, or null if no elements.
	 */
	public static <T> T last(Iterable<T> iterable) {
		if (iterable == null) return null;
		if (iterable instanceof List<T> list) {
			if (list.isEmpty()) return null;
			return list.get(list.size() - 1);
		}
		T last = null;
		for (T t : iterable)
			last = t;
		return last;
	}

	/**
	 * Returns the first element, or null if no elements.
	 */
	public static <T> T first(Iterable<T> iterable) {
		if (iterable == null) return null;
		var i = iterable.iterator();
		return i.hasNext() ? i.next() : null;
	}

	/**
	 * Returns the element at index based on the set iterator, or null if no element.
	 */
	public static <T> T get(int index, Set<T> set) {
		if (set == null || set.size() <= index) return null;
		return IteratorUtil.nth(set.iterator(), index);
	}

	/**
	 * Removes all given items from the collection. Returns true if the collection is modified.
	 */
	@SafeVarargs
	public static <T> boolean removeAll(Collection<? super T> collection, T... ts) {
		return collection.removeAll(Arrays.asList(ts));
	}

	/**
	 * Removes collection entries that match the predicate. Returns the removed entry count.
	 */
	public static <E extends Exception, T> int removeIf(Collection<T> collection,
		Excepts.Predicate<? extends E, ? super T> predicate) throws E {
		return IteratorUtil.removeIf(collection.iterator(), predicate);
	}

	/**
	 * Removes map entries that match the predicate. Returns the removed entry count.
	 */
	public static <E extends Exception, K, V> int removeIf(Map<K, V> map,
		BiPredicate<E, K, V> predicate) throws E {
		return removeIf(map.entrySet(), Predicates.testingMapEntry(predicate));
	}

	/**
	 * Removes items from the first collection that are not in the second.
	 */
	public static void intersect(Collection<?> lhs, Collection<?> rhs) {
		lhs.removeIf(o -> !rhs.contains(o));
	}

	/**
	 * Creates a typed array from a collection and given type. The type must not be a primitive
	 * class type such as int.class otherwise a ClassCastException will be thrown.
	 */
	public static <T> T[] toArray(Collection<? extends T> collection, Class<T> type) {
		if (type.isPrimitive()) throw new IllegalArgumentException("Primitives types not allowed");
		T[] array = ArrayUtil.create(type, collection.size());
		return collection.toArray(array);
	}

	/**
	 * Finds the first key with matching value.
	 */
	public static <K, V> K key(Map<K, V> map, V value) {
		for (var entry : map.entrySet()) {
			if (entry.getValue() == null && value != null) continue;
			if (entry.getValue() == value || entry.getValue().equals(value)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Finds all keys with matching value.
	 */
	public static <K, V> Collection<K> keys(Map<K, V> map, V value) {
		var keys = supplier.<K>set().get();
		for (var entry : map.entrySet()) {
			if (entry.getValue() == null && value != null) continue;
			if (entry.getValue() == value || entry.getValue().equals(value))
				keys.add(entry.getKey());
		}
		return keys;
	}

	/**
	 * Creates an identity hash set backed by a map.
	 */
	public static <T> Set<T> identityHashSet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	/**
	 * Creates an identity hash set, backed by a map, and copying from the given set.
	 */
	public static <T> Set<T> identityHashSet(Set<? extends T> set) {
		var copy = CollectionUtil.<T>identityHashSet();
		copy.addAll(set);
		return copy;
	}

	/**
	 * Returns a map with a maximum number of keys, for caching. When full, a new added entry will
	 * cause the oldest entry to be removed.
	 */
	@SuppressWarnings("serial")
	public static <K, V> LinkedHashMap<K, V> fixedSizeCache(int max) {
		return new LinkedHashMap<>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > max;
			}
		};
	}

	// Support methods

	private static <E extends Exception, K, V> Function<K, V> wrapFn(FunctionWrapper<E> w,
		Excepts.Function<E, ? super K, ? extends V> fn) {
		return k -> w.wrap.get(() -> fn.apply(k));
	}

	private static <E extends Exception, K, V> BiFunction<K, V, V> wrapBiFn(FunctionWrapper<E> w,
		Excepts.BiFunction<E, ? super K, ? super V, ? extends V> fn) {
		return (k, v) -> w.wrap.get(() -> fn.apply(k, v));
	}
}
