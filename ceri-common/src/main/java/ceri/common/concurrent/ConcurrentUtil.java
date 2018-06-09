package ceri.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;

public class ConcurrentUtil {

	private ConcurrentUtil() {}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, E> exceptionConstructor) throws E {
		get(submit(executor, runnable), exceptionConstructor);
	}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, E> exceptionConstructor, int timeoutMs)
			throws E {
		get(submit(executor, runnable), exceptionConstructor, timeoutMs);
	}

	/**
	 * Calls future get with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Function<Throwable, E> exceptionConstructor) throws E {
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			throw exceptionConstructor.apply(e.getCause());
		}
	}

	/**
	 * Calls future get with millisecond time limit, with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Function<Throwable, E> exceptionConstructor, int timeoutMs) throws E {
		try {
			return future.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (TimeoutException e) {
			throw exceptionConstructor.apply(e);
		} catch (ExecutionException e) {
			throw exceptionConstructor.apply(e.getCause());
		}
	}

	/**
	 * Submits a runnable to the executor service, allowing exceptions to be thrown. Returns a
	 * boolean Future.
	 */
	public static Future<?> submit(ExecutorService executor, ExceptionRunnable<?> runnable) {
		return executor.submit(callable(runnable));
	}

	/**
	 * Converts a runnable with exception into a callable type.
	 */
	public static Callable<?> callable(ExceptionRunnable<?> runnable) {
		return () -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception, T> T
	executeGet(Lock lock, ExceptionSupplier<E, T> supplier) throws E {
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
	public static <E extends Exception> void execute(Lock lock, ExceptionRunnable<E> runnable)
		throws E {
		lock.lock();
		try {
			runnable.run();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Checks if the current thread has been interrupted and throws an InterruptedException.
	 */
	public static void checkInterrupted() throws InterruptedException {
		if (Thread.interrupted()) throw new InterruptedException("Thread has been interrupted");
	}

	/**
	 * Checks if the current thread has been interrupted and throws a RuntimeInterruptedException
	 * instead.
	 */
	public static void checkRuntimeInterrupted() throws RuntimeInterruptedException {
		if (Thread.interrupted()) throw new RuntimeInterruptedException(
			"Thread has been interrupted");
	}

	/**
	 * Executes and converts InterruptedException to runtime.
	 */
	public static void executeInterruptible(ExceptionRunnable<InterruptedException> runnable) {
		try {
			runnable.run();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}
	
	/**
	 * Executes and converts InterruptedException to runtime.
	 */
	public static <T> T executeGetInterruptible(ExceptionSupplier<InterruptedException, T> supplier) {
		try {
			return supplier.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}
	
}
