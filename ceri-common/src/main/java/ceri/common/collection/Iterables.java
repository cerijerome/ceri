package ceri.common.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.BasicUtil;

/**
 * Iterable type support.
 */
public class Iterables {
	/** A no-op stateless instance. */
	public static final Iterable<Object> NULL = () -> Iterators.NULL;

	private Iterables() {}

	/**
	 * Returns a no-op iterable.
	 */
	public static <T> Iterable<T> ofNull() {
		return BasicUtil.unchecked(NULL);
	}

	/**
	 * Converts an iterator into a single-use iterable type.
	 */
	public static <T> Iterable<T> of(Iterator<T> iterator) {
		return iterator == null ? ofNull() : () -> iterator;
	}

	/**
	 * Returns the first element, or null if no elements.
	 */
	public static <T> T first(Iterable<T> iterable) {
		return iterable == null ? null : Iterators.next(iterable.iterator());
	}

	/**
	 * Calls the consumer for each element and returns the element count.
	 */
	public static <E extends Exception, T> int forEach(Iterable<T> iterable,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		if (iterable == null) return 0;
		return Iterators.forEach(iterable.iterator(), consumer);
	}

	/**
	 * Removes elements that match the predicate, and returns the number of removed elements.
	 */
	public static <E extends Exception, T> int removeIf(Iterable<T> iterable,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		if (iterable == null || predicate == null) return 0;
		int n = 0;
		for (var iterator = iterable.iterator(); iterator.hasNext();) {
			if (!predicate.test(iterator.next())) continue;
			iterator.remove();
			n++;
		}
		return n;
	}

	// collections

	/**
	 * Adds elements to a collection.
	 */
	public static <T, C extends Collection<T>> C add(C collection,
		Iterable<? extends T> iterable) {
		for (var t : iterable)
			collection.add(t);
		return collection;
	}

	/**
	 * Creates a set from elements.
	 */
	public static <T> Set<T>
		set(Iterable<? extends T> iterable) {
		return collect(CollectionUtil.supplier.set(), iterable);
	}

	/**
	 * Creates a set from elements.
	 */
	public static <T> List<T>
		list(Iterable<? extends T> iterable) {
		return collect(CollectionUtil.supplier.list(), iterable);
	}

	/**
	 * Creates a collection from elements.
	 */
	public static <T, C extends Collection<T>> C
		collect(Functions.Supplier<C> collectionSupplier, Iterable<? extends T> iterable) {
		return add(collectionSupplier.get(), iterable);
	}

	// map modification

	/**
	 * Adds to a map by mapping each element to a key.
	 */
	public static <E extends Exception, T, K, M extends Map<K, T>> M put(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper, M map,
		Iterable<? extends T> iterable) throws E {
		return put(keyMapper, t -> t, map, iterable);
	}

	/**
	 * Adds to a map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M put(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, M map,
		Iterable<? extends T> iterable) throws E {
		for (var t : iterable)
			map.put(keyMapper.apply(t), valueMapper.apply(t));
		return map;
	}

	/**
	 * Adds to a map by mapping each element to a key, if absent.
	 */
	public static <E extends Exception, T, K, M extends Map<K, T>> M putIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper, M map,
		Iterable<? extends T> iterable) throws E {
		return putIfAbsent(keyMapper, t -> t, map, iterable);
	}

	/**
	 * Adds to a map by mapping each element to a key and value, if absent.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M putIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper, M map,
		Iterable<? extends T> iterable) throws E {
		for (var t : iterable)
			map.putIfAbsent(keyMapper.apply(t), valueMapper.apply(t));
		return map;
	}

	// map creation

	/**
	 * Creates a mutable map by mapping each element to a key.
	 */
	public static <E extends Exception, T, K> Map<K, T> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return map(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates a mutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return map(keyMapper, valueMapper, CollectionUtil.supplier.map(), iterable);
	}

	/**
	 * Creates a mutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M map(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Functions.Supplier<M> mapSupplier, Iterable<? extends T> iterable) throws E {
		return put(keyMapper, valueMapper, mapSupplier.get(), iterable);
	}

	/**
	 * Creates a mutable map by mapping each element to a key.
	 */
	public static <E extends Exception, T, K> Map<K, T> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Iterable<? extends T> iterable) throws E {
		return mapIfAbsent(keyMapper, t -> t, iterable);
	}

	/**
	 * Creates a mutable map by mapping each element to a key and value.
	 */
	public static <E extends Exception, T, K, V> Map<K, V> mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Iterable<? extends T> iterable) throws E {
		return mapIfAbsent(keyMapper, valueMapper, CollectionUtil.supplier.map(), iterable);
	}

	/**
	 * Creates a mutable map by mapping each element to a key and value, if not present.
	 */
	public static <E extends Exception, T, K, V, M extends Map<K, V>> M mapIfAbsent(
		Excepts.Function<? extends E, ? super T, ? extends K> keyMapper,
		Excepts.Function<? extends E, ? super T, ? extends V> valueMapper,
		Functions.Supplier<M> mapSupplier, Iterable<? extends T> iterable) throws E {
		return putIfAbsent(keyMapper, valueMapper, mapSupplier.get(), iterable);
	}

	// inverted maps

	/**
	 * Creates a mutable map by providing a collection for each element and mapping each element of
	 * the collection back to the element.
	 */
	public static <E extends Exception, T, K> Map<K, T> inverseMap(
		Excepts.Function<E, ? super T, ? extends Collection<K>> mapper,
		Iterable<? extends T> iterable) throws E {
		return inverseMap(mapper, CollectionUtil.supplier.map(), iterable);
	}

	/**
	 * Creates a mutable map by providing a collection for each element and mapping each element of
	 * the collection back to the element.
	 */
	public static <E extends Exception, T, K, M extends Map<K, T>> M inverseMap(
		Excepts.Function<E, ? super T, ? extends Collection<K>> mapper,
		Functions.Supplier<M> mapSupplier, Iterable<? extends T> iterable) throws E {
		var map = mapSupplier.get();
		for (var t : iterable)
			for (var k : mapper.apply(t))
				map.put(k, t);
		return map;
	}
}
