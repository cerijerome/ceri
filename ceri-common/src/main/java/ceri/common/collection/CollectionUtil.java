package ceri.common.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;

/**
 * Utility methods to test and manipulate collections.
 */
public class CollectionUtil {

	private CollectionUtil() {}

	/**
	 * Copies an array of objects into an mutable LinkedHashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return addAll(new LinkedHashSet<>(), array);
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
	 * Converts a collection to a map.
	 */
	public static <K, T> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper,
		Collection<T> collection) {
		return StreamUtil.toMap(collection.stream(), keyMapper);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, T> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper,
		Supplier<Map<K, T>> mapSupplier, Collection<T> collection) {
		return StreamUtil.toMap(collection.stream(), keyMapper, mapSupplier);
	}

	/**
	 * Converts a collection to a map.
	 */
	public static <K, V, T> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
		Function<? super T, ? extends V> valueMapper, Collection<T> collection) {
		return StreamUtil.toMap(collection.stream(), keyMapper, valueMapper);
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
		return transform(keyMapper, valueMapper, LinkedHashMap::new, map);
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
		return transform(keyMapper, valueMapper, LinkedHashMap::new, map);
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
		return transformKeys(keyMapper, LinkedHashMap::new, map);
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
		return transformKeys(keyMapper, LinkedHashMap::new, map);
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
		return transformValues(valueMapper, LinkedHashMap::new, map);
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
		return transformValues(valueMapper, LinkedHashMap::new, map);
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
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Entry<K, V>> stream = map.entrySet().stream();
		Comparator<V> comparator = Comparators.comparable();
		Comparator<Entry<K, V>> entryComparator =
			(e1, e2) -> comparator.compare(e1.getValue(), e2.getValue());
		stream.sorted(entryComparator).forEach(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	/**
	 * Allows an enumeration to be run in a for-each loop.
	 */
	public static final <T> Iterable<T> iterable(final Enumeration<? extends T> enumeration) {
		return BasicUtil.forEach(iterator(enumeration));
	}

	/**
	 * Returns an iterator for an enumeration.
	 */
	public static final <T> Iterator<T> iterator(final Enumeration<? extends T> enumeration) {
		return new Iterator<>() {
			@Override
			public final boolean hasNext() {
				return enumeration.hasMoreElements();
			}

			@Override
			public final T next() {
				return enumeration.nextElement();
			}

			@Override
			public final void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Returns a reversed list iterator.
	 */
	public static <T> Iterator<T> reverseListIterator(List<T> list) {
		final ListIterator<T> listIterator = list.listIterator(list.size());
		return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return listIterator.hasPrevious();
			}

			@Override
			public T next() {
				return listIterator.previous();
			}

			@Override
			public void remove() {
				listIterator.remove();
			}
		};
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> reverseIterableList(final List<T> list) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return reverseListIterator(list);
			}
		};
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> reverseIterableQueue(final Deque<T> deque) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return deque.descendingIterator();
			}
		};
	}

	/**
	 * Returns a reverse iterable type useful in for-each loops.
	 */
	public static <T> Iterable<T> reverseIterableSet(final NavigableSet<T> navSet) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return navSet.descendingIterator();
			}
		};
	}

	/**
	 * Map wrapper that returns a default value if key is not in map or its value is null.
	 */
	public static <K, V> Map<K, V> defaultValueMap(Map<K, V> map, final V def) {
		return new DelegatingMap<>(map) {
			@Override
			public V get(Object key) {
				V value = super.get(key);
				if (value != null) return value;
				return def;
			}
		};
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
		Iterator<T> i = iterable.iterator();
		while (i.hasNext())
			last = i.next();
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
	 * Removes all given items from the collection. Returns true if the collection is modified.
	 */
	@SafeVarargs
	public static <T> boolean removeAll(Collection<? super T> collection, T... ts) {
		return collection.removeAll(Arrays.asList(ts));
	}

	/**
	 * Removes items from the first collection that are not in the second.
	 */
	public static void intersect(Collection<?> lhs, Collection<?> rhs) {
		for (Iterator<?> i = lhs.iterator(); i.hasNext();) {
			if (!rhs.contains(i.next())) i.remove();
		}
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
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue() == null && value != null) continue;
			if (entry.getValue() == value || entry.getValue().equals(value)) return entry.getKey();
		}
		return null;
	}

	/**
	 * Finds all keys with matching value.
	 */
	public static <K, V> Collection<K> keys(Map<K, V> map, V value) {
		Set<K> keys = new LinkedHashSet<>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (entry.getValue() == null && value != null) continue;
			if (entry.getValue() == value || entry.getValue().equals(value))
				keys.add(entry.getKey());
		}
		return keys;
	}

}
