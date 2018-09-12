package ceri.common.function;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionBiConsumer<E extends Exception, T, U> {
	void accept(T t, U u) throws E;
}