package ceri.common.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.CollectionUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Methods for building predicates.
 */
public class Predicates {
	public static final Functions.Predicate<Object> YES = (_ -> true);
	public static final Functions.Predicate<Object> NO = (_ -> false);
	public static final Functions.Predicate<Object> IS_NULL = (t -> t == null);
	public static final Functions.Predicate<Object> NON_NULL = (t -> t != null);

	private Predicates() {}

	// Principles:
	// - Avoid rt() as it creates a new object (required for java predicates)
	// - ex() is cheap; use to ensure uniform exceptions across predicates
	// - Return Functions.Predicate if rt() is not needed

	// TODO:
	// - replace wrapped stream use with new streams
	// - look for general stream use
	// - replace predicates

	// Casting

	/**
	 * Converts a runtime exception predicate to a non-exception predicate. (Try to avoid)
	 */
	public static <T> Functions.Predicate<T>
		rt(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return predicate == null ? null : predicate::test;
	}

	/**
	 * Converts a runtime exception predicate to a non-exception predicate. (Try to avoid)
	 */
	public static Functions.IntPredicate
		rt(Excepts.IntPredicate<? extends RuntimeException> predicate) {
		return predicate == null ? null : predicate::test;
	}

	/**
	 * Converts a runtime exception predicate to a non-exception predicate. (Try to avoid)
	 */
	public static Functions.LongPredicate
		rt(Excepts.LongPredicate<? extends RuntimeException> predicate) {
		return predicate == null ? null : predicate::test;
	}

	/**
	 * Converts a runtime exception predicate to a non-exception predicate. (Try to avoid)
	 */
	public static Functions.DoublePredicate
		rt(Excepts.DoublePredicate<? extends RuntimeException> predicate) {
		return predicate == null ? null : predicate::test;
	}

	/**
	 * Casts a runtime exception predicate to an exception predicate.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		ex(Excepts.Predicate<? extends RuntimeException, ? super T> predicate) {
		return BasicUtil.unchecked(predicate);
	}

	/**
	 * Casts a runtime exception predicate to an exception predicate.
	 */
	public static <E extends Exception> Excepts.IntPredicate<E>
		ex(Excepts.IntPredicate<? extends RuntimeException> predicate) {
		return BasicUtil.unchecked(predicate);
	}

	/**
	 * Casts a runtime exception predicate to an exception predicate.
	 */
	public static <E extends Exception> Excepts.LongPredicate<E>
		ex(Excepts.LongPredicate<? extends RuntimeException> predicate) {
		return BasicUtil.unchecked(predicate);
	}

	/**
	 * Casts a runtime exception predicate to an exception predicate.
	 */
	public static <E extends Exception> Excepts.DoublePredicate<E>
		ex(Excepts.DoublePredicate<? extends RuntimeException> predicate) {
		return BasicUtil.unchecked(predicate);
	}

	// General predicates

	/**
	 * Returns true for all arguments.
	 */
	public static <T> Functions.Predicate<T> yes() {
		return BasicUtil.unchecked(YES);
	}

	/**
	 * Returns false for all arguments.
	 */
	public static <T> Functions.Predicate<T> no() {
		return BasicUtil.unchecked(NO);
	}

	/**
	 * Returns true if the argument is null.
	 */
	public static <T> Functions.Predicate<T> isNull() {
		return BasicUtil.unchecked(IS_NULL);
	}

	/**
	 * Returns true if the argument is not null.
	 */
	public static <T> Functions.Predicate<T> nonNull() {
		return BasicUtil.unchecked(NON_NULL);
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static <T> Functions.Predicate<T> eq(T value) {
		return t -> Objects.equals(t, value);
	}

	/**
	 * Returns true if the argument equals any of the values.
	 */
	@SafeVarargs
	public static <T> Functions.Predicate<T> eqAny(T... values) {
		if (values == null) return no();
		return eqAny(Arrays.asList(values));
	}

	/**
	 * Returns true if the argument equals any of the values. The collection is not copied.
	 */
	public static <T> Functions.Predicate<T> eqAny(Collection<? extends T> values) {
		if (CollectionUtil.empty(values)) return no();
		return t -> values.contains(t);
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.IntPredicate eqInt(int value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.LongPredicate eqLong(long value) {
		return t -> t == value;
	}

	/**
	 * Returns true if the argument equals the value.
	 */
	public static Functions.DoublePredicate eqDouble(double value) {
		return t -> t == value;
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
		if (predicate == null) return ex(nullResult ? YES : NO);
		return t -> (t == null ? nullResult : predicate.test(t));
	}

	/**
	 * Inverts a predicate. Nulls are passed to the predicate.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		not(Excepts.Predicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return t -> !predicate.test(t);
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T, R> Excepts.Predicate<E, T> testing(
		Excepts.Function<? extends E, ? super T, ? extends R> accessor,
		Excepts.Predicate<? extends E, ? super R> predicate) {
		if (predicate == null || accessor == null) return ex(NO);
		return t -> t != null && predicate.test(accessor.apply(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingInt(
		Excepts.ToIntFunction<? extends E, ? super T> accessor,
		Excepts.IntPredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return ex(NO);
		return t -> t != null && predicate.test(accessor.applyAsInt(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingLong(
		Excepts.ToLongFunction<? extends E, ? super T> accessor,
		Excepts.LongPredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return ex(NO);
		return t -> t != null && predicate.test(accessor.applyAsLong(t));
	}

	/**
	 * Transforms a predicate with accessor; returns false for null.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T> testingDouble(
		Excepts.ToDoubleFunction<? extends E, ? super T> accessor,
		Excepts.DoublePredicate<? extends E> predicate) {
		if (predicate == null || accessor == null) return ex(NO);
		return t -> t != null && predicate.test(accessor.applyAsDouble(t));
	}

	/**
	 * Transforms a bi-predicate into a map entry predicate.
	 */
	public static <E extends Exception, K, V> Excepts.Predicate<E, Map.Entry<K, V>>
		testingMapEntry(Excepts.BiPredicate<? extends E, ? super K, ? super V> predicate) {
		if (predicate == null) return ex(NO);
		return t -> t != null && predicate.test(t.getKey(), t.getValue());
	}

	// Composers

	/**
	 * Returns true if all the predicates return true.
	 */
	@SafeVarargs
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		and(Excepts.Predicate<? extends E, ? super T>... predicates) {
		if (ArrayUtil.isEmpty(predicates)) return ex(YES);
		return and(Arrays.asList(predicates));
	}

	/**
	 * Returns true if all the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		and(Collection<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		if (CollectionUtil.empty(predicates)) return ex(YES);
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
		if (ArrayUtil.isEmpty(predicates)) return ex(NO);
		return or(Arrays.asList(predicates));
	}

	/**
	 * Returns true if any of the predicates return true. The collection is not copied.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T>
		or(Collection<? extends Excepts.Predicate<? extends E, ? super T>> predicates) {
		if (CollectionUtil.empty(predicates)) return ex(NO);
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
	public static <T extends Comparable<T>> Functions.Predicate<T> gte(T min) {
		if (min == null) return yes();
		return t -> t != null && t.compareTo(min) >= 0;
	}

	/**
	 * Returns true if the argument > min and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> gt(T min) {
		if (min == null) return yes();
		return t -> t != null && t.compareTo(min) > 0;
	}

	/**
	 * Returns true if the argument <= max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> lte(T max) {
		if (max == null) return yes();
		return t -> t != null && t.compareTo(max) <= 0;
	}

	/**
	 * Returns true if the argument < max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> lt(T max) {
		if (max == null) return yes();
		return t -> t != null && t.compareTo(max) < 0;
	}

	/**
	 * Returns true if the min <= argument <= max and not null.
	 */
	public static <T extends Comparable<T>> Functions.Predicate<T> range(T min, T max) {
		var gte = gte(min);
		var lte = lte(max);
		return t -> t != null && gte.test(t) && lte.test(t);
	}

	// String predicates

	/**
	 * Returns true if the argument equals the string, ignoring case.
	 */
	public static Functions.Predicate<String> eqIgnoreCase(String value) {
		if (value == null) return isNull();
		return t -> t != null && value.equalsIgnoreCase(t);
	}

	/**
	 * Predicate that returns true for strings that contain the given substring.
	 */
	public static Functions.Predicate<String> contains(CharSequence s) {
		if (s == null) return isNull();
		if (s.isEmpty()) return yes();
		return t -> t != null && t.contains(s);
	}

	/**
	 * Predicate that returns true for strings that contain the given substring.
	 */
	public static Functions.Predicate<String> containsIgnoreCase(String s) {
		if (s == null) return isNull();
		if (s.isEmpty()) return yes();
		return t -> t != null && StringUtil.containsIgnoreCase(t, s);
	}

	/**
	 * Returns true if the argument contains the pattern.
	 */
	public static Functions.Predicate<CharSequence> find(Pattern pattern) {
		if (pattern == null) return isNull();
		return t -> t != null && pattern.matcher(t).find();
	}

	/**
	 * Returns true if the argument matches the pattern.
	 */
	public static Functions.Predicate<String> match(Pattern pattern) {
		if (pattern == null) return isNull();
		return t -> t != null && pattern.matcher(t).matches();
	}

	/**
	 * Returns true if the argument matches the pattern.
	 */
	public static <E extends Exception> Excepts.Predicate<E, String> matching(Pattern pattern,
		Excepts.Predicate<? extends E, ? super Matcher> predicate) {
		if (pattern == null) return ex(IS_NULL);
		if (predicate == null) return ex(NO);
		return t -> t != null && predicate.test(pattern.matcher(t));
	}

	// Array predicates

	/**
	 * Predicate that returns true if any element and index matches.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T[]>
		arrayAny(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			for (int i = 0; i < ts.length; i++)
				if (predicate.test(ts[i], i)) return true;
			return false;
		};
	}

	/**
	 * Predicate that returns true if any element and index matches.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, T[]>
		arrayAll(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			for (int i = 0; i < ts.length; i++)
				if (!predicate.test(ts[i], i)) return false;
			return true;
		};
	}

	// Iterable predicates

	/**
	 * Predicate that returns true if any element matches.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
		forAny(Excepts.Predicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			for (T t : ts)
				if (predicate.test(t)) return true;
			return false;
		};
	}

	/**
	 * Predicate that returns true if all elements match.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
		forAll(Excepts.Predicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			for (T t : ts)
				if (!predicate.test(t)) return false;
			return true;
		};
	}

	/**
	 * Predicate that returns true if any indexed element matches.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
		forAnyIndex(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			int i = 0;
			for (T t : ts)
				if (predicate.test(t, i++)) return true;
			return false;
		};
	}

	/**
	 * Predicate that returns true if all indexed elements match.
	 */
	public static <E extends Exception, T> Excepts.Predicate<E, Iterable<T>>
		forAllIndex(Excepts.ObjIntPredicate<? extends E, ? super T> predicate) {
		if (predicate == null) return ex(NO);
		return ts -> {
			if (ts == null) return false;
			int i = 0;
			for (T t : ts)
				if (!predicate.test(t, i++)) return false;
			return true;
		};
	}

	// Collection predicates

	/**
	 * Predicate that returns true if a collection contains all the values.
	 */
	public static <T> Functions.Predicate<Collection<T>> containsItem(T value) {
		return ts -> ts != null && ts.contains(value);
	}

	/**
	 * Predicate that returns true if a collection contains all the values.
	 */
	@SafeVarargs
	public static <T> Functions.Predicate<Collection<T>> containsAll(T... values) {
		if (values == null) return isNull();
		return containsAll(Arrays.asList(values));
	}

	/**
	 * Predicate that returns true if a collection contains all the values.
	 */
	public static <T> Functions.Predicate<Collection<T>>
		containsAll(Collection<? extends T> values) {
		if (values == null) return isNull();
		return ts -> ts != null && ts.containsAll(ts);
	}

	// Bi-predicates

	/**
	 * Returns true if the predicate applies for all values.
	 */
	@SafeVarargs
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAllOf(Excepts.BiPredicate<? extends E, T, U> predicate, U... values) {
		if (values == null) return ex(YES);
		return applyAll(predicate, Arrays.asList(values));
	}

	/**
	 * Returns true if the predicate applies for all values.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAll(Excepts.BiPredicate<? extends E, T, U> predicate, Iterable<U> values) {
		if (values == null) return ex(YES);
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
		if (values == null) return ex(NO);
		return applyAny(predicate, Arrays.asList(values));
	}

	/**
	 * Returns true if the predicate applies for any value.
	 */
	public static <E extends Exception, T, U> Excepts.Predicate<E, T>
		applyAny(Excepts.BiPredicate<? extends E, T, U> predicate, Iterable<U> values) {
		if (values == null) return ex(NO);
		return t -> {
			if (t == null) return false;
			for (var value : values)
				if (predicate.test(t, value)) return true;
			return false;
		};
	}

}
