package ceri.common.collect;

import java.util.Collection;
import java.util.Iterator;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Iterable type support.
 */
public class Iterables {
	private static final Iterable<Object> NULL = () -> Iterators.ofNull();

	private Iterables() {}

	/**
	 * Iterable filter support.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Adapts type parameters.
		 */
		public static <T, U extends T, I extends Iterable<U>> Functions.Predicate<I>
			cast(Functions.Predicate<Iterable<T>> predicate) {
			return Reflect.unchecked(predicate);
		}

		/**
		 * Adapts type parameters.
		 */
		public static <E extends Exception, T, U extends T, I extends Iterable<U>>
			Excepts.Predicate<E, I> castEx(Excepts.Predicate<? extends E, Iterable<T>> predicate) {
			return Reflect.unchecked(predicate);
		}

		/**
		 * Predicate that returns true if an iterable sequence contains the value.
		 */
		public static <T> Functions.Predicate<Iterable<T>> has(T value) {
			return ts -> testAny(ts, Filters.equal(value));
		}

		/**
		 * Predicate that returns true if an iterable sequence contains any of the values.
		 */
		@SafeVarargs
		public static <T> Functions.Predicate<Iterable<T>> hasAnyOf(T... values) {
			if (values == null) return Filters.NO.cast();
			return hasAny(Sets.ofAll(values));
		}

		/**
		 * Predicate that returns true if an iterable sequence contains any of the values.
		 */
		public static <T> Functions.Predicate<Iterable<T>> hasAny(Collection<? extends T> values) {
			if (values == null) return Filters.NO.cast();
			return any(values::contains);
		}

		/**
		 * Returns true if any element matches the predicate.
		 */
		public static <T> Functions.Predicate<Iterable<T>>
			any(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
			if (predicate == null) return Filters.NO.cast();
			return ts -> testAny(ts, predicate);
		}

		/**
		 * Returns true if any element matches the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			anyEx(Excepts.Predicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Filters.NO.castEx();
			return ts -> testAny(ts, predicate);
		}

		/**
		 * Returns true if all elements match the predicate.
		 */
		public static <T> Functions.Predicate<Iterable<T>>
			all(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
			if (predicate == null) return Filters.NO.cast();
			return ts -> testAll(ts, predicate);
		}

		/**
		 * Returns true if all elements match the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			allEx(Excepts.Predicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Filters.NO.castEx();
			return ts -> testAll(ts, predicate);
		}

		/**
		 * Returns true if any element and index matches the predicate.
		 */
		public static <T> Functions.Predicate<Iterable<T>>
			anyIndex(Excepts.ObjIntPredicate<? extends RuntimeException, ? super T> predicate) {
			if (predicate == null) return Filters.NO.cast();
			return ts -> testAnyIndex(ts, predicate);
		}

		/**
		 * Returns true if any element and index matches the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			anyIndexEx(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Filters.NO.castEx();
			return ts -> testAnyIndex(ts, predicate);
		}

		/**
		 * Returns true if all elements and indexes match the predicate.
		 */
		public static <T> Functions.Predicate<Iterable<T>>
			allIndex(Excepts.ObjIntPredicate<? extends RuntimeException, ? super T> predicate) {
			if (predicate == null) return Filters.NO.cast();
			return ts -> testAllIndex(ts, predicate);
		}

		/**
		 * Returns true if all elements and indexes match the predicate.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
			allIndexEx(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
			if (predicate == null) return Filters.NO.castEx();
			return ts -> testAllIndex(ts, predicate);
		}

		/**
		 * Returns true if the predicate matches the iterable.
		 */
		public static <E extends Exception, T, U extends T> boolean test(Iterable<U> iterable,
			Excepts.Predicate<E, Iterable<T>> predicate) throws E {
			return Filters.test(null, iterable, castEx(predicate));
		}

		/**
		 * Returns true if the predicate matches any elements.
		 */
		public static <E extends Exception, T> boolean testAny(Iterable<T> iterable,
			Excepts.Predicate<? extends E, ? super T> predicate) throws E {
			if (iterable == null) return false;
			for (T t : iterable)
				if (predicate.test(t)) return true;
			return false;
		}

		/**
		 * Returns true if the predicate matches all elements.
		 */
		public static <E extends Exception, T> boolean testAll(Iterable<T> iterable,
			Excepts.Predicate<? extends E, ? super T> predicate) throws E {
			if (iterable == null) return false;
			for (T t : iterable)
				if (!predicate.test(t)) return false;
			return true;
		}

		/**
		 * Returns true if the predicate matches any element and index.
		 */
		public static <E extends Exception, T> boolean testAnyIndex(Iterable<T> iterable,
			Excepts.ObjIntPredicate<? extends E, ? super T> predicate) throws E {
			if (iterable == null) return false;
			int i = 0;
			for (T t : iterable)
				if (predicate.test(t, i++)) return true;
			return false;
		}

		/**
		 * Returns true if the predicate matches all elements and indexes.
		 */
		public static <E extends Exception, T> boolean testAllIndex(Iterable<T> iterable,
			Excepts.ObjIntPredicate<? extends E, ? super T> predicate) throws E {
			if (iterable == null) return false;
			int i = 0;
			for (T t : iterable)
				if (!predicate.test(t, i++)) return false;
			return true;
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
