package ceri.common.function;

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.common.util.Validate;

/**
 * Comparators for primitives and other objects, handling null cases.
 */
public class Compares {
	public static final Comparator<Object> NULL = (_, _) -> 0;
	public static final Comparator<Boolean> BOOL = of();
	public static final Comparator<Character> CHAR = of();
	public static final Comparator<Integer> INT = of();
	public static final Comparator<Integer> UINT = of(Integer::compareUnsigned);
	public static final Comparator<Long> LONG = of();
	public static final Comparator<Long> ULONG = of(Long::compareUnsigned);
	public static final Comparator<Float> FLOAT = of();
	public static final Comparator<Double> DOUBLE = of();
	public static final Comparator<String> STRING = of();
	public static final Comparator<Date> DATE = of();
	public static final Comparator<Locale> LOCALE = string();
	public static final Comparator<Object> TO_STRING = as(Strings::safe);

	private Compares() {}

	/**
	 * Determines comparator behavior for null values.
	 */
	public enum Nulls {
		/** Let the comparator handle nulls directly. */
		none,
		/** Nulls are ordered first. */
		first,
		/** Nulls are ordered last. */
		last,
		/** Null comparisons throw an exception. */
		fail;

		/** The default behavior. */
		public static final Nulls def = first;

		/**
		 * Makes sure the behavior does not pass nulls.
		 */
		public static Nulls safe(Nulls nulls) {
			return (nulls == null || nulls == none) ? fail : nulls;
		}

		/**
		 * Reverses the behavior.
		 */
		public static Nulls not(Nulls nulls) {
			return switch (nulls) {
				case first -> last;
				case last -> first;
				case null -> none;
				default -> nulls;
			};
		}
	}

	/**
	 * A comparator that always returns 0.
	 */
	public static <T> Comparator<T> ofNull() {
		return Reflect.unchecked(NULL);
	}

	/**
	 * A comparable comparator with default null behavior.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> of() {
		return of(Nulls.def);
	}

	/**
	 * A comparable comparator with given null behavior.
	 */
	public static <T extends Comparable<? super T>> Comparator<T> of(Nulls nulls) {
		return (l, r) -> compare(nulls, Comparator.naturalOrder(), l, r);
	}

	/**
	 * Wraps a comparator with default null behavior.
	 */
	public static <T> Comparator<T> of(Comparator<? super T> comparator) {
		return of(Nulls.def, comparator);
	}

	/**
	 * Wraps a comparator with given null behavior.
	 */
	public static <T> Comparator<T> of(Nulls nulls, Comparator<? super T> comparator) {
		return (l, r) -> compare(nulls, comparator, l, r);
	}

	/**
	 * Returns a zero comparator if the comparator is null.
	 */
	public static <T> Comparator<T> safe(Comparator<T> comparator) {
		return comparator == null ? ofNull() : comparator;
	}

	/**
	 * Reverses a comparator, with default null behavior.
	 */
	public static <T> Comparator<T> not(Comparator<? super T> comparator) {
		return not(Nulls.def, comparator);
	}

	/**
	 * Reverses a comparator, with given null behavior.
	 */
	public static <T> Comparator<T> not(Nulls nulls, Comparator<? super T> comparator) {
		var reversed = reverse(comparator);
		return (l, r) -> compare(nulls, reversed, l, r);
	}

	/**
	 * Adapts a comparable comparator using an accessor, with default null behavior.
	 */
	public static <T, U extends Comparable<? super U>> Comparator<T>
		as(Functions.Function<? super T, ? extends U> accessor) {
		return as(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparable comparator using an accessor, with given null behavior.
	 */
	public static <T, U extends Comparable<? super U>> Comparator<T> as(Nulls nulls,
		Functions.Function<? super T, ? extends U> accessor) {
		return as(nulls, accessor, of(nulls));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T, U> Comparator<T> as(Functions.Function<? super T, ? extends U> accessor,
		Comparator<? super U> comparator) {
		return as(Nulls.def, accessor, comparator);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T, U> Comparator<T> as(Nulls nulls,
		Functions.Function<? super T, ? extends U> accessor, Comparator<? super U> comparator) {
		return (l, r) -> compare(nulls, comparator, Functional.apply(accessor, l),
			Functional.apply(accessor, r));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T> Comparator<T> asInt(Functions.ToIntFunction<? super T> accessor) {
		return asInt(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T> Comparator<T> asInt(Nulls nulls,
		Functions.ToIntFunction<? super T> accessor) {
		return of(Nulls.safe(nulls), accessor == null ? ofNull() :
			(l, r) -> Integer.compare(accessor.applyAsInt(l), accessor.applyAsInt(r)));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T> Comparator<T> asUint(Functions.ToIntFunction<? super T> accessor) {
		return asUint(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T> Comparator<T> asUint(Nulls nulls,
		Functions.ToIntFunction<? super T> accessor) {
		return of(Nulls.safe(nulls), accessor == null ? ofNull() :
			(l, r) -> Integer.compareUnsigned(accessor.applyAsInt(l), accessor.applyAsInt(r)));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T> Comparator<T> asLong(Functions.ToLongFunction<? super T> accessor) {
		return asLong(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T> Comparator<T> asLong(Nulls nulls,
		Functions.ToLongFunction<? super T> accessor) {
		return of(Nulls.safe(nulls), accessor == null ? ofNull() :
			(l, r) -> Long.compare(accessor.applyAsLong(l), accessor.applyAsLong(r)));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T> Comparator<T> asUlong(Functions.ToLongFunction<? super T> accessor) {
		return asUlong(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T> Comparator<T> asUlong(Nulls nulls,
		Functions.ToLongFunction<? super T> accessor) {
		return of(Nulls.safe(nulls), accessor == null ? ofNull() :
			(l, r) -> Long.compareUnsigned(accessor.applyAsLong(l), accessor.applyAsLong(r)));
	}

	/**
	 * Adapts a comparator using an accessor, with default null behavior.
	 */
	public static <T> Comparator<T> asDouble(Functions.ToDoubleFunction<? super T> accessor) {
		return asDouble(Nulls.def, accessor);
	}

	/**
	 * Adapts a comparator using an accessor, with given null behavior.
	 */
	public static <T> Comparator<T> asDouble(Nulls nulls,
		Functions.ToDoubleFunction<? super T> accessor) {
		return of(Nulls.safe(nulls), accessor == null ? ofNull() :
			(l, r) -> Double.compare(accessor.applyAsDouble(l), accessor.applyAsDouble(r)));
	}

	/**
	 * Comparator for string representations of objects.
	 */
	public static <T> Comparator<T> string() {
		return Reflect.unchecked(TO_STRING);
	}

	// support

	private static <T> Comparator<T> reverse(Comparator<T> comparator) {
		return comparator == null ? null : (l, r) -> comparator.compare(r, l);
	}

	private static <T> int compare(Comparator<? super T> comparator, T l, T r) {
		return comparator == null ? 0 : comparator.compare(l, r);
	}

	private static <T> int compare(Nulls nulls, Comparator<? super T> comparator, T l, T r) {
		return switch (nulls) {
			case first -> nullAs(comparator, l, r, -1);
			case last -> nullAs(comparator, l, r, 1);
			case fail -> nullsFail(comparator, l, r);
			default -> compare(comparator, l, r);
		};
	}

	private static <T> int nullAs(Comparator<? super T> comparator, T l, T r, int value) {
		if (l == r) return 0;
		if (l == null) return value;
		if (r == null) return -value;
		return compare(comparator, l, r);
	}

	private static <T> int nullsFail(Comparator<? super T> comparator, T l, T r) {
		Validate.nonNull(l, "Left value");
		Validate.nonNull(r, "Right value");
		return compare(comparator, l, r);
	}
}
