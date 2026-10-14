package ceri.common.function;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import ceri.common.collect.Collectable;
import ceri.common.collect.Lists;
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
	public static final Functions.BiPredicate<Object, Object> EQUALS = Objects::equals;

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

	// Casting

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		cast(Excepts.Predicate<? extends E, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		castEx(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception> Excepts.IntPredicate<E>
		castIntEx(Excepts.IntPredicate<? super RuntimeException> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception> Excepts.LongPredicate<E>
		castLongEx(Excepts.LongPredicate<? super RuntimeException> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception> Excepts.DoublePredicate<E>
		castDoubleEx(Excepts.DoublePredicate<? super RuntimeException> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T, U> Excepts.BiPredicate<E, T, U>
		castEx(Excepts.BiPredicate<? extends RuntimeException, ? super T, ? super U> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.ObjBoolPredicate<E, T>
		castEx(Excepts.ObjBoolPredicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.ObjBytePredicate<E, T>
		castEx(Excepts.ObjBytePredicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.ObjIntPredicate<E, T>
		castEx(Excepts.ObjIntPredicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.ObjLongPredicate<E, T>
		castEx(Excepts.ObjLongPredicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	/**
	 * Adapts type parameters.
	 */
	public static <E extends Exception, T> Excepts.ObjDoublePredicate<E, T>
		castEx(Excepts.ObjDoublePredicate<? extends RuntimeException, ? super T> predicate) {
		return Reflect.unchecked(predicate);
	}

	// Execution

	/**
	 * Execute the predicate with given null behavior.
	 */
	public static <E extends Exception, T> boolean test(Nulls nulls, T t,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		return switch (nulls) {
			case no -> t == null ? false : test(t, predicate);
			case yes -> t == null ? true : test(t, predicate);
			case fail -> test(Validate.nonNull(t), predicate);
			case null, default -> test(t, predicate);
		};
	}

	/**
	 * Execute the bi-predicate with given null behavior.
	 */
	public static <E extends Exception, T, U> boolean test(Nulls nulls, T t, U u,
		Excepts.BiPredicate<E, ? super T, ? super U> predicate) throws E {
		return switch (nulls) {
			case no -> t == null || u == null ? false : test(t, u, predicate);
			case yes -> t == null || u == null ? true : test(t, u, predicate);
			case fail -> test(Validate.nonNull(t), Validate.nonNull(u), predicate);
			case null, default -> test(t, u, predicate);
		};
	}

	/**
	 * Returns true if any predicate returns true, with given null behavior.
	 */
	@SafeVarargs
	public static <E extends Exception, T> boolean testAnyOf(Nulls nulls, T t,
		Excepts.Predicate<? extends E, ? super T>... predicates) throws E {
		return testAny(nulls, t, listOf(predicates));
	}

	/**
	 * Returns true if any predicate returns true, with given null behavior.
	 */
	public static <E extends Exception, T> boolean testAny(Nulls nulls, T t,
		Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) throws E {
		for (var predicate : predicates)
			if (predicate != null && test(nulls, t, predicate)) return true;
		return false;
	}

	/**
	 * Returns true if all predicates return true, with given null behavior.
	 */
	@SafeVarargs
	public static <E extends Exception, T> boolean testAllOf(Nulls nulls, T t,
		Excepts.Predicate<? extends E, ? super T>... predicates) throws E {
		return testAll(nulls, t, listOf(predicates));
	}

	/**
	 * Returns true if all predicates return true, with given null behavior.
	 */
	public static <E extends Exception, T> boolean testAll(Nulls nulls, T t,
		Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) throws E {
		for (var predicate : predicates)
			if (predicate == null || !test(nulls, t, predicate)) return false;
		return true;
	}

	/**
	 * Returns true if the bi-predicate returns true for any element, with given null behavior.
	 */
	public static <E extends Exception, T, U> boolean testAnyElement(Nulls nulls, T t,
		Iterable<U> iterable, Excepts.BiPredicate<E, ? super T, ? super U> predicate) throws E {
		if (iterable == null) return false;
		for (var u : iterable)
			if (test(nulls, t, u, predicate)) return true;
		return false;
	}

	/**
	 * Returns true if the bi-predicate returns true for all elements, with given null behavior.
	 */
	public static <E extends Exception, T, U> boolean testAllElements(Nulls nulls, T t,
		Iterable<U> iterable, Excepts.BiPredicate<E, ? super T, ? super U> predicate) throws E {
		if (iterable == null) return false;
		for (var u : iterable)
			if (!test(nulls, t, u, predicate)) return false;
		return true;
	}

	// General predicates

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <T> Functions.Predicate<T> equal(T value) {
		return t -> Objects.equals(t, value);
	}

	/**
	 * Returns true if the argument equals any of the values.
	 */
	@SafeVarargs
	public static <T> Functions.Predicate<T> equalAnyOf(T... values) {
		if (values == null) return NO.cast();
		return contained(Lists.wrap(values));
	}

	/**
	 * Returns true if the argument equals any of the values. The collection is not copied.
	 */
	public static <T> Functions.Predicate<T> equalAny(Iterable<? extends T> values) {
		if (values == null) return NO.cast();
		return t -> testAnyElement(Nulls.def, t, values, EQUALS);
	}

	/**
	 * Returns true if the argument equals any of the values. The collection is not copied.
	 */
	public static <T> Functions.Predicate<T> contained(Collection<?> values) {
		if (Collectable.isEmpty(values)) return NO.cast();
		return values::contains;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.IntPredicate equal(int value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.LongPredicate equal(long value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.DoublePredicate equal(double value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument is an instance of the class. A null class matches all.
	 */
	public static <T> Functions.Predicate<T> instance(Class<?> cls) {
		return t -> cls == null || cls.isInstance(t);
	}

	// Adapters

	/**
	 * Wraps a predicate with default null behavior.
	 */
	public static <T> Functions.Predicate<T>
		of(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return of(Nulls.def, predicate);
	}

	/**
	 * Wraps a predicate with given null behavior.
	 */
	public static <T> Functions.Predicate<T> of(Nulls nulls,
		Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return t -> test(nulls, t, predicate);
	}

	/**
	 * Wraps a predicate with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		ofEx(Excepts.Predicate<? extends E, ? super T> predicate) {
		return ofEx(Nulls.def, predicate);
	}

	/**
	 * Wraps a predicate with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> ofEx(Nulls nulls,
		Excepts.Predicate<? extends E, ? super T> predicate) {
		return t -> test(nulls, t, predicate);
	}

	/**
	 * Returns a no predicate if the predicate is null.
	 */
	public static <T> Functions.Predicate<T> safe(Functions.Predicate<T> predicate) {
		return predicate == null ? NO.cast() : predicate;
	}

	/**
	 * Returns a no predicate if the predicate is null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		safeEx(Excepts.Predicate<E, T> predicate) {
		return predicate == null ? NO.castEx() : predicate;
	}

	/**
	 * Reverses a predicate, with default null behavior.
	 */
	public static <T> Functions.Predicate<T>
		not(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return not(Nulls.def, predicate);
	}

	/**
	 * Reverses a predicate, with given null behavior.
	 */
	public static <T> Functions.Predicate<T> not(Nulls nulls,
		Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		var reversed = reverse(predicate);
		return t -> test(nulls, t, reversed);
	}

	/**
	 * Reverses a predicate, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		notEx(Excepts.Predicate<? extends E, ? super T> predicate) {
		return notEx(Nulls.def, predicate);
	}

	/**
	 * Reverses a predicate, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> notEx(Nulls nulls,
		Excepts.Predicate<? extends E, ? super T> predicate) {
		var reversed = reverse(predicate);
		return t -> test(nulls, t, reversed);
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <T, U> Functions.Predicate<T> as(
		Excepts.Function<? extends RuntimeException, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends RuntimeException, ? super U> predicate) {
		return as(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <T, U> Functions.Predicate<T> as(Nulls nulls,
		Excepts.Function<? extends RuntimeException, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends RuntimeException, ? super U> predicate) {
		return t -> test(nulls, Functional.apply(accessor, t), predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T> asEx(
		Excepts.Function<? extends E, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends E, ? super U> predicate) {
		return asEx(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T> asEx(Nulls nulls,
		Excepts.Function<? extends E, ? super T, ? extends U> accessor,
		Excepts.Predicate<? extends E, ? super U> predicate) {
		return t -> test(nulls, Functional.apply(accessor, t), predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <T> Functions.Predicate<T> asInt(
		Excepts.ToIntFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.IntPredicate<? extends RuntimeException> predicate) {
		return asInt(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <T> Functions.Predicate<T> asInt(Nulls nulls,
		Excepts.ToIntFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.IntPredicate<? extends RuntimeException> predicate) {
		return accessor == null ? of(nulls, NO.cast()) :
			of(nulls, t -> testInt(accessor.applyAsInt(t), predicate));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asIntEx(
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		return asIntEx(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asIntEx(Nulls nulls,
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		return accessor == null ? ofEx(nulls, NO.castEx()) :
			ofEx(nulls, t -> testInt(accessor.applyAsInt(t), predicate));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <T> Functions.Predicate<T> asLong(
		Excepts.ToLongFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.LongPredicate<? extends RuntimeException> predicate) {
		return asLong(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <T> Functions.Predicate<T> asLong(Nulls nulls,
		Excepts.ToLongFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.LongPredicate<? extends RuntimeException> predicate) {
		return accessor == null ? of(nulls, NO.cast()) :
			of(nulls, t -> testLong(accessor.applyAsLong(t), predicate));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asLongEx(
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		return asLongEx(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asLongEx(Nulls nulls,
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		return accessor == null ? ofEx(nulls, NO.castEx()) :
			ofEx(nulls, t -> testLong(accessor.applyAsLong(t), predicate));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <T> Functions.Predicate<T> asDouble(
		Excepts.ToDoubleFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.DoublePredicate<? extends RuntimeException> predicate) {
		return asDouble(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <T> Functions.Predicate<T> asDouble(Nulls nulls,
		Excepts.ToDoubleFunction<? extends RuntimeException, ? super T> accessor,
		Excepts.DoublePredicate<? extends RuntimeException> predicate) {
		return accessor == null ? of(nulls, NO.cast()) :
			of(nulls, t -> testDouble(accessor.applyAsDouble(t), predicate));
	}

	/**
	 * Adapts a predicate using an accessor, with default null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asDoubleEx(
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		return asDoubleEx(Nulls.def, accessor, predicate);
	}

	/**
	 * Adapts a predicate using an accessor, with given null behavior.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> asDoubleEx(Nulls nulls,
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		return accessor == null ? ofEx(nulls, NO.castEx()) :
			ofEx(nulls, t -> testDouble(accessor.applyAsDouble(t), predicate));
	}

	/**
	 * Adapts a predicate using accessors, with default null behavior.
	 */
	public static <T, U, V> Functions.Predicate<T> biAs(
		Excepts.Function<? extends RuntimeException, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends RuntimeException, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends RuntimeException, ? super U, ? super V> predicate) {
		return biAs(Nulls.def, uAccessor, vAccessor, predicate);
	}

	/**
	 * Adapts a predicate using accessors, with given null behavior.
	 */
	public static <T, U, V> Functions.Predicate<T> biAs(Nulls nulls,
		Excepts.Function<? extends RuntimeException, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends RuntimeException, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends RuntimeException, ? super U, ? super V> predicate) {
		return t -> test(nulls, Functional.apply(uAccessor, t), Functional.apply(vAccessor, t),
			predicate);
	}

	/**
	 * Adapts a predicate using accessors, with default null behavior.
	 */
	public static <E extends Exception, T, U, V> Excepts.Predicate<E, T> biAsEx(
		Excepts.Function<? extends E, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends E, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends E, ? super U, ? super V> predicate) {
		return biAsEx(Nulls.def, uAccessor, vAccessor, predicate);
	}

	/**
	 * Adapts a predicate using accessors, with given null behavior.
	 */
	public static <E extends Exception, T, U, V> Excepts.Predicate<E, T> biAsEx(Nulls nulls,
		Excepts.Function<? extends E, ? super T, ? extends U> uAccessor,
		Excepts.Function<? extends E, ? super T, ? extends V> vAccessor,
		Excepts.BiPredicate<? extends E, ? super U, ? super V> predicate) {
		return t -> test(nulls, Functional.apply(uAccessor, t), Functional.apply(vAccessor, t),
			predicate);
	}

	// Composers

	/**
	 * Returns true if all the predicates return true.
	 */
	@SafeVarargs
	public static <T> Functions.Predicate<T>
		andOf(Excepts.Predicate<? extends RuntimeException, ? super T>... predicates) {
		return and(listOf(predicates));
	}

	/**
	 * Returns true if all the predicates return true.
	 */
	public static <T> Functions.Predicate<T> and(
		Iterable<? extends Excepts.Predicate<? extends RuntimeException, ? super T>> predicates) {
		return t -> testAll(Nulls.def, t, predicates);
	}

	/**
	 * Returns true if all the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		andOfEx(Excepts.Predicate<? extends E, ? super T>... predicates) {
		return andEx(listOf(predicates));
	}

	/**
	 * Returns true if all the predicates return true.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		andEx(Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		return t -> testAll(Nulls.def, t, predicates);
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	@SafeVarargs
	public static <T> Functions.Predicate<T>
		orOf(Excepts.Predicate<? extends RuntimeException, ? super T>... predicates) {
		return or(listOf(predicates));
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	public static <T> Functions.Predicate<T> or(
		Iterable<? extends Excepts.Predicate<? extends RuntimeException, ? super T>> predicates) {
		return t -> testAny(Nulls.def, t, predicates);
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		orOfEx(Excepts.Predicate<? extends E, ? super T>... predicates) {
		return orEx(Lists.wrap(predicates));
	}

	/**
	 * Returns true if any of the predicates return true.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		orEx(Iterable<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		return t -> testAny(Nulls.def, t, predicates);
	}

	// Comparable predicates

	/**
	 * Returns true if the argument >= min and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> gte(T min) {
		return t -> t != null && (min == null || t.compareTo(min) >= 0);
	}

	/**
	 * Returns true if the argument > min and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> gt(T min) {
		return t -> t != null && (min == null || t.compareTo(min) > 0);
	}

	/**
	 * Returns true if the argument <= max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> lte(T max) {
		return t -> t != null && (max == null || t.compareTo(max) <= 0);
	}

	/**
	 * Returns true if the argument < max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> lt(T max) {
		return t -> t != null && (max == null || t.compareTo(max) < 0);
	}

	/**
	 * Returns true if the min <= argument <= max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> range(T min, T max) {
		return andOf(gte(min), lte(max));
	}

	// support

	private static <E extends Exception, T> Excepts.Predicate<E, T>
		reverse(Excepts.Predicate<? extends E, ? super T> predicate) {
		return predicate == null ? null : t -> !predicate.test(t);
	}

	private static <E extends Exception, T> boolean test(T t,
		Excepts.Predicate<E, ? super T> predicate) throws E {
		return predicate == null ? false : predicate.test(t);
	}

	private static <E extends Exception> boolean testInt(int i, Excepts.IntPredicate<E> predicate)
		throws E {
		return predicate == null ? false : predicate.test(i);
	}

	private static <E extends Exception> boolean testLong(long l,
		Excepts.LongPredicate<E> predicate) throws E {
		return predicate == null ? false : predicate.test(l);
	}

	private static <E extends Exception> boolean testDouble(double d,
		Excepts.DoublePredicate<E> predicate) throws E {
		return predicate == null ? false : predicate.test(d);
	}

	private static <E extends Exception, T, U> boolean test(T t, U u,
		Excepts.BiPredicate<E, ? super T, ? super U> predicate) throws E {
		return predicate == null ? false : predicate.test(t, u);
	}

	@SafeVarargs
	private static <E extends Exception, T> List<Excepts.Predicate<E, T>>
		listOf(Excepts.Predicate<? extends E, ? super T>... predicates) {
		return Reflect.unchecked(Lists.wrap(predicates));
	}
}
