package ceri.common.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.math.MathUtil;

/**
 * Support for mutable lists.
 */
public class Lists {
	private Lists() {}

	/**
	 * List supplier.
	 */
	public static class Supplier {
		private Supplier() {}

		/**
		 * Returns a typed list instance.
		 */
		public static <T> List<T> array() {
			return new ArrayList<>();
		}

		/**
		 * Returns a typed list instance, optimized for initial size.
		 */
		public static <T> List<T> array(int initialSize) {
			return new ArrayList<>(initialSize);
		}

		/**
		 * Returns a typed list instance.
		 */
		public static <T> List<T> linked() {
			return new LinkedList<>();
		}
	}

	/**
	 * Creates a mutable list from values.
	 */
	@SafeVarargs
	public static <T> List<T> of(T... values) {
		return Mutable.addAll(Supplier.array(), values);
	}

	/**
	 * Creates a mutable list from array values.
	 */
	public static <T> List<T> list(T[] array, int offset) {
		return list(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable list from array values.
	 */
	public static <T> List<T> list(T[] array, int offset, int length) {
		return Mutable.add(Supplier.array(), array, offset, length);
	}

	/**
	 * Creates a mutable list from iterable values.
	 */
	public static <T> List<T> list(Iterable<? extends T> values) {
		return Mutable.add(Supplier.array(), values);
	}

	/**
	 * Creates a mutable list from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> List<U> adaptAll(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return Mutable.adaptAddAll(Supplier.array(), mapper, values);
	}

	/**
	 * Creates a mutable list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U>
		adapt(Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset)
			throws E {
		return adapt(mapper, array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable list from transformed array values.
	 */
	public static <E extends Exception, T, U> List<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T[] array, int offset,
		int length) throws E {
		return Mutable.adaptAdd(Supplier.array(), mapper, array, offset, length);
	}

	/**
	 * Creates a mutable list from transformed iterable values.
	 */
	public static <E extends Exception, T, U> List<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return Mutable.adaptAdd(Supplier.array(), mapper, values);
	}

	/**
	 * Creates a mutable list from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> List<T> unmap(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return Mutable.convertAdd(Supplier.array(), unmapper, map);
	}

	/**
	 * Gets the element at index, or null.
	 */
	public static <T> T at(List<? extends T> list, int index) {
		return at(list, index, null);
	}

	/**
	 * Gets the element at index, or default.
	 */
	public static <T> T at(List<? extends T> list, int index, T def) {
		if (list == null || index < 0 || list.isEmpty() || index >= list.size()) return def;
		return list.get(index);
	}

	/**
	 * Returns the last element, or default.
	 */
	public static <T> T last(List<? extends T> list) {
		return last(list, null);
	}

	/**
	 * Returns the last element, or default.
	 */
	public static <T> T last(List<? extends T> list, T def) {
		if (list == null || list.isEmpty()) return def;
		return list.getLast();
	}

	/**
	 * Inserts values into the list at index.
	 */
	@SafeVarargs
	public static <T> List<T> insertAll(List<T> dest, int index, T... src) {
		return insert(dest, index, src, 0);
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, T[] src, int offset) {
		return insert(dest, index, src, offset, Integer.MAX_VALUE);
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, T[] src, int offset, int length) {
		if (src == null || dest == null) return dest;
		return ArrayUtil.applySlice(src.length, offset, length, (o, l) -> {
			int i = MathUtil.limit(index, 0, dest.size() - 1);
			dest.addAll(i, Arrays.asList(src).subList(o, o + l));
			return dest;
		});
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, Collection<T> src) {
		if (src == null || dest == null) return dest;
		int i = MathUtil.limit(index, 0, dest.size() - 1);
		dest.addAll(i, src);
		return dest;
	}
}
