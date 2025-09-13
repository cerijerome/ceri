package ceri.common.comparator;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

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
	public static final Comparator<Double> DOUBLE = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Float> FLOAT = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Byte> BYTE = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Short> SHORT = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Integer> INT = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Long> LONG = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Boolean> BOOL = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Character> CHAR = Reflect.unchecked(COMPARABLE);
	public static final Comparator<String> STRING = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Date> DATE = Reflect.unchecked(COMPARABLE);
	public static final Comparator<Byte> UBYTE = Comparator.nullsFirst(Byte::compareUnsigned);
	public static final Comparator<Short> USHORT = Comparator.nullsFirst(Short::compareUnsigned);
	public static final Comparator<Integer> UINT = Comparator.nullsFirst(Integer::compareUnsigned);
	public static final Comparator<Long> ULONG = Comparator.nullsFirst(Long::compareUnsigned);
	public static final Comparator<Locale> LOCALE = string();
	public static final Comparator<Object> TO_STRING = comparing(String::valueOf, STRING);
	private static final Comparator<Object> NULL = ((_, _) -> 0);

	private Comparators() {}

	/**
	 * Null comparator treats everything as equal.
	 */
	public static <T> Comparator<T> ofNull() {
		return Reflect.unchecked(NULL);
	}

	/**
	 * Casts the comparator, with default no-op comparator.
	 */
	public static <T> Comparator<T> of(Comparator<? super T> comparator) {
		return Reflect.unchecked(comparator != null ? comparator : NULL);
	}

	/**
	 * Wraps a comparator, where null values are considered inferior to non-null values.
	 */
	public static <T> Comparator<T> nonNull(final Comparator<? super T> comparator) {
		if (comparator == null) return Reflect.unchecked(NULL);
		return ((lhs, rhs) -> {
			if (lhs == rhs) return 0;
			if (lhs == null) return -1;
			if (rhs == null) return 1;
			if (lhs.equals(rhs)) return 0;
			return comparator.compare(lhs, rhs);
		});
	}

	/**
	 * Nulls first comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> nullsFirst() {
		return Reflect.unchecked(NULLS_FIRST_COMPARABLE);
	}

	/**
	 * Nulls last comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> nullsLast() {
		return Reflect.unchecked(NULLS_LAST_COMPARABLE);
	}

	/**
	 * Comparator for comparable objects.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> comparable() {
		return Reflect.unchecked(COMPARABLE);
	}

	/**
	 * Comparator for string representations of objects.
	 */
	public static <T> Comparator<T> string() {
		return Reflect.unchecked(TO_STRING);
	}

	/**
	 * Nulls first, field access and comparison.
	 */
	public static <T, U> Comparator<T> comparing(
		Functions.Function<? super T, ? extends U> accessor, Comparator<? super U> comparator) {
		return Comparator.nullsFirst(Comparator.comparing(accessor, comparator));
	}

	/**
	 * Nulls first, field access and natural order comparison.
	 */
	public static <T, U extends Comparable<? super U>> Comparator<T>
		comparing(Functions.Function<? super T, ? extends U> accessor) {
		return comparing(accessor, Comparator.naturalOrder());
	}

	/**
	 * Nulls first, field access and natural order comparison.
	 */
	public static <T> Comparator<T> comparingInt(Functions.ToIntFunction<? super T> accessor) {
		return Comparator.nullsFirst(Comparator.comparingInt(accessor));
	}

	/**
	 * Nulls first, field access and natural order comparison.
	 */
	public static <T> Comparator<T> comparingLong(Functions.ToLongFunction<? super T> accessor) {
		return Comparator.nullsFirst(Comparator.comparingLong(accessor));
	}

	/**
	 * Nulls first, field access and natural order comparison.
	 */
	public static <T> Comparator<T>
		comparingDouble(Functions.ToDoubleFunction<? super T> accessor) {
		return Comparator.nullsFirst(Comparator.comparingDouble(accessor));
	}
}
