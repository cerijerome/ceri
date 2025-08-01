package ceri.common.comparator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import ceri.common.collection.ImmutableUtil;
import ceri.common.util.BasicUtil;

/**
 * Comparators for primitives and other objects, handling null cases.
 */
public class Comparators {
	private static final Comparator<Comparable<Comparable<?>>> NULLS_FIRST_COMPARABLE =
		Comparator.nullsFirst(Comparator.naturalOrder());
	private static final Comparator<Comparable<Comparable<?>>> NULLS_LAST_COMPARABLE =
		Comparator.nullsLast(Comparator.naturalOrder());
	private static final Comparator<Comparable<Comparable<?>>> COMPARABLE =
		nonNull(Comparable::compareTo);
	public static final Comparator<Double> DOUBLE = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Float> FLOAT = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Byte> BYTE = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Short> SHORT = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Integer> INT = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Long> LONG = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Boolean> BOOL = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Character> CHAR = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<String> STRING = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Date> DATE = BasicUtil.unchecked(COMPARABLE);
	public static final Comparator<Byte> UBYTE = nonNull(Byte::compareUnsigned);
	public static final Comparator<Short> USHORT = nonNull(Short::compareUnsigned);
	public static final Comparator<Integer> UINT = nonNull(Integer::compareUnsigned);
	public static final Comparator<Long> ULONG = nonNull(Long::compareUnsigned);
	public static final Comparator<Locale> LOCALE = string();
	private static final Comparator<?> STRING_VALUE =
		nonNull((lhs, rhs) -> STRING.compare(String.valueOf(lhs), String.valueOf(rhs)));
	private static final Comparator<?> NULL = ((_, _) -> 0);
	private static final Comparator<?> NON_NULL = nonNull((_, _) -> 0);

	private Comparators() {}

	/**
	 * Comparator based on position in a given collection. Items not in the list are placed at the
	 * end.
	 */
	@SafeVarargs
	public static <T> Comparator<T> order(T... ts) {
		return order(Arrays.asList(ts));
	}

	/**
	 * Comparator based on position in a given collection. Items not in the list are placed at the
	 * end.
	 */
	public static <T> Comparator<T> order(Collection<T> ts) {
		List<T> list = ImmutableUtil.copyAsList(ts);
		return transform(INT, t -> indexOf(list, t));
	}

	private static <T> int indexOf(List<T> list, T t) {
		int i = list.indexOf(t);
		return i == -1 ? list.size() : i;
	}

	/**
	 * Transforms a comparator of one type to another using an accessor.
	 */
	public static <T, R> Comparator<T> transform(Comparator<? super R> comparator,
		Function<T, R> accessor) {
		Function<T, R> safe = t -> t == null ? null : accessor.apply(t);
		return (lhs, rhs) -> comparator.compare(safe.apply(lhs), safe.apply(rhs));
	}

	/**
	 * Wraps a comparator, where null values are considered inferior to non-null values.
	 */
	public static <T> Comparator<T> nonNull(final Comparator<? super T> comparator) {
		if (comparator == null) return BasicUtil.unchecked(NULL);
		return ((lhs, rhs) -> {
			if (lhs == rhs) return 0;
			if (lhs == null) return -1;
			if (rhs == null) return 1;
			if (lhs.equals(rhs)) return 0;
			return comparator.compare(lhs, rhs);
		});
	}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> nullsFirstComparable() {
		return BasicUtil.unchecked(NULLS_FIRST_COMPARABLE);
	}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> nullsLastComparable() {
		return BasicUtil.unchecked(NULLS_LAST_COMPARABLE);
	}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> comparable() {
		return BasicUtil.unchecked(COMPARABLE);
	}

	/**
	 * Comparator for string representations of objects.
	 */
	public static <T> Comparator<T> string() {
		return BasicUtil.unchecked(STRING_VALUE);
	}

	/**
	 * Null comparator treats everything as equal.
	 */
	public static <T> Comparator<T> nullComparator() {
		return BasicUtil.unchecked(NULL);
	}

	/**
	 * Non-null comparator treats null as inferior, everything else equal.
	 */
	public static <T> Comparator<T> nonNullComparator() {
		return BasicUtil.unchecked(NON_NULL);
	}

	/**
	 * Create a comparator the checks comparators in sequence.
	 */
	@SafeVarargs
	// public static <T> Comparator<T> sequence(Comparator<? super T>... comparators) {
	public static <T> Comparator<T> sequence(Comparator<T>... comparators) {
		return sequence(Arrays.asList(comparators));
	}

	/**
	 * Create a comparator the checks comparators in sequence.
	 */
	public static <T> Comparator<T> sequence(Collection<? extends Comparator<T>> comparators) {
		// sequence(Collection<? extends Comparator<? super T>> comparators) {
		return ComparatorSequence.<T>builder().add(comparators).build();
	}

	/**
	 * Comparator to group given items first, then apply the comparator.
	 */
	@SafeVarargs
	public static <T> Comparator<T> group(Comparator<? super T> comparator, T... ts) {
		return group(comparator, Arrays.asList(ts));
	}

	/**
	 * Comparator to group given items first.
	 */
	public static <T> Comparator<T> group(final Comparator<? super T> comparator,
		final Collection<T> ts) {
		return nonNull((lhs, rhs) -> {
			boolean lhsEq = ts.contains(lhs);
			boolean rhsEq = ts.contains(rhs);
			if (lhsEq && rhsEq) return comparator.compare(lhs, rhs);
			if (lhsEq) return -1;
			if (rhsEq) return 1;
			return comparator.compare(lhs, rhs);
		});
	}

	/**
	 * Reverses a given comparator.
	 */
	public static <T> Comparator<T> reverse(final Comparator<T> comparator) {
		return ((lhs, rhs) -> -comparator.compare(lhs, rhs));
	}

}
