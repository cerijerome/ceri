package ceri.common.concurrent;

public class ConcurrentUtil {

	private ConcurrentUtil() {}

	/**
	 * Checks if the current thread has been interrupted and throws an
	 * InterruptedException.
	 */
	public static void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException("Thread has been interrupted");
	}

	/**
	 * Checks if the current thread has been interrupted and throws a
	 * RuntimeInterruptedException instead.
	 */
	public static void checkRuntimeInterrupted() throws RuntimeInterruptedException {
		if (Thread.interrupted()) throw new RuntimeInterruptedException(
			"Thread has been interrupted");
	}

}
