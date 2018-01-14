package ceri.common.collection;

import static ceri.common.collection.CollectionUtil.addAll;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods for creating immutable objects.
 */
public class ImmutableUtil {

	private ImmutableUtil() {}

	/**
	 * Creates an immutable iterable wrapper that returns an immutable iterator.
	 */
	public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
		return () -> ImmutableUtil.iterator(iterable.iterator());
	}

	/**
	 * Creates an immutable iterator wrapper.
	 */
	public static <T> Iterator<T> iterator(final Iterator<T> iterator) {
		return new Iterator<T>() {
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
	 * Copies a collection of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		return copyAsSet(set, LinkedHashSet::new);
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
	public static <T> SortedSet<T> copyAsSortedSet(Collection<? extends T> set) {
		return copyAsSortedSet(set, TreeSet::new);
	}

	/**
	 * Copies a collection of objects into an immutable SortedSet.
	 */
	public static <T> SortedSet<T> copyAsSortedSet(Collection<? extends T> set,
		Supplier<SortedSet<T>> supplier) {
		if (set.isEmpty()) return Collections.emptySortedSet();
		SortedSet<T> copy = supplier.get();
		copy.addAll(set);
		return Collections.unmodifiableSortedSet(copy);
	}

	/**
	 * Copies a map of objects into an immutable LinkedHashMap.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map) {
		return copyAsMap(map, LinkedHashMap::new);
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
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, Set<V>>
		copyAsMapOfSets(Map<? extends K, ? extends Collection<? extends V>> map) {
		return copyAsMapOfSets(map, LinkedHashMap::new, LinkedHashSet::new);
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, Set<V>> copyAsMapOfSets(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, Set<V>>> mapSupplier, Supplier<Set<V>> listSupplier) {
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
		return copyAsMapOfLists(map, LinkedHashMap::new, ArrayList::new);
	}

	/**
	 * Copies a map of collections into an immutable map.
	 */
	public static <K, V> Map<K, List<V>> copyAsMapOfLists(
		Map<? extends K, ? extends Collection<? extends V>> map,
		Supplier<Map<K, List<V>>> mapSupplier, Supplier<List<V>> listSupplier) {
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
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> list) {
		return copyAsList(list, ArrayList::new);
	}

	/**
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> list, Supplier<List<T>> supplier) {
		if (list.isEmpty()) return Collections.emptyList();
		List<T> copy = supplier.get();
		copy.addAll(list);
		return Collections.unmodifiableList(copy);
	}

	/**
	 * Copies a stream of objects into an immutable LinkedHashSet.
	 */
	public static <T> Set<T> collectAsSet(Stream<? extends T> stream) {
		Collector<T, ?, LinkedHashSet<T>> collector = Collectors.toCollection(LinkedHashSet::new);
		return Collections.<T>unmodifiableSet(stream.collect(collector));
	}

	/**
	 * Copies a stream of objects into an immutable LinkedHashSet.
	 */
	public static <T> SortedSet<T> collectAsSortedSet(Stream<? extends T> stream) {
		Collector<T, ?, TreeSet<T>> collector = Collectors.toCollection(TreeSet::new);
		return Collections.<T>unmodifiableSortedSet(stream.collect(collector));
	}

	/**
	 * Copies a stream of objects into an immutable ArrayList.
	 */
	public static <T> List<T> collectAsList(Stream<? extends T> stream) {
		Collector<T, ?, List<T>> collector = Collectors.toList();
		return Collections.unmodifiableList(stream.collect(collector));
	}

	/**
	 * Copies an array of objects into an immutable ArrayList.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		if (array.length == 0) return Collections.emptyList();
		List<T> list = new ArrayList<>();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Copies an array of objects into an immutable LinkedHashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		if (array.length == 0) return Collections.emptySet();
		Set<T> set = new LinkedHashSet<>();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
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
	public static <F, T> SortedSet<T> convertAsSortedSet(Function<? super F, ? extends T> fn,
		F... fs) {
		return convertAsSortedSet(fn, Arrays.asList(fs));
	}

	public static <F, T> SortedSet<T> convertAsSortedSet(Function<? super F, ? extends T> fn,
		Iterable<F> fs) {
		return convertAsSortedSet(fn, fs, TreeSet::new);
	}

	public static <F, T> SortedSet<T> convertAsSortedSet(Function<? super F, ? extends T> fn,
		Iterable<F> fs, Supplier<SortedSet<T>> supplier) {
		SortedSet<T> ts = supplier.get();
		for (F f : fs)
			ts.add(fn.apply(f));
		return Collections.unmodifiableSortedSet(ts);
	}

	@SafeVarargs
	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn, T... ts) {
		return convertAsMap(fn, LinkedHashMap::new, ts);
	}

	@SafeVarargs
	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Supplier<Map<K, T>> mapSupplier, T... ts) {
		return convertAsMap(fn, Arrays.asList(ts), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Collection<T> ts) {
		return convertAsMap(fn, ts, LinkedHashMap::new);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Collection<T> ts, Supplier<Map<K, T>> mapSupplier) {
		return convertAsMap(fn, ts.stream(), mapSupplier);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Stream<T> stream) {
		return convertAsMap(fn, stream, LinkedHashMap::new);
	}

	public static <K, T> Map<K, T> convertAsMap(Function<? super T, ? extends K> fn,
		Stream<T> stream, Supplier<Map<K, T>> mapSupplier) {
		return Collections.unmodifiableMap(stream.collect(Collectors.toMap( //
			fn, Function.identity(), StreamUtil.mergeError(), mapSupplier)));
	}

	public static <K, T extends Enum<T>> Map<K, T> enumMap(Function<T, K> fn, Class<T> cls) {
		return convertAsMap(fn, EnumSet.allOf(cls));
	}

	public static <T extends Enum<T>> Set<T> enumSet(T one) {
		return Collections.unmodifiableSet(EnumSet.of(one));
	}

	@SafeVarargs
	public static <T extends Enum<T>> Set<T> enumSet(T first, T... rest) {
		return Collections.unmodifiableSet(EnumSet.of(first, rest));
	}

}
