package ceri.common.collection;

import java.util.Collection;
import java.util.Iterator;
import ceri.common.function.Excepts;
import ceri.common.function.Predicates;
import ceri.common.reflect.Reflect;

/**
 * Iterable type support.
 */
public class Iterables {
	private static final Iterable<Object> NULL = () -> Iterators.ofNull();

	private Iterables() {}

	public static class Filter {
		private Filter() {}

		/**
		 * Predicate that returns true if a collection contains all the values.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>> has(T value) {
			return any(Predicates.eq(value));
		}

		/**
		 * Predicate that returns true if a collection contains all the values.
		 */
		@SafeVarargs
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			hasAny(T... values) {
			if (values == null) return Predicates.isNull();
			return hasAny(Sets.ofAll(values));
		}

		/**
		 * Predicate that returns true if a collection contains all the values.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			hasAny(Collection<? extends T> values) {
			if (values == null) return Predicates.isNull();
			return any(values::contains);
		}

		/**
		 * Returns true if any element matches the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			any(Excepts.Predicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				for (T t : ts)
					if (predicate.test(t)) return true;
				return false;
			};
		}

		/**
		 * Returns true if all elements match the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			all(Excepts.Predicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				for (T t : ts)
					if (!predicate.test(t)) return false;
				return true;
			};
		}

		/**
		 * Returns true if any element and index matches the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			anyIndex(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				int i = 0;
				for (T t : ts)
					if (predicate.test(t, i++)) return true;
				return false;
			};
		}

		/**
		 * Returns true if all elements and indexes match the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			allIndex(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Predicates.no();
			return ts -> {
				if (ts == null) return false;
				int i = 0;
				for (T t : ts)
					if (!predicate.test(t, i++)) return false;
				return true;
			};
		}
	}

	/**
	 * Returns a no-op, stateless iterable.
	 */
	public static <T> Iterable<T> ofNull() {
		return Reflect.unchecked(NULL);
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
		return iterable == null ? 0 : Iterators.removeIf(iterable.iterator(), predicate);
	}
}
