package ceri.common.filter;

import java.util.function.Predicate;

/**
 * Filter interface - returns true if the conditions are met.
 * Was created before Predicate; retrofitted to match.
 */
public interface Filter<T> extends Predicate<T> {

	boolean filter(T t);

	@Override
	default boolean test(T t) {
		return filter(t);
	}

}
