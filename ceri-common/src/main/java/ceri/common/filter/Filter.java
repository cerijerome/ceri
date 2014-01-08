package ceri.common.filter;

/**
 * Filter interface - returns true if the conditions are met.
 */
public interface Filter<T> {

	boolean filter(T t);
	
}
