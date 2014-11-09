package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class ConcurrentUtil {

	private ConcurrentUtil() {}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <T> T execute(Lock lock, Supplier<T> supplier) {
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Executes the operation within the lock.
	 */
	public static void execute(Lock lock, Runnable runnable) {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}
	
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
