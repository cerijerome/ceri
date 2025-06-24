package ceri.common.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import ceri.common.collection.CollectionUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Helpers for building predicates.
 */
public class Predicates {
	private static final Predicate<Object> YES = (_ -> true);
	private static final Predicate<Object> NO = (_ -> false);

	private Predicates() {}

	// General predicates

	/**
	 * A filter that returns true for all conditions.
	 */
	public static <T> Predicate<T> yes() {
		return BasicUtil.uncheckedCast(YES);
	}

	/**
	 * A filter that returns false for all conditions.
	 */
	public static <T> Predicate<T> no() {
		return BasicUtil.uncheckedCast(NO);
	}

	/**
	 * Inverts a given filter.
	 */
	public static <T> Predicate<T> not(final Predicate<? super T> filter) {
		if (filter == null) return no();
		return nonNull(t -> !filter.test(t));
	}

	/**
	 * A filter that returns true if the value is null.
	 */
	public static <T> Predicate<T> isNull() {
		return Objects::isNull;
	}

	/**
	 * Returns true if value equals given value.
	 */
	public static <T> Predicate<T> eq(final T value) {
		return t -> Objects.equals(t, value);
	}

	/**
	 * Returns true if value equals any of the given values.
	 */
	@SafeVarargs
	public static <T> Predicate<T> eqAny(final T... values) {
		return eqAny(Arrays.asList(values));
	}

	/**
	 * Returns true if value equals any of the given values.
	 */
	public static <T> Predicate<T> eqAny(Collection<? extends T> values) {
		List<Predicate<T>> filters = new ArrayList<>();
		for (T value : values)
			filters.add(eq(value));
		return any(filters);
	}

	/**
	 * Wraps a filter, returning false for null values.
	 */
	public static <T> Predicate<T> nonNull(final Predicate<? super T> filter) {
		if (filter == null) return no();
		return (t -> t != null && filter.test(t));
	}

	/**
	 * Combines filters to return true if any filter matches.
	 */
	@SafeVarargs
	public static <T> Predicate<T> any(final Predicate<? super T>... filters) {
		var list = Arrays.asList(filters);
		return any(list);
	}

	/**
	 * Combines filters to return true only if all filters match.
	 */
	@SafeVarargs
	public static <T> Predicate<T> all(final Predicate<? super T>... filters) {
		var list = Arrays.asList(filters);
		return all(list);
	}

	/**
	 * Combines filters to return true if any filter matches.
	 */
	public static <T> Predicate<T> any(final Collection<? extends Predicate<? super T>> filters) {
		if (CollectionUtil.empty(filters)) return no();
		return nonNull(t -> {
			for (Predicate<? super T> filter : filters)
				if (filter.test(t)) return true;
			return false;
		});
	}

	/**
	 * Combines filters to return true only if all filters match.
	 */
	public static <T> Predicate<T> all(final Collection<? extends Predicate<? super T>> filters) {
		if (CollectionUtil.empty(filters)) return yes();
		return nonNull(t -> {
			for (Predicate<? super T> filter : filters)
				if (!filter.test(t)) return false;
			return true;
		});
	}

	// Adapter predicates
	
	/**
	 * Transforms a predicate of one type to another using an accessor.
	 */
	public static <T, R> Predicate<T> testing(Function<? super T, ? extends R> accessor,
		Predicate<? super R> predicate) {
		return t -> predicate.test(t == null ? null : accessor.apply(t));
	}

	/**
	 * Transforms a predicate of one type to another using an accessor.
	 */
	public static <T> Predicate<T> testingInt(ToIntFunction<? super T> accessor,
		IntPredicate predicate) {
		return t -> predicate.test(t == null ? null : accessor.applyAsInt(t));
	}

	/**
	 * Transforms a predicate of one type to another using an accessor.
	 */
	public static <T> Predicate<T> testingLong(ToLongFunction<? super T> accessor,
		LongPredicate predicate) {
		return t -> predicate.test(t == null ? null : accessor.applyAsLong(t));
	}

	/**
	 * Transforms a predicate of one type to another using an accessor.
	 */
	public static <T> Predicate<T> testingDouble(ToDoubleFunction<? super T> accessor,
		DoublePredicate predicate) {
		return t -> predicate.test(t == null ? null : accessor.applyAsDouble(t));
	}

	// Comparable predicates

	/**
	 * Comparable filter that returns true if the value >= given minimum value.
	 */
	public static <T extends Comparable<T>> Predicate<T> gte(final T min) {
		if (min == null) return yes();
		return nonNull(t -> t.compareTo(min) >= 0);
	}

	/**
	 * Comparable filter that returns true if the value > given minimum value.
	 */
	public static <T extends Comparable<T>> Predicate<T> gt(final T min) {
		if (min == null) return yes();
		return nonNull(t -> t.compareTo(min) > 0);
	}

	/**
	 * Comparable filter that returns true if the value <= given maximum value.
	 */
	public static <T extends Comparable<T>> Predicate<T> lte(final T max) {
		if (max == null) return yes();
		return nonNull(t -> t.compareTo(max) <= 0);
	}

	/**
	 * Comparable filter that returns true if the value < given maximum value.
	 */
	public static <T extends Comparable<T>> Predicate<T> lt(final T max) {
		if (max == null) return yes();
		return nonNull(t -> t.compareTo(max) < 0);
	}

	/**
	 * Comparable filter that returns true if the value is between given values. A null limit value
	 * means don't check that end of the limit.
	 */
	public static <T extends Comparable<T>> Predicate<T> range(final T min, final T max) {
		if (min == null && max == null) return yes();
		return nonNull(t -> {
			if (min != null && t.compareTo(min) < 0) return false;
			if (max != null && t.compareTo(max) > 0) return false;
			return true;
		});
	}

	// String predicates

	/**
	 * Predicate that returns true for strings that equal the given substring, ignoring case.
	 */
	public static Predicate<String> eqIgnoreCase(String value) {
		return (t -> {
			if (t == value) return true;
			if (t == null || value == null) return false;
			return t.equalsIgnoreCase(value);

		});
	}

	/**
	 * Predicate that returns true for strings that match the given pattern.
	 */
	public static Predicate<String> pattern(String pattern) {
		if (StringUtil.blank(pattern)) return yes();
		return pattern(Pattern.compile(pattern));
	}

	/**
	 * Predicate that returns true for strings that match the given pattern.
	 */
	public static Predicate<String> pattern(final Pattern pattern) {
		if (pattern == null) return yes();
		return nonNull(s -> pattern.matcher(s).find());
	}

	/**
	 * Predicate that returns true for strings that contain the given substring.
	 */
	public static Predicate<String> contains(String str) {
		if (str == null || str.isEmpty()) return yes();
		return nonNull(s -> s.contains(str));
	}

	/**
	 * Predicate that returns true for strings that contain the given substring, ignoring case.
	 */
	public static Predicate<String> containsIgnoreCase(String str) {
		if (str == null || str.isEmpty()) return yes();
		String lowerStr = str.toLowerCase();
		return nonNull(s -> s.toLowerCase().contains(lowerStr));
	}

	/**
	 * Predicate that converts strings to lower case before applying the next filter.
	 */
	public static Predicate<String> lower(final Predicate<? super String> filter) {
		if (filter == null) return yes();
		return nonNull(s -> {
			String lower = s.toLowerCase();
			return filter.test(lower);
		});
	}

	// Enum predicates

	/**
	 * Predicate to match enum name.
	 */
	public static <T extends Enum<T>> Predicate<T> name(String name) {
		return Predicates.name(eq(name));
	}

	/**
	 * Predicate applied to enum name.
	 */
	public static <T extends Enum<T>> Predicate<T> name(Predicate<String> filter) {
		return nonNull(t -> filter.test(t.name()));
	}

	// Collection predicates

	/**
	 * Filter that applies given filter to the size of the collection.
	 */
	public static <T> Predicate<Collection<T>> forSize(Predicate<? super Integer> filter) {
		return nonNull(ts -> filter.test(ts.size()));
	}

	/**
	 * Filter that applies given filter to item at index i of the list.
	 */
	public static <T> Predicate<List<T>> forIndex(int i, Predicate<? super T> filter) {
		return nonNull(ts -> {
			if (i >= ts.size()) return false;
			return filter.test(ts.get(i));
		});
	}

	/**
	 * Filter that returns true if the given filter matches any elements in the collection.
	 */
	public static <T> Predicate<Collection<T>> forAny(Predicate<? super T> filter) {
		return nonNull(ts -> {
			for (T t : ts)
				if (filter.test(t)) return true;
			return false;
		});
	}

	/**
	 * Filter that only returns true if the given filter matches all elements in the collection.
	 */
	public static <T> Predicate<Collection<T>> forAll(Predicate<? super T> filter) {
		return nonNull(ts -> {
			for (T t : ts)
				if (!filter.test(t)) return false;
			return true;
		});
	}

	// Action methods

	/**
	 * Applies a filter to a collection, removing items that do not match.
	 */
	public static <T> void remove(Iterable<T> ts, Predicate<? super T> filter) {
		for (Iterator<T> i = ts.iterator(); i.hasNext();) {
			T t = i.next();
			if (!filter.test(t)) i.remove();
		}
	}
}
