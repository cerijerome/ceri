package ceri.common.function;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionFunction<E extends Exception, T, R> {
	R apply(T t) throws E;
}
