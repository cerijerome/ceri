package ceri.common.concurrent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import ceri.common.collection.CollectionUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;

public class ConcurrentUtil {

	private ConcurrentUtil() {}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, ? extends E> exceptionConstructor)
		throws E {
		get(submit(executor, runnable), exceptionConstructor);
	}

	public static <E extends Exception> void executeAndWait(ExecutorService executor,
		ExceptionRunnable<?> runnable, Function<Throwable, ? extends E> exceptionConstructor,
		int timeoutMs) throws E {
		get(submit(executor, runnable), exceptionConstructor, timeoutMs);
	}

	/**
	 * Calls future get with support for converting exceptions.
	 */
	public static <T, E extends Exception> T get(Future<T> future,
		Function<Throwable, ? extends E> exceptionConstructor) throws E {
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
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs) throws E {
		try {
			return future.get(timeoutMs, MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (TimeoutException e) {
			throw exceptionConstructor.apply(e);
		} catch (ExecutionException e) {
			throw exceptionConstructor.apply(e.getCause());
		}
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, E> exceptionConstructor, ExceptionRunnable<E>... runnables)
		throws InterruptedException, E {
		invoke(executor, exceptionConstructor, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, Collection<ExceptionRunnable<E>> runnables)
		throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, null, runnables);
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	@SafeVarargs
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		ExceptionRunnable<E>... runnables) throws InterruptedException, E {
		invoke(executor, exceptionConstructor, timeoutMs, Arrays.asList(runnables));
	}

	/**
	 * Executes all runnable tasks and waits for completion. If the timeout expires, tasks are
	 * cancelled and a CancellationException is thrown.
	 */
	public static <E extends Exception> void invoke(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, int timeoutMs,
		Collection<ExceptionRunnable<E>> runnables) throws InterruptedException, E {
		invokeAll(executor, exceptionConstructor, timeoutMs, runnables);
	}

	/**
	 * Executes all runnable tasks and waits for completion. A null timeout will wait indefinitely.
	 * If the timeout expires, tasks are cancelled and a CancellationException is thrown.
	 */
	private static <E extends Exception> void invokeAll(ExecutorService executor,
		Function<Throwable, ? extends E> exceptionConstructor, Integer timeoutMs,
		Collection<ExceptionRunnable<E>> runnables) throws InterruptedException, E {
		List<Callable<Boolean>> callables =
			CollectionUtil.toList(ConcurrentUtil::callable, runnables);
		List<Future<Boolean>> futures = timeoutMs == null ? executor.invokeAll(callables) :
			executor.invokeAll(callables, timeoutMs, MILLISECONDS);
		for (Future<Boolean> future : futures)
			get(future, exceptionConstructor);
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
	public static Callable<Boolean> callable(ExceptionRunnable<?> runnable) {
		return () -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public static <E extends Exception, T> T executeGet(Lock lock,
		ExceptionSupplier<E, T> supplier)
		throws E {
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
		if (Thread.interrupted())
			throw new RuntimeInterruptedException("Thread has been interrupted");
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
	public static <T> T executeGetInterruptible(
		ExceptionSupplier<InterruptedException, T> supplier) {
		try {
			return supplier.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

}
