package ceri.common.concurrent;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionConsumer<E extends Exception, T> {
	void accept(T t) throws E;
}