package ceri.common.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple filters to be applied to collections.
 */
public class CollectionFilters {

	private CollectionFilters() {}

	/**
	 * Filter that applies given filter to the keys of a map.
	 */
	public static <K, V> Filter<Map<K, V>> mapKeys(Filter<? super Set<K>> filter) {
		return Filters.nonNull(map -> filter.filter(map.keySet()));
	}
	
	/**
	 * Filter that applies given filter to the values of a map.
	 */
	public static <K, V> Filter<Map<K, V>> mapValues(Filter<? super Collection<V>> filter) {
		return Filters.nonNull(map -> filter.filter(map.values()));
	}
	
	/**
	 * Filter that applies given filter to the entries of a map.
	 */
	public static <K, V> Filter<Map<K, V>> mapEntries(Filter<? super Set<Map.Entry<K, V>>> filter) {
		return Filters.nonNull(map -> filter.filter(map.entrySet()));
	}
	
	/**
	 * Filter that returns true if the collection is not empty.
	 */
	public static <T> Filter<Collection<T>> notEmpty() {
		return Filters.nonNull(ts -> !ts.isEmpty());
	}

	/**
	 * Filter that applies given filter to the size of the collection.
	 */
	public static <T> Filter<Collection<T>> size(Filter<? super Integer> filter) {
		return Filters.nonNull(ts -> filter.filter(ts.size()));
	}

	/**
	 * Filter that applies given filter to item at index i of the list.
	 */
	public static <T> Filter<List<T>> atIndex(int i, Filter<? super T> filter) {
		return Filters.nonNull(ts -> {
			if (i >= ts.size()) return false;
			return filter.filter(ts.get(i));
		});
	}

	/**
	 * Filter that returns true if the given filter matches any elements in the collection.
	 */
	public static <T> Filter<Collection<T>> any(Filter<? super T> filter) {
		return Filters.nonNull(ts -> {
			for (T t : ts)
				if (filter.filter(t)) return true;
			return false;
		});
	}

	/**
	 * Filter that only returns true if the given filter matches all elements in the collection.
	 */
	public static <T> Filter<Collection<T>> all(Filter<? super T> filter) {
		return Filters.nonNull(ts -> {
			for (T t : ts)
				if (!filter.filter(t)) return false;
			return true;
		});
	}

}
