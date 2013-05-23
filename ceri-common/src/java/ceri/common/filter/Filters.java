package ceri.common.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.util.BasicUtil;

/**
 * Basic filters and filter utilities.
 */
public class Filters {
	
	private static final Filter<Object> NULL = new Filter<Object>() {
		@Override
		public boolean filter(Object t) {
			return true;
		}
	};

	private Filters() {}	
	
	/**
	 * Applies a filter to a collection, removing items that do not match.
	 */
	public static <T> void filter(Collection<T> ts, Filter<? super T> filter) {
		for (Iterator<T> i = ts.iterator(); i.hasNext();) {
			T t = i.next();
			if (t == null || !filter.filter(t)) i.remove();
		}
	}

	/**
	 * A null filter, returns true for all conditions.
	 */
	public static <T> Filter<T> nul() {
		return BasicUtil.uncheckedCast(NULL);
	}

	/**
	 * Returns true if value equals any of the given values.
	 */
	@SafeVarargs
	public static <T> Filter<T> eqAny(final T...values) {
		List<Filter<T>> filters = new ArrayList<>();
		for (T value : values) filters.add(eq(value));
		return any(filters);
	}
	
	/**
	 * Returns true if value equals given value.
	 */
	public static <T> Filter<T> eq(final T value) {
		return new Filter<T>() {
			@Override
			public boolean filter(T t) {
				if (t == value) return true;
				if (t == null || value == null) return false;
				return t.equals(value);
			}
		};
	}

	/**
	 * Inverts a given filter.
	 */
	public static <T> Filter<T> not(final Filter<? super T> filter) {
		return new Filter<T>() {
			@Override
			public boolean filter(T t) {
				return !filter.filter(t);
			}
		};
	}

	/**
	 * Combines filters to return true if any filter matches.
	 */
	@SafeVarargs
	public static <T> Filter<T> any(final Filter<? super T>... filters) {
		if (filters == null || filters.length == 0) return nul();
		return any(Arrays.asList(filters));
	}

	/**
	 * Combines filters to return true only if all filters match.
	 */
	@SafeVarargs
	public static <T> Filter<T> all(final Filter<? super T>... filters) {
		if (filters == null || filters.length == 0) return nul();
		return all(Arrays.asList(filters));
	}

	/**
	 * Combines filters to return true if any filter matches.
	 */
	public static <T> Filter<T> any(final Collection<? extends Filter<? super T>> filters) {
		if (filters == null || filters.isEmpty()) return nul();
		return new Filter<T>() {
			@Override
			public boolean filter(T t) {
				for (Filter<? super T> filter : filters)
					if (filter.filter(t)) return true;
				return false;
			}
		};
	}

	/**
	 * Combines filters to return true only if all filters match.
	 */
	public static <T> Filter<T> all(final Collection<? extends Filter<? super T>> filters) {
		if (filters == null || filters.isEmpty()) return nul();
		return new Filter<T>() {
			@Override
			public boolean filter(T t) {
				for (Filter<? super T> filter : filters)
					if (!filter.filter(t)) return false;
				return true;
			}
		};
	}

	/**
	 * Filter that returns true for strings that match the given pattern.
	 */
	public static Filter<String> pattern(String pattern) {
		return pattern(Pattern.compile(pattern));
	}
	
	/**
	 * Filter that returns true for strings that match the given pattern.
	 */
	public static Filter<String> pattern(final Pattern pattern) {
		return new BaseFilter<String>() {
			@Override
			public boolean filterNonNull(String s) {
				return pattern.matcher(s).find();
			}
		};
	}

	/**
	 * Filter that returns true for strings that contain the given substring.
	 * Can specify to ignore case.
	 */
	public static Filter<String> contains(String str, final boolean ignoreCase) {
		final String containsStr = ignoreCase ? str.toLowerCase() : str;
		return new BaseFilter<String>() {
			@Override
			public boolean filterNonNull(String s) {
				String str = ignoreCase ? s.toLowerCase() : s;
				return str.contains(containsStr);
			}
		};
	}

	/**
	 * Comparable filter that returns true if the value >= given minimum value.
	 */
	public static <T extends Comparable<T>> Filter<T> min(final T min) {
		return new BaseFilter<T>() {
			@Override
			public boolean filterNonNull(T t) {
				return t.compareTo(min) >= 0;
			}
		};
	}

	/**
	 * Comparable filter that returns true if the value <= given maximum value.
	 */
	public static <T extends Comparable<T>> Filter<T> max(final T max) {
		return new BaseFilter<T>() {
			@Override
			public boolean filterNonNull(T t) {
				return t.compareTo(max) <= 0;
			}
		};
	}

	/**
	 * Comparable filter that returns true if the value is between given values.
	 * A null limit value means don't check that end of the limit.
	 */
	public static <T extends Comparable<T>> Filter<T> range(final T min, final T max) {
		return new Filter<T>() {
			@Override
			public boolean filter(T t) {
				if (min == null && max == null) return true;
				if (t == null) return false;
				if (min != null && t.compareTo(min) < 0) return false;
				if (max != null && t.compareTo(max) > 0) return false;
				return true;
			};
		};
	}
	
}
