package ceri.common.collection;

import static java.util.function.Function.identity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import ceri.common.comparator.Comparators;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionBiFunction;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.FunctionWrapper;
import ceri.common.util.BasicUtil;

/**
 * Utility methods to test and manipulate collections.
 */
public class CollectionUtil {
	private static final Supplier<Map<?, ?>> mapSupplier = LinkedHashMap::new;
	private static final Supplier<NavigableMap<?, ?>> navigableMapSupplier = TreeMap::new;
	private static final Supplier<Set<?>> setSupplier = LinkedHashSet::new;
	private static final Supplier<NavigableSet<?>> navigableSetSupplier = TreeSet::new;
	private static final Supplier<List<?>> listSupplier = ArrayList::new;

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
	 * Checks if the given collection is null or empty.
	 */
	public static boolean empty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Checks if the given collection is null or empty.
	 */
	public static boolean nonEmpty(Collection<?> collection) {
		return !empty(collection);
	}

	/**
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Iterates over map entries; allows checked exceptions.
	 */
	public static <E extends Exception, K, V> void forEach(Map<K, V> map,
		ExceptionBiConsumer<E, K, V> consumer) throws E {
		for (var entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

	/**
	 * Computes and (re-)maps the value if for the key.
	 */
	public static <E extends Exception, K, V> V compute(Map<K, V> map, K key,
		ExceptionBiFunction<E, ? super K, ? super V, ? extends V> remappingFunction) throws E {
		FunctionWrapper<E> w = FunctionWrapper.of();
		return w.unwrapSupplier(() -> map.compute(key, w.wrap(remappingFunction)));
	}

	/**
	 * Computes and adds the value if the key is not mapped.
	 */
	public static <E extends Exception, K, V> V computeIfAbsent(Map<K, V> map, K key,
		ExceptionFunction<E, ? super K, ? extends V> mappingFunction) throws E {
		FunctionWrapper<E> w = FunctionWrapper.of();
		return w.unwrapSupplier(() -> map.computeIfAbsent(key, w.wrap(mappingFunction)));
	}

	/**
	 * Computes and re-maps the value if the key is not present.
	 */
	public static <E extends Exception, K, V> V computeIfPresent(Map<K, V> map, K key,
		ExceptionBiFunction<E, ? super K, ? super V, ? extends V> remappingFunction) throws E {
		FunctionWrapper<E> w = FunctionWrapper.of();
		return w.unwrapSupplier(() -> map.computeIfPresent(key, w.wrap(remappingFunction)));
	}

	/**
	 * Applies the function to each map entry.
	 */
	public static <E extends Exception, K, V> void reaplaceAll(Map<K, V> map,
		ExceptionBiFunction<E, ? super K, ? super V, ? extends V> function) throws E {
		FunctionWrapper<E> w = FunctionWrapper.of();
		w.unwrap(() -> map.replaceAll(w.wrap(function)));
	}

	/**
	 * Map the value, or merge the currently mapped value.
	 */
	public static <E extends Exception, K, V> V merge(Map<K, V> map, K key, V value,
		ExceptionBiFunction<E, ? super V, ? super V, ? extends V> remappingFunction) throws E {
		FunctionWrapper<E> w = FunctionWrapper.of();
		return w.unwrapSupplier(() -> map.merge(key, value, w.wrap(remappingFunction)));
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
	 * Gets element of a list or default value if the index does not exist.
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
		return addAll(CollectionUtil.<T>setSupplier().get(), array);
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
		return invert(map, mapSupplier());
	}

	/**
	 * Inverts keys and values.
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> map, Supplier<Map<V, K>> supplier) {
		return transform((k, v) -> v, (k, v) -> k, supplier, map);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, T> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper,
		Collection<T> collection) {
		return toMap(keyMapper, mapSupplier(), collection);
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
		return toMap(keyMapper, valueMapper, mapSupplier(), collection);
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
	 * Transforms a map.
	 */
	public static <K, V, T, U> Map<K, V> transform(Function<? super T, ? extends K> keyMapper,
		Function<? super U, ? extends V> valueMapper, Map<T, U> map) {
		return transform(keyMapper, valueMapper, mapSupplier(), map);
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
		return transform(keyMapper, valueMapper, mapSupplier(), map);
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
		return transformKeys(keyMapper, mapSupplier(), map);
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
		return transformKeys(keyMapper, mapSupplier(), map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, T, U> Map<K, U> transformKeys(
		BiFunction<? super T, ? super U, ? extends K> keyMapper, Supplier<Map<K, U>> mapSupplier,
		Map<T, ? extends U> map) {
		return transform(keyMapper, (k, v) -> v, mapSupplier, map);
	}

	/**
	 * Transforms a map's values.
	 */
	public static <K, V, U> Map<K, V> transformValues(Function<? super U, ? extends V> valueMapper,
		Map<? extends K, U> map) {
		return transformValues(valueMapper, mapSupplier(), map);
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
		return transformValues(valueMapper, mapSupplier(), map);
	}

	/**
	 * Transforms a map's keys.
	 */
	public static <K, V, U> Map<K, V> transformValues(
		BiFunction<? super K, ? super U, ? extends V> valueMapper, Supplier<Map<K, V>> mapSupplier,
		Map<? extends K, U> map) {
		return transform((k, v) -> k, valueMapper, mapSupplier, map);
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
	 * Returns a linked hash map copy with entries sorted by value.
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		Map<K, V> result = CollectionUtil.<K, V>mapSupplier().get();
		Stream<Entry<K, V>> stream = map.entrySet().stream();
		Comparator<V> comparator = Comparators.comparable();
		Comparator<Entry<K, V>> entryComparator =
			(e1, e2) -> comparator.compare(e1.getValue(), e2.getValue());
		stream.sorted(entryComparator).forEach(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	/**
	 * Reverses the items in a list and returns the given reference.
	 */
	public static <T> List<T> reverse(List<T> ts) {
		Collections.reverse(ts);
		return ts;
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
	 * Returns the last element.
	 */
	public static <T> T last(Iterable<T> iterable) {
		if (iterable == null) return null;
		if (iterable instanceof List) {
			List<T> list = BasicUtil.uncheckedCast(iterable);
			if (list.isEmpty()) return null;
			return list.get(list.size() - 1);
		}
		T last = null;
		for (T t : iterable)
			last = t;
		return last;
	}

	/**
	 * Returns the first element.
	 */
	public static <T> T first(Iterable<T> iterable) {
		if (iterable == null) return null;
		Iterator<T> i = iterable.iterator();
		if (!i.hasNext()) return null;
		return i.next();
	}

	/**
	 * Returns the element at index based on the set iterator.
	 */
	public static <T> T get(int index, Set<T> set) {
		if (set == null || index < 0 || set.size() <= index) return null;
		Iterator<T> i = set.iterator();
		while (index-- > 0)
			i.next();
		return i.next();
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
	public static <T> int removeIf(Collection<T> collection, Predicate<T> predicate) {
		int n = 0;
		for (var i = collection.iterator(); i.hasNext();) {
			if (!predicate.test(i.next())) continue;
			i.remove();
			n++;
		}
		return n;
	}

	/**
	 * Removes map entries that match the predicate. Returns the removed entry count.
	 */
	public static <K, V> int removeIf(Map<K, V> map, BiPredicate<K, V> predicate) {
		int n = 0;
		for (var i = map.entrySet().iterator(); i.hasNext();) {
			var next = i.next();
			if (!predicate.test(next.getKey(), next.getValue())) continue;
			i.remove();
			n++;
		}
		return n;
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
		Set<K> keys = CollectionUtil.<K>setSupplier().get();
		for (var entry : map.entrySet()) {
			if (entry.getValue() == null && value != null) continue;
			if (entry.getValue() == value || entry.getValue().equals(value))
				keys.add(entry.getKey());
		}
		return keys;
	}

	public static <K, V> Supplier<Map<K, V>> mapSupplier() {
		return BasicUtil.uncheckedCast(mapSupplier);
	}

	public static <K, V> Supplier<NavigableMap<K, V>> navigableMapSupplier() {
		return BasicUtil.uncheckedCast(navigableMapSupplier);
	}

	public static <T> Supplier<Set<T>> setSupplier() {
		return BasicUtil.uncheckedCast(setSupplier);
	}

	public static <T> Supplier<NavigableSet<T>> navigableSetSupplier() {
		return BasicUtil.uncheckedCast(navigableSetSupplier);
	}

	public static <T> Supplier<List<T>> listSupplier() {
		return BasicUtil.uncheckedCast(listSupplier);
	}

}
