package ceri.log.util;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.Immutable;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteUtil;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.Excepts.Consumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.Predicate;
import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;
import ceri.common.reflect.Reflect;
import ceri.common.test.BinaryPrinter;
import ceri.common.text.StringUtil;
import ceri.common.util.Align;
import ceri.common.util.StartupValues;
import ceri.log.io.LogPrintStream;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	private static final Logger logger = LogManager.getLogger();
	private static final int TIMEOUT_MS_DEF = 10000;
	private static final int TITLE_MAX_WIDTH = 76;

	private LogUtil() {}

	/**
	 * For lazy string instantiation.
	 */
	private static class ToString {
		private final Supplier<?, String> stringSupplier;

		private ToString(Supplier<?, String> stringSupplier) {
			this.stringSupplier = stringSupplier;
		}

		@Override
		public String toString() {
			return ExceptionAdapter.runtime.get(stringSupplier);
		}
	}

	/**
	 * Returns the logger name for a class.
	 */
	public static String loggerName(Class<?> cls) {
		String logger = cls.getCanonicalName();
		if (logger == null) logger = cls.getName();
		return logger;
	}

	/**
	 * Provides StartupValues that logs whenever a value is read.
	 */
	public static StartupValues startupValues(String... args) {
		var caller = Reflect.previousCaller(1);
		return StartupValues.of(args).prefix(caller.pkg()).notifier(logger::info);
	}

	/**
	 * Provides StartupValues that logs whenever a value is read.
	 */
	public static StartupValues startupValues(Level level, String... args) {
		var caller = Reflect.previousCaller(1);
		return StartupValues.of(args).prefix(caller.pkg()).notifier(s -> logger.log(level, s));
	}

	/**
	 * A binary printer that writes output to the logger.
	 */
	public static BinaryPrinter binaryLogger() {
		return binaryLogger(BinaryPrinter.ASCII);
	}

	/**
	 * Copies a binary printer, that writes output to the logger.
	 */
	@SuppressWarnings("resource")
	public static BinaryPrinter binaryLogger(BinaryPrinter printer) {
		return binaryLogger(printer, LogPrintStream.of());
	}

	/**
	 * Copies a binary printer, that writes output to the logger.
	 */
	public static BinaryPrinter binaryLogger(BinaryPrinter printer, LogPrintStream stream) {
		return BinaryPrinter.builder(printer).out(stream).build();
	}

	/**
	 * Invokes the supplier and returns the supplied value, which may be null. If an exception
	 * occurs, it is logged, and null is returned. Interrupted exceptions will re-interrupt the
	 * current thread.
	 */
	public static <T> T getSilently(Supplier<?, T> supplier) {
		return getSilently(supplier, null);
	}

	/**
	 * Invokes the supplier and returns the supplied value, which may be null. If an exception
	 * occurs, it is logged, and the error value is returned. Interrupted exceptions will
	 * re-interrupt the current thread.
	 */
	public static <T> T getSilently(Supplier<?, T> supplier, T errorVal) {
		return getIt(supplier::get, errorVal);
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be logged, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	public static boolean runSilently(Runnable<?> runnable) {
		return runIt(runnable::run);
	}

	/**
	 * Invokes the consumer on the given resource if non-null. If an exception occurs, the resource
	 * is closed, and the exception re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T extends AutoCloseable> T acceptOrClose(T t,
		Consumer<E, T> consumer) throws E {
		return acceptOrClose(t, consumer, LogUtil::close);
	}

	/**
	 * Invokes the consumer on the given resource if non-null. If an exception occurs, the resource
	 * is closed, and the exception re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T> T acceptOrClose(T t, Consumer<E, T> consumer,
		Consumer<E, T> closeFn) throws E {
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
		Function<E, T, R> function) throws E {
		return applyOrClose(t, function, null);
	}

	/**
	 * Invokes the function on the given closable resource, if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or the given
	 * default value if the resource or function result is null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R applyOrClose(T t,
		Function<E, T, R> function, R def) throws E {
		return applyOrClose(t, function, def, LogUtil::close);
	}

	/**
	 * Invokes the function on the given closable resource, if non-null. If an exception occurs, the
	 * resource is closed, and the exception re-thrown. Returns the function result, or the given
	 * default value if the resource or function result is null.
	 */
	public static <E extends Exception, T, R> R applyOrClose(T t, Function<E, T, R> function, R def,
		Consumer<E, T> closeFn) throws E {
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
		Runnable<E> runnable) throws E {
		return runOrClose(t, runnable, LogUtil::close);
	}

	/**
	 * Invokes the runnable. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the passed-in resource.
	 */
	public static <E extends Exception, T> T runOrClose(T t, Runnable<E> runnable,
		Consumer<E, T> closeFn) throws E {
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
		Supplier<E, R> supplier) throws E {
		return getOrClose(t, supplier, null);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R getOrClose(T t,
		Supplier<E, R> supplier, R def) throws E {
		return getOrClose(t, supplier, def, LogUtil::close);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resource is closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T, R> R getOrClose(T t, Supplier<E, R> supplier, R def,
		Consumer<E, T> closeFn) throws E {
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
		acceptOrCloseAll(Collection<T> ts, Consumer<E, Collection<T>> consumer) throws E {
		return acceptOrClose(ts, consumer, LogUtil::close);
	}

	/**
	 * Invokes the function on the given resources. If an exception occurs, the resources are
	 * closed, and the exception re-thrown. Returns the function result.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		applyOrCloseAll(Collection<T> ts, Function<E, Collection<T>, R> function) throws E {
		return applyOrCloseAll(ts, function, null);
	}

	/**
	 * Invokes the function on the given resources. If an exception occurs, the resources are
	 * closed, and the exception re-thrown. Returns the function result, or the given default value
	 * if the resources or function result are null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		applyOrCloseAll(Collection<T> ts, Function<E, Collection<T>, R> function, R def) throws E {
		return applyOrClose(ts, function, def, LogUtil::close);
	}

	/**
	 * Invokes the runnable. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the passed-in resources.
	 */
	public static <E extends Exception, T extends AutoCloseable> Collection<T>
		runOrCloseAll(Collection<T> ts, Runnable<E> runnable) throws E {
		return runOrClose(ts, runnable, LogUtil::close);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the function result.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		getOrCloseAll(Collection<T> ts, Supplier<E, R> supplier) throws E {
		return getOrCloseAll(ts, supplier, null);
	}

	/**
	 * Invokes the supplier. If an exception occurs, the resources are closed, and the exception
	 * re-thrown. Returns the function result, or the given default value if the function result is
	 * null.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> R
		getOrCloseAll(Collection<T> ts, Supplier<E, R> supplier, R def) throws E {
		return getOrClose(ts, supplier, def, LogUtil::close);
	}

	/**
	 * Closes resources in order. Exceptions are logged, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	public static boolean close(AutoCloseable... closeables) {
		return close(Arrays.asList(closeables));
	}

	/**
	 * Closes resources in order. Exceptions are logged, and interrupted exceptions will
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
	 * subject is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
	 * thread. Returns false if any exception occurred.
	 */
	public static <T> boolean close(T t, Consumer<?, T> closeFn) {
		return consumeIt(t, closeFn::accept);
	}

	/**
	 * Kills the process if non-null, and waits for the completion result. Returns true if the
	 * process is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(Process process) {
		return close(process, TIMEOUT_MS_DEF);
	}

	/**
	 * Kills the process if non-null, and waits for the completion result. Returns true if the
	 * process is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
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
	 * executor is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(ExecutorService executor) {
		return close(executor, TIMEOUT_MS_DEF);
	}

	/**
	 * Shuts down the executor if non-null, and waits for the completion result. Returns true if the
	 * executor is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any exception occurred.
	 */
	public static boolean close(ExecutorService executor, int timeoutMs) {
		return testIt(executor, _ -> {
			executor.shutdownNow();
			return ConcurrentUtil.getWhileInterrupted(executor::awaitTermination, timeoutMs,
				MILLISECONDS);
		});
	}

	/**
	 * Cancels the future if non-null, and waits for completion, returning true. Returns true if the
	 * future is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
	 * thread. Returns false on timeout, or if any non-cancellation exception occurred.
	 */
	public static boolean close(Future<?> future) {
		return close(future, TIMEOUT_MS_DEF);
	}

	/**
	 * Cancels the future if non-null, and waits for completion, returning true. Returns true if the
	 * future is null. Exceptions are logged, and interrupted exceptions will re-interrupt the
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
	 * Closes resources in reverse order. Exceptions are logged, and interrupted exceptions will
	 * re-interrupt the thread. Returns false if any exceptions occurred.
	 */
	public static boolean closeReversed(AutoCloseable... closeables) {
		return closeReversed(Arrays.asList(closeables));
	}

	/**
	 * Closes resources in reverse order. Exceptions are logged, and interrupted exceptions will
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
		create(Function<E, T, R> constructor, T... inputs) throws E {
		return create(constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T, R extends AutoCloseable> List<R>
		create(Function<E, T, R> constructor, Iterable<T> inputs) throws E {
		List<R> results = new ArrayList<>();
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
		create(Supplier<E, T> constructor, int count) throws E {
		List<T> results = new ArrayList<>(count);
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
	public static <E extends Exception, T, R extends AutoCloseable> R[]
		createArray(IntFunction<R[]> arrayFn, Function<E, T, R> constructor, T... inputs) throws E {
		return createArray(arrayFn, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(
		IntFunction<R[]> arrayFn, Function<E, T, R> constructor, Collection<T> inputs) throws E {
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
	public static <E extends Exception, T extends AutoCloseable> T[]
		createArray(IntFunction<T[]> arrayFn, Supplier<E, T> constructor, int count) throws E {
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
	 * Returns an object whose toString() executes the supplier method. Used for logging lazy string
	 * instantiations.
	 */
	public static Object toString(Supplier<?, String> stringSupplier) {
		return new ToString(stringSupplier);
	}

	/**
	 * Returns an object whose toString() executes the conversion method on the given object. Used
	 * for logging lazy string instantiations.
	 */
	public static <T> Object toString(T t, Function<?, T, String> converter) {
		return toString(() -> converter.apply(t));
	}

	/**
	 * Returns an object whose toString() returns the hex hash code.
	 */
	public static Object hashId(Object obj) {
		return toString(() -> Reflect.hashId(obj));
	}

	/**
	 * Returns an object whose toString() returns the hex string of the given integer value.
	 */
	public static Object toHex(int value) {
		return toString(() -> Integer.toHexString(value));
	}

	/**
	 * Returns an object whose toString() returns the formatted string.
	 */
	public static Object toFormat(String format, Object... args) {
		return toString(() -> StringUtil.format(format, args));
	}

	/**
	 * Returns an object with a compact toString(), replacing multiple whitespace with a single
	 * space.
	 */
	public static Object compact(Object obj) {
		return toString(() -> StringUtil.compact(String.valueOf(obj)));
	}

	/**
	 * Returns an object with an escaped toString(), replacing unprintable chars with literals.
	 */
	public static Object escaped(Object obj) {
		return toString(() -> StringUtil.escape(String.valueOf(obj)));
	}

	/**
	 * Returns an object with an escaped toString(), replacing unprintable chars with literals.
	 */
	public static Object escapedAscii(byte[] bytes, int offset, int length) {
		return toString(() -> StringUtil.escape(ByteUtil.fromAscii(bytes, offset, length)));
	}

	/**
	 * Intro logging message.
	 */
	public static String introMessage(String title) {
		if (title.length() > TITLE_MAX_WIDTH) title = title.substring(0, TITLE_MAX_WIDTH);
		return "\n"
			+ "================================================================================\n"
			+ "|                                                                              |\n"
			+ "| " + StringUtil.pad(title, TITLE_MAX_WIDTH, Align.H.center) + " |\n"
			+ "|                                                                              |\n"
			+ "================================================================================";
	}

	/**
	 * Invokes the consumer on the object if non-null, and returns true. If the object is null, true
	 * is returned. If an exception is thrown, it will be logged, and false is returned. Interrupted
	 * exceptions will re-interrupt the thread.
	 */
	private static <T> boolean consumeIt(T t, Consumer<Exception, T> consumer) {
		if (t == null) return true;
		return runIt(() -> consumer.accept(t));
	}

	/**
	 * Invokes the runnable and returns true. If an exception is thrown, it will be logged, and
	 * false is returned. Interrupted exceptions will re-interrupt the thread.
	 */
	private static boolean runIt(Runnable<Exception> runnable) {
		return getIt(() -> {
			runnable.run();
			return true;
		}, false);
	}

	/**
	 * Invokes the predicate on the object if non-null, and returns the result. If the object is
	 * null, true is returned. If an exception is thrown, it will be logged, and false is returned.
	 * Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> boolean testIt(T t, Predicate<Exception, T> predicate) {
		if (t == null) return true;
		return getIt(() -> predicate.test(t), false);
	}

	/**
	 * Invokes the supplier and returns the result. If an exception is thrown, it will be logged,
	 * and error value returned instead. Interrupted exceptions will re-interrupt the thread.
	 */
	private static <T> T getIt(Supplier<Exception, T> supplier, T errorVal) {
		try {
			return supplier.get();
		} catch (RuntimeInterruptedException | InterruptedException e) {
			logger.debug(e);
			ConcurrentUtil.interrupt(); // reset interrupt since we ignore the exception
		} catch (Exception e) {
			logger.catching(Level.WARN, e);
		}
		return errorVal;
	}
}
