package ceri.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for creating immutable objects.
 */
public class ImmutableUtil {

	private ImmutableUtil() {}

	/**
	 * Creates an immutable iterable wrapper that returns an immutable iterator.
	 */
	public static <T> Iterable<T> iterable(final Iterable<T> iterable) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return ImmutableUtil.iterator(iterable.iterator());
			}
		};
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
	 * Copies a collection of objects into an immutable HashSet.
	 */
	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		if (set.isEmpty()) return Collections.emptySet();
		return Collections.unmodifiableSet(new HashSet<>(set));
	}

	/**
	 * Copies a map of objects into an immutable HashMap.
	 */
	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map) {
		if (map.isEmpty()) return Collections.emptyMap();
		return Collections.unmodifiableMap(new HashMap<>(map));
	}

	/**
	 * Copies a collection of objects into an immutable ArrayList.
	 */
	public static <T> List<T> copyAsList(Collection<? extends T> list) {
		if (list.isEmpty()) return Collections.emptyList();
		return Collections.unmodifiableList(new ArrayList<>(list));
	}

	/**
	 * Copies an array of objects into an immutable ArrayList.
	 */
	public static <T> List<T> arrayAsList(T[] array) {
		if (array.length == 0) return Collections.emptyList();
		List<T> list = new ArrayList<>();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	/**
	 * Copies an array of objects into an immutable HashSet.
	 */
	public static <T> Set<T> arrayAsSet(T[] array) {
		if (array.length == 0) return Collections.emptySet();
		Set<T> set = new HashSet<>();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Copies objects into an immutable ArrayList.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		return arrayAsList(array);
	}
	
	/**
	 * Copies objects into an immutable HashSet.
	 */
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return arrayAsSet(array);
	}
	
}