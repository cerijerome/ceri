package ceri.common.function;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionBiFunction<E extends Exception, T, U, R> {
	R apply(T t, U u) throws E;
}