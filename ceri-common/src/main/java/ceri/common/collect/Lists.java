package ceri.common.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Compares;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;

/**
 * Support for mutable lists.
 */
public class Lists {
	private Lists() {}

	/**
	 * Utility for building lists.
	 */
	public static class Builder<T, L extends List<T>>
		extends Collectable.Builder<T, L, Builder<T, L>> {

		/**
		 * Create a builder using the list.
		 */
		public static <T, L extends List<T>> Builder<T, L> of(L list) {
			return new Builder<>(list);
		}

		private Builder(L list) {
			super(list);
		}

		@Override
		public List<T> wrap() {
			return Immutable.wrap(get());
		}
	}

	/**
	 * Create a builder.
	 */
	@SafeVarargs
	public static <T> Builder<T, List<T>> build(T value, T... values) {
		return build(Lists::of, value, values);
	}

	/**
	 * Create a builder using the supplier.
	 */
	@SafeVarargs
	public static <T, L extends List<T>> Builder<T, L> build(Functions.Supplier<L> supplier,
		T value, T... values) {
		return Builder.of(supplier.get()).add(value, values);
	}

	// create

	/**
	 * Creates an empty mutable linked list.
	 */
	public static <T> LinkedList<T> link() {
		return new LinkedList<>();
	}

	/**
	 * Creates an empty mutable list.
	 */
	public static <T> List<T> of() {
		return new ArrayList<>();
	}

	/**
	 * Creates a fixed-size list view of the values.
	 */
	@SafeVarargs
	public static <T> List<T> wrap(T... values) {
		return ArrayUtil.isEmpty(values) ? List.of() : Arrays.asList(values); 
	}

	/**
	 * Creates a mutable list from values.
	 */
	@SafeVarargs
	public static <T> List<T> ofAll(T... values) {
		return of(values, 0);
	}

	/**
	 * Creates a mutable list from array values.
	 */
	public static <T> List<T> of(T[] array, int offset) {
		return of(array, offset, Integer.MAX_VALUE);
	}

	/**
	 * Creates a mutable list from array values.
	 */
	public static <T> List<T> of(T[] array, int offset, int length) {
		return Collectable.add(of(), array, offset, length);
	}

	/**
	 * Creates a mutable list from iterable values.
	 */
	public static <T> List<T> of(Iterable<? extends T> values) {
		return Collectable.add(of(), values);
	}

	/**
	 * Creates a mutable list from transformed values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> List<U> adaptAll(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, T... values) throws E {
		return adapt(mapper, values, 0);
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
		return Collectable.adaptAdd(of(), mapper, array, offset, length);
	}

	/**
	 * Creates a mutable list from transformed iterable values.
	 */
	public static <E extends Exception, T, U> List<U> adapt(
		Excepts.Function<? extends E, ? super T, ? extends U> mapper, Iterable<? extends T> values)
		throws E {
		return Collectable.adaptAdd(of(), mapper, values);
	}

	/**
	 * Creates a mutable list from transformed map entries.
	 */
	public static <E extends Exception, K, V, T> List<T> convert(
		Excepts.BiFunction<? extends E, ? super K, ? super V, ? extends T> unmapper, Map<K, V> map)
		throws E {
		return Collectable.convertAdd(of(), unmapper, map);
	}

	// access

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
	 * Returns a bounded sub-list view.
	 */
	public static <T> List<T> sub(List<T> list, int offset, int length) {
		if (list == null) return Immutable.list();
		return ArrayUtil.applySlice(Collectable.size(list), offset, length,
			(o, l) -> list.subList(o, o + l));
	}

	/**
	 * Sorts the list with natural ordering and nulls first.
	 */
	public static <T extends Comparable<? super T>, L extends List<T>> L sort(L list) {
		return sort(list, Compares.nullsFirst());
	}

	/**
	 * Sorts the list.
	 */
	public static <T, L extends List<T>> L sort(L list, Comparator<? super T> comparator) {
		if (Collectable.isEmpty(list) || comparator == null) return list;
		list.sort(comparator);
		return list;
	}

	// set

	/**
	 * Fills the list with the given value.
	 */
	public static <T, L extends List<T>> L fill(L list, T fill) {
		return fill(list, 0, fill);
	}

	/**
	 * Fills the list range with the given value.
	 */
	public static <T, L extends List<T>> L fill(L list, int offset, T fill) {
		return fill(list, offset, Integer.MAX_VALUE, fill);
	}

	/**
	 * Fills the list range with the given value.
	 */
	public static <T, L extends List<T>> L fill(L list, int offset, int length, T fill) {
		if (list != null) ArrayUtil.acceptSlice(list.size(), offset, length, (o, l) -> {
			for (int i = 0; i < l; i++)
				list.set(o + i, fill);
		});
		return list;
	}

	// insert

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
			int i = Maths.limit(index, 0, dest.size() - 1);
			dest.addAll(i, Arrays.asList(src).subList(o, o + l));
			return dest;
		});
	}

	/**
	 * Inserts values into the list at index.
	 */
	public static <T> List<T> insert(List<T> dest, int index, Collection<? extends T> src) {
		if (src == null || dest == null) return dest;
		int i = Maths.limit(index, 0, dest.size() - 1);
		dest.addAll(i, src);
		return dest;
	}
}
