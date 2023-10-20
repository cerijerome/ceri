package ceri.common.function;

/**
 * Function that accepts varargs and returns a result. Care should be taken to avoid heap pollution.
 */
public interface VarArgsFunction<T, R> {
	R apply(@SuppressWarnings("unchecked") T... ts);
}
