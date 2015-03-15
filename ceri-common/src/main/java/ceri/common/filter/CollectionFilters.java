package ceri.common.filter;

import java.util.Collection;
import java.util.List;

/**
 * Simple filters to be applied to collections.
 */
public class CollectionFilters {

	private CollectionFilters() {}

	/**
	 * Filter that returns true if the collection is not empty.
	 */
	public static <T> Filter<Collection<T>> notEmpty() {
		return (ts -> !ts.isEmpty());
	}

	/**
	 * Filter that applies given filter to the size of the collection.
	 */
	public static <T> Filter<Collection<T>> size(final Filter<? super Integer> filter) {
		return (ts -> filter.filter(ts.size()));
	}

	/**
	 * Filter that applies given filter to item at index i of the list.
	 */
	public static <T> Filter<List<T>> atIndex(final int i, final Filter<? super T> filter) {
		return (ts -> {
			if (i >= ts.size()) return false;
			return filter.filter(ts.get(i));
		});
	}

	/**
	 * Filter that returns true if the given filter matches any elements in the collection.
	 */
	public static <T> Filter<Collection<T>> any(final Filter<? super T> filter) {
		return (ts -> {
			for (T t : ts)
				if (filter.filter(t)) return true;
			return false;
		});
	}

	/**
	 * Filter that only returns true if the given filter matches all elements in the collection.
	 */
	public static <T> Filter<Collection<T>> all(final Filter<? super T> filter) {
		return (ts -> {
			for (T t : ts)
				if (!filter.filter(t)) return false;
			return true;
		});
	}

}
