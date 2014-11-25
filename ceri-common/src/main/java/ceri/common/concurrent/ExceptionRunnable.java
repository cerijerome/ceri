package ceri.common.concurrent;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	void run() throws E;
}