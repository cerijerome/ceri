package ceri.common.function;

import java.util.Collection;
import java.util.Objects;
import ceri.common.collect.Lists;
import ceri.common.collect.Sets;
import ceri.common.reflect.Reflect;
import ceri.common.util.Validate;

/**
 * Methods for building predicates.
 */
public class Filters {
	public static final Functions.Predicate<Object> YES = _ -> true;
	public static final Functions.Predicate<Object> NO = _ -> false;
	public static final Functions.Predicate<Object> IS_NULL = (t -> t == null);
	public static final Functions.Predicate<Object> NON_NULL = (t -> t != null);

	private Filters() {}

	/**
	 * Determines predicate behavior for null values.
	 */
	public enum Nulls {
		/** Let the predicate handle nulls directly. */
		none,
		/** Nulls are false. */
		no,
		/** Nulls are true. */
		yes,
		/** Nulls throw an exception. */
		fail;

		/** The default behavior. */
		public static final Nulls def = no;

		/**
		 * Makes sure the behavior does not pass nulls.
		 */
		public static Nulls safe(Nulls nulls) {
			return (nulls == null || nulls == none) ? fail : nulls;
		}
	}

	// Execution
	
	public static <E extends Exception, T> boolean test(Nulls nulls,
		Excepts.Predicate<E, ? super T> predicate, T t) throws E {
		return switch (nulls) {
			case no -> t == null ? false : test(predicate, t);
			case yes -> t == null ? true : test(predicate, t);
			case fail -> test(predicate, Validate.nonNull(t));
			default -> test(predicate, t);
		};
	}

	public static <E extends Exception, T, U> boolean test(Nulls nulls,
		Excepts.BiPredicate<E, ? super T, ? super U> predicate, T t, U u) throws E {
		return switch (nulls) {
			case no -> t == null || u == null ? false : test(predicate, t, u);
			case yes -> t == null || u == null  ? true : test(predicate, t, u);
			case fail -> test(predicate, Validate.nonNull(t), Validate.nonNull(u));
			default -> test(predicate, t, u);
		};
	}
	
	// Casting

	/**
	 * Casts to super exception and sub-type.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		cast(Excepts.Predicate<? extends E, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Casts from runtime to any exception, and type to sub-type.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		ex(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	// General predicates

	/**
	 * Returns true for all arguments.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> yes() {
		return Reflect.unchecked(YES);
	}

	/**
	 * Returns false for all arguments.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> no() {
		return Reflect.unchecked(NO);
	}

	/**
	 * Returns true if the argument is null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> isNull() {
		return Reflect.unchecked(IS_NULL);
	}

	/**
	 * Returns true if the argument is not null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> nonNull() {
		return Reflect.unchecked(NON_NULL);
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> equal(T value) {
		return t -> Objects.equals(t, value);
	}

	/**
	 * Returns true if the argument equals any of the values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T> equalAnyOf(T... values) {
		if (values == null) return no();
		return equalAny(Sets.ofAll(values));
	}

	/**
	 * Returns true if the argument equals any of the values. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		equalAny(Collection<? extends T> values) {
		if (values == null) return no();
		return values::contains;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.IntPredicate<E> equal(int value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.LongPredicate<E> equal(long value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.DoublePredicate<E> equal(double value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument is an instance of the class. A null class matches all.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> instance(Class<?> cls) {
		return t -> cls == null || cls.isInstance(t);
	}

	// Adapters

	/**
	 * Wraps a predicate with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		of(Excepts.Predicate<? extends E, ? super T> predicate) {
		return of(Nulls.def, predicate);
	}

	/**
	 * Wraps a predicate with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> of(Nulls nulls,
		Excepts.Predicate<? extends E, ? super T> predicate) {
		return t -> test(nulls, predicate, t);
	}

	/**
	 * Returns a no predicate if the predicate is null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		safe(Excepts.Predicate<E, T> predicate) {
		return predicate == null ? no() : predicate;
	}

	/**
	 * Reverses a predicate, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		not(Excepts.Predicate<? extends E, ? super T> predicate) {
		return not(Nulls.def, predicate);
	}

	/**
	 * Reverses a predicate, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> not(Nulls nulls,
		Excepts.Predicate<? extends E, ? super T> predicate) {
		var reversed = reverse(predicate);
		return t -> test(nulls, reversed, t);
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T> as(
		Excepts.Function<? extends E, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends E, ? super U> predicate) {
		return as(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T> as(Nulls nulls,
		Excepts.Function<? extends E, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends E, ? super U> predicate) {
		return t -> test(nulls, predicate, Functional.apply(accessor, t));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asInt(
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		return asInt(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asInt(Nulls nulls,
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		return of(Nulls.safe(nulls),
			accessor == null ? no() : t -> testInt(predicate, accessor.applyAsInt(t)));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asLong(
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		return asLong(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asLong(Nulls nulls,
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		return of(Nulls.safe(nulls),
			accessor == null ? no() : t -> testLong(predicate, accessor.applyAsLong(t)));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asDouble(
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		return asDouble(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asDouble(Nulls nulls,
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		return of(Nulls.safe(nulls),
			accessor == null ? no() : t -> testDouble(predicate, accessor.applyAsDouble(t)));
	}

	/**
	 * Adapts a predicate using accessors, with default null behavior.
	 */
	public static <E extends Exception, T, U, V> Excepts.Predicate<E, T> biAs(
		Excepts.Function<? extends E, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends E, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends E, ? super U, ? super V> predicate) {
		return biAs(Nulls.def, uAccessor, vAccessor, predicate);
	}
	
	/**
	 * Adapts a predicate using accessors, with given null behavior.
	 */
	public static <E extends Exception, T, U, V> Excepts.Predicate<E, T> biAs(Nulls nulls,
		Excepts.Function<? extends E, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends E, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends E, ? super U, ? super V> predicate) {
		return t -> test(nulls, predicate, Functional.apply(uAccessor, t),
			Functional.apply(vAccessor, t));
	}

	// Composers

	/**
	 * Returns true if all the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		andOf(Excepts.Predicate<? extends E, ? super T>... predicates) {
		return and(Lists.wrap(predicates));
	}

	/**
	 * Returns true if all the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		and(Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		return t -> {
			if (t == null) return false;
			for (var predicate : predicates)
				if (predicate == null || !predicate.test(t)) return false;
			return true;
		};
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		orOf(Excepts.Predicate<? extends E, ? super T>... predicates) {
		return or(Lists.wrap(predicates));
	}

	/**
	 * Returns true if any of the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		or(Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		return t -> {
			if (t != null) for (var predicate : predicates)
				if (predicate != null && predicate.test(t)) return true;
			return false;
		};
	}

	// Comparable predicates

	/**
	 * Returns true if the argument >= min and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T>
		gte(T min) {
		return t -> t != null && (min == null || t.compareTo(min) >= 0);
	}

	/**
	 * Returns true if the argument > min and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T> gt(T min) {
		return t -> t != null && (min == null || t.compareTo(min) > 0);
	}

	/**
	 * Returns true if the argument <= max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T>
		lte(T max) {
		return t -> t != null && (max == null || t.compareTo(max) <= 0);
	}

	/**
	 * Returns true if the argument < max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T> lt(T max) {
		return t -> t != null && (max == null || t.compareTo(max) < 0);
	}

	/**
	 * Returns true if the min <= argument <= max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T>
		range(T min, T max) {
		return andOf(gte(min), lte(max));
	}
	
	// support

	private static <E extends Exception, T> Excepts.Predicate<E, T>
		reverse(Excepts.Predicate<E, T> predicate) {
		return predicate == null ? null : t -> !predicate.test(t);
	}

	private static <E extends Exception, T> boolean test(Excepts.Predicate<E, ? super T> predicate,
		T t) throws E {
		return predicate == null ? false : predicate.test(t);
	}

	private static <E extends Exception> boolean testInt(Excepts.IntPredicate<E> predicate, int i)
		throws E {
		return predicate == null ? false : predicate.test(i);
	}

	private static <E extends Exception> boolean testLong(Excepts.LongPredicate<E> predicate,
		long l) throws E {
		return predicate == null ? false : predicate.test(l);
	}

	private static <E extends Exception> boolean testDouble(Excepts.DoublePredicate<E> predicate,
		double d) throws E {
		return predicate == null ? false : predicate.test(d);
	}

	private static <E extends Exception, T, U> boolean
		test(Excepts.BiPredicate<E, ? super T, ? super U> predicate, T t, U u) throws E {
		return predicate == null ? false : predicate.test(t, u);
	}
}
