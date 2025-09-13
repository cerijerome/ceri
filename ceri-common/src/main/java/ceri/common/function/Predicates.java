package ceri.common.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.Collectable;
import ceri.common.collection.Sets;
import ceri.common.reflect.Reflect;

/**
 * Methods for building predicates.
 */
public class Predicates {
	public static final Functions.Predicate<Object> yes = (_ -> true);
	public static final Functions.Predicate<Object> no = (_ -> false);
	public static final Functions.Predicate<Object> isNull = (t -> t == null);
	public static final Functions.Predicate<Object> nonNull = (t -> t != null);

	private Predicates() {}

	// Principles:
	// - flexible return type, remove need for caller to cast
	// - but caller needs to store Excepts.Predicate
	// - null predicate == no
	// - filters usually static final; don't worry too much about optimization

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
	public static <E extends Exception, T> Excepts.Predicate<E, T> eq(T value) {
		return t -> Objects.equals(t, value);
	}

	/**
	 * Returns true if the argument equals any of the values.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T> eqAny(T... values) {
		if (values == null) return no();
		return eqAny(Sets.ofAll(values));
	}

	/**
	 * Returns true if the argument equals any of the values. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		eqAny(Collection<? extends T> values) {
		if (values == null) return no();
		return values::contains;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.IntPredicate<E> eqInt(int value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.LongPredicate<E> eqLong(long value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <E extends Exception> Excepts.DoublePredicate<E> eqDouble(double value) {
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
		if (predicate == null) return no();
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
		and(Excepts.Predicate<? extends E, ? super T>... predicates) {
		if (ArrayUtil.isEmpty(predicates)) return yes();
		return and(Arrays.asList(predicates));
	}

	/**
	 * Returns true if all the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		and(Collection<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		if (Collectable.isEmpty(predicates)) return yes();
		return t -> {
			for (var predicate : predicates)
				if (predicate != null && !predicate.test(t)) return false;
			return true;
		};
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		or(Excepts.Predicate<? extends E, ? super T>... predicates) {
		if (ArrayUtil.isEmpty(predicates)) return ex(no);
		return or(Arrays.asList(predicates));
	}

	/**
	 * Returns true if any of the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		or(Collection<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		if (Collectable.isEmpty(predicates)) return ex(no);
		return t -> {
			for (var predicate : predicates)
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
		if (min == null) return yes();
		return t -> t != null && t.compareTo(min) >= 0;
	}

	/**
	 * Returns true if the argument > min and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T> gt(T min) {
		if (min == null) return yes();
		return t -> t != null && t.compareTo(min) > 0;
	}

	/**
	 * Returns true if the argument <= max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T>
		lte(T max) {
		if (max == null) return yes();
		return t -> t != null && t.compareTo(max) <= 0;
	}

	/**
	 * Returns true if the argument < max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T> lt(T max) {
		if (max == null) return yes();
		return t -> t != null && t.compareTo(max) < 0;
	}

	/**
	 * Returns true if the min <= argument <= max and not null.
	 */
	public static <E extends Exception, T extends Comparable<T>> Excepts.Predicate<E, T>
		range(T min, T max) {
		return and(gte(min), lte(max));
	}

	// Bi-predicates

	/**
	 * Returns true if the predicate applies for all values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAllOf(Excepts.BiPredicate<? extends E, T, U> predicate, U... values) {
		if (values == null) return ex(yes);
		return applyAll(predicate, Arrays.asList(values));
	}

	/**
	 * Returns true if the predicate applies for all values.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAll(Excepts.BiPredicate<? extends E, T, U> predicate, Iterable<U> values) {
		if (values == null) return ex(yes);
		return t -> {
			if (t == null) return false;
			for (var value : values)
				if (!predicate.test(t, value)) return false;
			return true;
		};
	}

	/**
	 * Returns true if the predicate applies for any value.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAnyOf(Excepts.BiPredicate<? extends E, T, U> predicate, U... values) {
		if (values == null) return ex(no);
		return applyAny(predicate, Arrays.asList(values));
	}

	/**
	 * Returns true if the predicate applies for any value.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAny(Excepts.BiPredicate<? extends E, T, U> predicate, Iterable<U> values) {
		if (values == null) return ex(no);
		return t -> {
			if (t == null) return false;
			for (var value : values)
				if (predicate.test(t, value)) return true;
			return false;
		};
	}
}
