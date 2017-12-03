package ceri.common.function;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	void run() throws E;
}