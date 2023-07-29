package ceri.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionPredicate;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;

/**
 * Utilities for AutoCloseable resources.
 */
public class CloseableUtil {
	private static final int TIMEOUT_MS_DEF = 1000;

	private CloseableUtil() {}

	/**
	 * Reference to a closeable resource; use as a method return to avoid resource leak warnings.
	 */
	public static class Ref<T extends AutoCloseable> {
		public final T ref;

		private Ref(T ref) {
			this.ref = ref;
		}
	}

	/**
	 * Return a closeable reference, to avoid resource leak warnings.
	 */
	public static <T extends AutoCloseable> Ref<T> ref(T closeable) {
		return new Ref<>(closeable);
	}

	/**
	 * Invokes the consumer on the given resource if non-null. If an exception occurs, the resource
	 * is closed, and the exception re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T extends AutoCloseable> T acceptOrClose(T t,
		ExceptionConsumer<E, T> consumer) throws E {
		try {
			if (t != null) consumer.accept(t);
			return t;
		} catch (Exception e) {
			close(t);
			throw e;
		}
	}

	/**
	 * Invokes the function on the given closable resource if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or null if the
	 * resource is null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrClose(T t,
		ExceptionFunction<E, T, R> function) throws E {
		return applyOrClose(t, function, null);
	}

	/**
	 * Invokes the function on the given closable resource, if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or the given
	 * default value if the resource or function result is null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrClose(T t,
		ExceptionFunction<E, T, R> function, R def) throws E {
		try {
			if (t == null) return def;
			var result = function.apply(t);
			return result == null ? def : result;
		} catch (Exception e) {
			close(t);
			throw e;
		}
	}

	/**
	 * Closes resources in order. Exceptions are suppressed, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	public static boolean close(AutoCloseable... closeables) {
		return close(Arrays.asList(closeables));
	}

	/**
	 * Closes resources in order. Exceptions are suppressed, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	public static boolean close(Iterable<? extends AutoCloseable> closeables) {
		boolean success = true;
		if (closeables != null) for (var closeable : closeables)
			if (!close(closeable, AutoCloseable::close)) success = false;
		return success;
	}

	/**
	 * Closes a subject if non-null, applying the consumer, and returns true. Returns true if the
	 * subject is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false if any exception occurred.
	 */
	public static <T> boolean close(T t, ExceptionConsumer<?, T> closeFn) {
		return consumeIt(t, closeFn::accept);
	}

	/**
	 * Kills the process if non-null, and waits for the completion result. Returns true if the
	 * process is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(Process process) {
		return close(process, TIMEOUT_MS_DEF);
	}

	/**
	 * Kills the process if non-null, and waits for the completion result. Returns true if the
	 * process is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(Process process, int timeoutMs) {
		return testIt(process, p -> {
			process.destroy();
			return process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
		});
	}

	/**
	 * Shuts down the executor if non-null, and waits for the completion result. Returns true if the
	 * executor is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(ExecutorService executor) {
		return close(executor, TIMEOUT_MS_DEF);
	}

	/**
	 * Shuts down the executor if non-null, and waits for the completion result. Returns true if the
	 * executor is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(ExecutorService executor, int timeoutMs) {
		return testIt(executor, e -> {
			executor.shutdownNow();
			return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
		});
	}

	/**
	 * Cancels the future if non-null, and waits for completion, returning true. Returns true if the
	 * future is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any non-cancellation exception occurred.
	 */
	public static boolean close(Future<?> future) {
		return close(future, TIMEOUT_MS_DEF);
	}

	/**
	 * Cancels the future if non-null, and waits for completion, returning true. Returns true if the
	 * future is null. Exceptions are suppressed, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any non-cancellation exception occurred.
	 */
	public static boolean close(Future<?> future, int timeoutMs) {
		return close(future, f -> {
			try {
				f.cancel(true);
				f.get(timeoutMs, TimeUnit.MILLISECONDS);
			} catch (CancellationException e) {
				// expected
			}
		});
	}

	/**
	 * Closes resources in reverse order. Exceptions are suppressed, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	public static boolean closeReversed(AutoCloseable... closeables) {
		return closeReversed(Arrays.asList(closeables));
	}

	/**
	 * Closes resources in reverse order. Exceptions are suppressed, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	@SuppressWarnings("resource")
	public static boolean closeReversed(List<? extends AutoCloseable> closeables) {
		boolean success = true;
		for (int i = closeables.size() - 1; i >= 0; i--)
			if (!close(closeables.get(i), AutoCloseable::close)) success = false;
		return success;
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		create(ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return create(constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		create(ExceptionFunction<E, T, R> constructor, Iterable<T> inputs) throws E {
		List<R> results = new ArrayList<>();
		try {
			for (T input : inputs)
				results.add(constructor.apply(input));
			return Collections.unmodifiableList(results);
		} catch (Exception e) {
			close(results);
			throw e;
		}
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return createArray(arrayFn, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, Collection<T> inputs)
		throws E {
		R[] results = arrayFn.apply(inputs.size());
		int i = 0;
		try {
			for (T input : inputs)
				results[i++] = constructor.apply(input);
			return results;
		} catch (Exception e) {
			close(results);
			throw e;
		}
	}

	/**
	 * Invokes the consumer on the object if non-null, and returns true. If the object is null, true
	 * is returned. If an exception is thrown, it will be suppressed, and false is returned.
	 * Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> boolean consumeIt(T t, ExceptionConsumer<Exception, T> consumer) {
		if (t == null) return true;
		return runIt(() -> consumer.accept(t));
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be suppressed, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static boolean runIt(ExceptionRunnable<Exception> runnable) {
		return getIt(() -> {
			runnable.run();
			return true;
		}, false);
	}

	/**
	 * Invokes the predicate on the object if non-null, and returns the result. If the object is
	 * null, true is returned. If an exception is thrown, it is suppressed, and false is returned.
	 * Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> boolean testIt(T t, ExceptionPredicate<Exception, T> predicate) {
		if (t == null) return true;
		return getIt(() -> predicate.test(t), false);
	}

	/**
	 * Invokes the supplier and returns the result. If an exception is thrown, it is suppressed, and
	 * error value returned instead. Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> T getIt(ExceptionSupplier<Exception, T> supplier, T errorVal) {
		try {
			return supplier.get();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			ConcurrentUtil.interrupt(); // reset interrupt since we ignore the exception
		} catch (Exception e) {
			// ignored
		}
		return errorVal;
	}
}
