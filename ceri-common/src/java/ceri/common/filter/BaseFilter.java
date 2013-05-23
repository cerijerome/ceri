package ceri.common.filter;

/**
 * Base filter that returns false for null values.
 * Implementing classes need only work with non-null values.
 */
public abstract class BaseFilter<T> implements Filter<T> {

	@Override
	public boolean filter(T t) {
		return t != null && filterNonNull(t);
	}
	
	public abstract boolean filterNonNull(T t);
	
}
