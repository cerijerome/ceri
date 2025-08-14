package ceri.common.collection;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import ceri.common.function.Excepts;

public class Sets {
	private Sets() {}

	/**
	 * Set supplier.
	 */
	public static class Supplier {
		private Supplier() {}

		/**
		 * Returns a typed set instance.
		 */
		public static <T> Set<T> hash() {
			return new HashSet<>();
		}

		/**
		 * Returns a typed set instance, optimized for initial size.
		 */
		public static <T> Set<T> hash(int initialSize) {
			return new HashSet<>(initialSize);
		}

		/**
		 * Returns a typed set instance.
		 */
		public static <T> Set<T> linked() {
			return new LinkedHashSet<>();
		}

		/**
		 * Returns a typed set instance, optimized for initial size.
		 */
		public static <T> Set<T> linked(int initialSize) {
			return new LinkedHashSet<>(initialSize);
		}

		/**
		 * Returns a typed set instance.
		 */
		public static <T> NavigableSet<T> tree() {
			return new TreeSet<>();
		}

		/**
		 * Returns an identity hash set backed by a map.
		 */
		public static <T> Set<T> identity() {
			return Collections.newSetFromMap(new IdentityHashMap<>());
		}
	}

	/**
	 * Creates a mutable set from values.
	 */
	@SafeVarargs
	public static <T> Set<T> of(T... values) {
		return Mutable.addAll(Supplier.hash(), values);
	}

	/**
	 * Creates a mutable set from array values.
	 */
	public static <T> Set<T> set(T[] array, int offset) {
		return set(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable set from array values.
	 */
	public static <T> Set<T> set(T[] array, int offset, int length) {
		return Mutable.add(Supplier.hash(), array, offset, length);
	}

	/**
	 * Creates a mutable set from iterable values.
	 */
	public static <T> Set<T> set(Iterable<? extends T> values) {
		return Mutable.add(Supplier.hash(), values);
	}

	/**
	 * Creates a mutable set from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Set<U> adaptAll(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return Mutable.adaptAddAll(Supplier.hash(), mapper, values);
	}

	/**
	 * Creates a mutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U>
		adapt(Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
			throws E {
		return adapt(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable set from transformed array values.
	 */
	public static <E extends Exception, T, U> Set<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return Mutable.adaptAdd(Supplier.hash(), mapper, array, offset, length);
	}

	/**
	 * Creates a mutable set from transformed iterable values.
	 */
	public static <E extends Exception, T, U> Set<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return Mutable.adaptAdd(Supplier.hash(), mapper, values);
	}

	/**
	 * Creates a mutable set from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> Set<T> unmap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return Mutable.convertAdd(Supplier.hash(), unmapper, map);
	}
}
