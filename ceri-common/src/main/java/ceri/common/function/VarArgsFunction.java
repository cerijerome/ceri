package ceri.common.function;

/**
 * Function that accepts varargs and returns a result. 
 */
public interface VarArgsFunction<T, R> {
	R apply(@SuppressWarnings("unchecked") T... ts);
}
