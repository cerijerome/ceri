package ceri.common.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Futures {

	private Futures() {}

	/**
	 * Returns a completed future with fixed result value.
	 */
	public static <T> RunnableFuture<T> done(T result) {
		return fixed(false, result, null);
	}

	/**
	 * Returns a cancelled future.
	 */
	public static <T> RunnableFuture<T> cancelled() {
		return fixed(true, null, null);
	}

	/**
	 * Returns a completed future that throws an exception.
	 */
	public static <T> RunnableFuture<T> error(Exception e) {
		return fixed(false, null, e);
	}

	private static <T> RunnableFuture<T> fixed(boolean cancelled, T result, Exception e) {
		return new RunnableFuture<>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return cancelled;
			}

			@Override
			public T get() throws InterruptedException, ExecutionException {
				if (e != null) throw new ExecutionException(e);
				return result;
			}

			@Override
			public T get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
				return get();
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public void run() {}
		};
	}

}
