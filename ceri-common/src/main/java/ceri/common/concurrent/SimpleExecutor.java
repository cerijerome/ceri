package ceri.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import ceri.common.function.ExceptionRunnable;

/**
 * A single-threaded execution of callable or runnable.
 */
public class SimpleExecutor<E extends Exception, T> implements AutoCloseable {
	private static final int TIMEOUT_MS_DEF = 3000;
	private final ExecutorService exec;
	private final Future<T> future;
	private final Function<Throwable, ? extends E> exceptionConstructor;
	private final int closeTimeoutMs;

	public static SimpleExecutor<RuntimeException, ?> run(ExceptionRunnable<?> runnable) {
		return run(runnable, RuntimeException::new);
	}

	public static <E extends Exception> SimpleExecutor<E, ?> run(ExceptionRunnable<?> runnable,
		Function<Throwable, ? extends E> exceptionConstructor) {
		return run(runnable, exceptionConstructor, TIMEOUT_MS_DEF);
	}

	public static <E extends Exception> SimpleExecutor<E, ?> run(ExceptionRunnable<?> runnable,
		Function<Throwable, ? extends E> exceptionConstructor, int closeTimeoutMs) {
		return run(runnable, null, exceptionConstructor, closeTimeoutMs);
	}

	public static <T> SimpleExecutor<RuntimeException, T> run(ExceptionRunnable<?> runnable,
		T value) {
		return run(runnable, value, RuntimeException::new);
	}

	public static <E extends Exception, T> SimpleExecutor<E, T> run(ExceptionRunnable<?> runnable,
		T value, Function<Throwable, ? extends E> exceptionConstructor) {
		return run(runnable, value, exceptionConstructor, TIMEOUT_MS_DEF);
	}

	public static <E extends Exception, T> SimpleExecutor<E, T> run(ExceptionRunnable<?> runnable,
		T value, Function<Throwable, ? extends E> exceptionConstructor, int closeTimeoutMs) {
		return call(() -> {
			runnable.run();
			return value;
		}, exceptionConstructor, closeTimeoutMs);
	}

	public static <T> SimpleExecutor<RuntimeException, T> call(Callable<T> callable) {
		return call(callable, RuntimeException::new);
	}

	public static <E extends Exception, T> SimpleExecutor<E, T> call(Callable<T> callable,
		Function<Throwable, ? extends E> exceptionConstructor) {
		return call(callable, exceptionConstructor, TIMEOUT_MS_DEF);
	}

	public static <E extends Exception, T> SimpleExecutor<E, T> call(Callable<T> callable,
		Function<Throwable, ? extends E> exceptionConstructor, int closeTimeoutMs) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		try {
			Future<T> future = exec.submit(callable);
			return new SimpleExecutor<>(exec, future, exceptionConstructor, closeTimeoutMs);
		} catch (RuntimeException e) {
			ConcurrentUtil.close(exec, closeTimeoutMs);
			throw e;
		}
	}

	private SimpleExecutor(ExecutorService exec, Future<T> future,
		Function<Throwable, ? extends E> exceptionConstructor, int closeTimeoutMs) {
		this.exec = exec;
		this.future = future;
		this.exceptionConstructor = exceptionConstructor;
		this.closeTimeoutMs = closeTimeoutMs;
	}

	public T get() throws E {
		return ConcurrentUtil.get(future, exceptionConstructor);
	}

	public T get(int timeoutMs) throws E {
		return ConcurrentUtil.get(future, exceptionConstructor, timeoutMs);
	}

	public boolean cancel() {
		return future.cancel(true);
	}

	@Override
	public void close() {
		ConcurrentUtil.close(exec, closeTimeoutMs);
	}
}
