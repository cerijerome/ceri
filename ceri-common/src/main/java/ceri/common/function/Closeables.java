package ceri.common.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Utilities for AutoCloseable resources.
 */
public class Closeables {
	public static Functions.Closeable NULL = () -> {};
	private static final int TIMEOUT_MS_DEF = 1000;

	private Closeables() {}

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
		Excepts.Consumer<E, T> consumer) throws E {
		return acceptOrClose(t, consumer, Closeables::close);
	}

	/**
	 * Invokes the consumer on the given resource if non-null. If an exception occurs, the resource
	 * is closed, and the exception re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T> T acceptOrClose(T t, Excepts.Consumer<E, T> consumer,
		Excepts.Consumer<E, T> closeFn) throws E {
		try {
			if (t != null) consumer.accept(t);
			return t;
		} catch (Exception e) {
			closeFn.accept(t);
			throw e;
		}
	}

	/**
	 * Invokes the function on the given closable resource if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or null if the
	 * resource is null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrClose(T t,
		Excepts.Function<E, T, R> function) throws E {
		return applyOrClose(t, function, null);
	}

	/**
	 * Invokes the function on the given closable resource, if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or the given
	 * default value if the resource or function result is null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrClose(T t,
		Excepts.Function<E, T, R> function, R def) throws E {
		return applyOrClose(t, function, def, Closeables::close);
	}

	/**
	 * Invokes the function on the given closable resource, if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or the given
	 * default value if the resource or function result is null.
	 */
	public static <E extends Exception, T, R> R applyOrClose(T t,
		Excepts.Function<E, T, R> function, R def, Excepts.Consumer<E, T> closeFn) throws E {
		try {
			if (t == null) return def;
			var result = function.apply(t);
			return result == null ? def : result;
		} catch (Exception e) {
			closeFn.accept(t);
			throw e;
		}
	}

	/**
	 * Invokes the runnable. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T extends AutoCloseable> T runOrClose(T t,
		Excepts.Runnable<E> runnable) throws E {
		return runOrClose(t, runnable, Closeables::close);
	}

	/**
	 * Invokes the runnable. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T> T runOrClose(T t, Excepts.Runnable<E> runnable,
		Excepts.Consumer<E, T> closeFn) throws E {
		try {
			runnable.run();
			return t;
		} catch (Exception e) {
			closeFn.accept(t);
			throw e;
		}
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the function result.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R getOrClose(T t,
		Excepts.Supplier<E, R> supplier) throws E {
		return getOrClose(t, supplier, null);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R getOrClose(T t,
		Excepts.Supplier<E, R> supplier, R def) throws E {
		return getOrClose(t, supplier, def, Closeables::close);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T, R> R getOrClose(T t, Excepts.Supplier<E, R> supplier,
		R def, Excepts.Consumer<E, T> closeFn) throws E {
		try {
			var result = supplier.get();
			return result == null ? def : result;
		} catch (Exception e) {
			closeFn.accept(t);
			throw e;
		}
	}

	/**
	 * Invokes the consumer on the given resources. If an exception occurs, the resource is closed,
	 * and the exception re-thrown. Returns the passed-in resources.
	 */
	public static <E extends Exception, T extends AutoCloseable> Collection<T>
		acceptOrCloseAll(Collection<T> ts, Excepts.Consumer<E, Collection<T>> consumer) throws E {
		return acceptOrClose(ts, consumer, Closeables::close);
	}

	/**
	 * Invokes the function on the given resources. If an exception occurs, the resources are
	 * closed, and the exception re-thrown. Returns the function result.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		applyOrCloseAll(Collection<T> ts, Excepts.Function<E, Collection<T>, R> function) throws E {
		return applyOrCloseAll(ts, function, null);
	}

	/**
	 * Invokes the function on the given resources. If an exception occurs, the resources are
	 * closed, and the exception re-thrown. Returns the function result, or the given default value
	 * if the resources or function result are null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrCloseAll(
		Collection<T> ts, Excepts.Function<E, Collection<T>, R> function, R def) throws E {
		return applyOrClose(ts, function, def, Closeables::close);
	}

	/**
	 * Invokes the runnable. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the passed-in resources.
	 */
	public static <E extends Exception, T extends AutoCloseable> Collection<T>
		runOrCloseAll(Collection<T> ts, Excepts.Runnable<E> runnable) throws E {
		return runOrClose(ts, runnable, Closeables::close);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the function result.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		getOrCloseAll(Collection<T> ts, Excepts.Supplier<E, R> supplier) throws E {
		return getOrCloseAll(ts, supplier, null);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		getOrCloseAll(Collection<T> ts, Excepts.Supplier<E, R> supplier, R def) throws E {
		return getOrClose(ts, supplier, def, Closeables::close);
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
	public static <T> boolean close(T t, Excepts.Consumer<?, ? super T> closeFn) {
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
		return testIt(process, _ -> {
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
		return testIt(executor, _ -> {
			executor.shutdownNow();
			return Concurrent.getWhileInterrupted(executor::awaitTermination, timeoutMs,
				TimeUnit.MILLISECONDS);
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
		create(Excepts.Function<E, T, R> constructor, T... inputs) throws E {
		return createFrom(constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		createFrom(Excepts.Function<E, T, R> constructor, Iterable<T> inputs) throws E {
		var results = Lists.<R>of();
		try {
			for (T input : inputs)
				results.add(constructor.apply(input));
			return Immutable.wrap(results);
		} catch (Exception e) {
			close(results);
			throw e;
		}
	}

	/**
	 * Constructs an immutable list of closeable instances by calling the constructor the given
	 * number of times. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T extends AutoCloseable> List<T>
		create(Excepts.Supplier<E, T> constructor, int count) throws E {
		var results = new ArrayList<T>(count);
		try {
			for (int i = 0; i < count; i++)
				results.add(constructor.get());
			return Immutable.wrap(results);
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
		IntFunction<R[]> arrayFn, Excepts.Function<E, T, R> constructor, T... inputs) throws E {
		return createArray(arrayFn, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, Excepts.Function<E, T, R> constructor, Collection<T> inputs)
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
	 * Constructs an array of closeable instances by calling the constructor the given number of
	 * times. If any exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T extends AutoCloseable> T[] createArray(
		IntFunction<T[]> arrayFn, Excepts.Supplier<E, T> constructor, int count) throws E {
		T[] results = arrayFn.apply(count);
		try {
			for (int i = 0; i < count; i++)
				results[i] = constructor.get();
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
	private static <T> boolean consumeIt(T t, Excepts.Consumer<Exception, T> consumer) {
		if (t == null) return true;
		return runIt(() -> consumer.accept(t));
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be suppressed, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static boolean runIt(Excepts.Runnable<Exception> runnable) {
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
	private static <T> boolean testIt(T t, Excepts.Predicate<Exception, T> predicate) {
		if (t == null) return true;
		return getIt(() -> predicate.test(t), false);
	}

	/**
	 * Invokes the supplier and returns the result. If an exception is thrown, it is suppressed, and
	 * error value returned instead. Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> T getIt(Excepts.Supplier<Exception, T> supplier, T errorVal) {
		try {
			return supplier.get();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			Concurrent.interrupt(); // reset interrupt since we ignore the exception
		} catch (Exception e) {
			// ignored
		}
		return errorVal;
	}
}
