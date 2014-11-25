package ceri.common.concurrent;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionSupplier<E extends Exception, T> {
	T get() throws E;
}