package ceri.common.function;

import java.util.Collection;
import java.util.Objects;
import ceri.common.collect.Lists;
import ceri.common.collect.Sets;
import ceri.common.reflect.Reflect;

/**
 * Methods for building predicates.
 */
public class Filters {
	public static final Functions.Predicate<Object> yes = _ -> true;
	public static final Functions.Predicate<Object> no = _ -> false;
	public static final Functions.Predicate<Object> isNull = (t -> t == null);
	public static final Functions.Predicate<Object> nonNull = (t -> t != null);

	private Filters() {}

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
		return Reflect.unchecked(yes);
	}

	/**
	 * Returns false for all arguments.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> no() {
		return Reflect.unchecked(no);
	}

	/**
	 * Returns true if the argument is null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> isNull() {
		return Reflect.unchecked(isNull);
	}

	/**
	 * Returns true if the argument is not null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> nonNull() {
		return Reflect.unchecked(nonNull);
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
	 * Modifies a predicate; returns true for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		nullYes(Excepts.Predicate<? extends E, ? super T> predicate) {
		return nullAs(true, predicate);
	}

	/**
	 * Modifies a predicate; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		nullNo(Excepts.Predicate<? extends E, ? super T> predicate) {
		return nullAs(false, predicate);
	}

	/**
	 * Modifies a predicate; returns the given result for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> nullAs(boolean nullResult,
		Excepts.Predicate<? extends E, ? super T> predicate) {
		if (predicate == null) return nullAs(nullResult, no());
		return t -> (t == null ? nullResult : predicate.test(t));
	}

	/**
	 * Inverts a predicate. Nulls are passed to the predicate.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		not(Excepts.Predicate<? extends E, ? super T> predicate) {
		if (predicate == null) return no();
		return t -> !predicate.test(t);
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T, R> Excepts.Predicate<E, T> testing(
		Excepts.Function<? extends E, ? super T, R> accessor,
		Excepts.Predicate<? extends E, ? super R> predicate) {
		if (predicate == null || accessor == null) return no();
		return t -> t != null && predicate.test(accessor.apply(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingInt(
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return no();
		return t -> t != null && predicate.test(accessor.applyAsInt(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingLong(
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return no();
		return t -> t != null && predicate.test(accessor.applyAsLong(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingDouble(
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return no();
		return t -> t != null && predicate.test(accessor.applyAsDouble(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T, U, V> Excepts.Predicate<E, T> biTesting(
		Excepts.Function<? extends E, ? super T, U> uAccessor,
		Excepts.Function<? extends E, ? super T, V> vAccessor,
		Excepts.BiPredicate<? extends E, ? super U, ? super V> predicate) {
		if (predicate == null || uAccessor == null || vAccessor == null) return no();
		return t -> t != null && predicate.test(uAccessor.apply(t), vAccessor.apply(t));
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
}
