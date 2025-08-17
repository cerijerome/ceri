package ceri.common.collection;

import java.util.Iterator;
import ceri.common.function.Excepts;
import ceri.common.util.BasicUtil;

/**
 * Iterable type support.
 */
public class Iterables {
	private static final Iterable<Object> NULL = () -> Iterators.ofNull();

	private Iterables() {}

	/**
	 * Returns a no-op, stateless iterable.
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
		return first(iterable, null);
	}

	/**
	 * Returns the first element, or default if no elements.
	 */
	public static <T> T first(Iterable<? extends T> iterable, T def) {
		return nth(iterable, 0, def);
	}

	/**
	 * Returns the nth value (starting from 0), or null if unavailable.
	 */
	public static <T> T nth(Iterable<? extends T> iterable, int n) {
		return nth(iterable, n, null);
	}

	/**
	 * Returns the nth value (starting from 0), or default if unavailable.
	 */
	public static <T> T nth(Iterable<? extends T> iterable, int n, T def) {
		return iterable == null ? def : Iterators.nth(iterable.iterator(), n, def);
	}

	/**
	 * Calls the consumer for each element and returns the element count.
	 */
	public static <E extends Exception, T> int forEach(Iterable<T> iterable,
		Excepts.Consumer<E, ? super T> consumer) throws E {
		return iterable == null ? 0 : Iterators.forEach(iterable.iterator(), consumer);
	}

	/**
	 * Removes elements that match the predicate, and returns the number of removed elements.
	 */
	public static <E extends Exception, T> int removeIf(Iterable<T> iterable,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		if (iterable == null || predicate == null) return 0;
		return Iterators.removeIf(iterable.iterator(), predicate);
	}
}
