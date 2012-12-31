/**
 * Created on Dec 3, 2005
 */
package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

/**
 * Utility methods to test and manipulate collections.
 */
public class CollectionUtil {

	private CollectionUtil() {
	}

	@SafeVarargs
	public static <T> List<T> immutableList(T...items) {
		return Collections.unmodifiableList(addAll(new ArrayList<T>(), items));
	}
	
	public static <T> List<T> immutableList(Collection<? extends T> items) {
		return Collections.unmodifiableList(new ArrayList<>(items));
	}
	
	public static <T> Iterable<T> reverseIterableList(final List<T> list) {
		return new Iterable<T> () {
			@Override
			public Iterator<T> iterator() {
				return new ReverseListIterator<>(list);
			}
		};
	}

	public static <T> Iterable<T> reverseIterableQueue(final Deque<T> deque) {
		return new Iterable<T> () {
			@Override
			public Iterator<T> iterator() {
				return deque.descendingIterator();
			}
		};
	}

	public static <T> Iterable<T> reverseIterableSet(final NavigableSet<T> navSet) {
		return new Iterable<T> () {
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
		return new DelegatingMap<K, V>(map) {
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
	public static <T, C extends Collection<? super T>> C addAll(C collection,
		T... items) {
		Collections.addAll(collection, items);
		return collection;
	}

	/**
	 * Returns the last element.
	 */
	public static <T> T last(Iterable<T> iterable) {
		if (iterable instanceof Collection<?>)
			return get(iterable, ((Collection<?>)iterable).size() - 1);
		Iterator<T> i = iterable.iterator();
		while (true) {
			T t = i.next();
			if (!i.hasNext()) return t;
		}
	}

	/**
	 * Returns the first element.
	 */
	public static <T> T first(Iterable<T> iterable) {
		return get(iterable, 0);
	}

	/**
	 * Returns the item at given index for an iterable instance.
	 * Optimized for Lists.
	 */
	public static <T> T get(Iterable<T> iterable, int index) {
		if (iterable instanceof LinkedList<?>) {
			LinkedList<T> linkedList = (LinkedList<T>)iterable;
			if (index == 0) return linkedList.getFirst();
			if (index == linkedList.size() - 1) return linkedList.getLast();
		}
		if (iterable instanceof List<?>)
			return ((List<T>)iterable).get(index);
		for (T t : iterable) if (index-- <= 0) return t;
		throw new IndexOutOfBoundsException(
			"Iterable does not contain item at index " + index);
	}

	/**
	 * Removes all given items from the collection.
	 * Returns true if the collection is modified.
	 */
	@SafeVarargs
	public static <T> boolean removeAll(Collection<? super T> collection, T...ts) {
		return collection.removeAll(Arrays.asList(ts));
	}

	/**
	 * Removes items from the first collection that are not in the second.
	 */
	public static void intersect(Collection<?> lhs, Collection<?> rhs) {
		for (Iterator<?> i = lhs.iterator(); i.hasNext(); ) {
			if (!rhs.contains(i.next())) i.remove();
		}
	}

	/**
	 * Creates a typed array from a collection and given type.
	 * The type must not be a primitive class type such as int.class
	 * otherwise a ClassCastException will be thrown.
	 */
	public static <T> T[] toArray(Collection<? extends T> collection,
		Class<T> type) {
		if (type.isPrimitive()) throw new IllegalArgumentException(
			"Primitives types not allowed");
		T[] array = ArrayUtil.create(type, collection.size());
		return collection.toArray(array);
	}

}
