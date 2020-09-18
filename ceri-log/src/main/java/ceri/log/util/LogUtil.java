package ceri.log.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.IntFunction;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.reflect.ReflectUtil;
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
	private static final int TIMEOUT_MS_DEF = 1000;
	private static final int TITLE_MAX_WIDTH = 76;

	private LogUtil() {}

	/**
	 * For lazy string instantiation.
	 */
	private static class ToString {
		private final ExceptionSupplier<?, String> stringSupplier;

		private ToString(ExceptionSupplier<?, String> stringSupplier) {
			this.stringSupplier = stringSupplier;
		}

		@Override
		public String toString() {
			return ExceptionAdapter.RUNTIME.get(stringSupplier);
		}
	}

	public static String loggerName(Class<?> cls) {
		String logger = cls.getCanonicalName();
		if (logger == null) logger = cls.getName();
		return logger;
	}

	public static StartupValues startupValues(String... args) {
		return startupValues(null, args);
	}

	public static StartupValues startupValues(Logger logger, String... args) {
		return StartupValues.of(args).notifier(logger(logger)::info);
	}

	public static BinaryPrinter binaryLogger() {
		return binaryLogger(BinaryPrinter.ASCII);
	}

	@SuppressWarnings("resource")
	public static BinaryPrinter binaryLogger(BinaryPrinter printer) {
		return binaryLogger(printer, LogPrintStream.of());
	}

	public static BinaryPrinter binaryLogger(BinaryPrinter printer, LogPrintStream stream) {
		return BinaryPrinter.builder(printer).out(stream).build();
	}

	/**
	 * Execute runnable and log any exception as an error. Returns true if the runnable executed
	 * successfully.
	 */
	public static boolean execute(Logger logger, ExceptionRunnable<Exception> runnable) {
		try {
			if (runnable == null) return false;
			runnable.run();
			return true;
		} catch (InterruptedException | RuntimeInterruptedException e) {
			if (logger != null) logger.info(e);
		} catch (Exception e) {
			if (logger != null) logger.catching(e);
		}
		return false;
	}

	/**
	 * Submit runnable to the executor service. Log any exception as an error. Future result is true
	 * if the runnable executed successfully.
	 */
	public static Future<Boolean> submit(Logger logger, ExecutorService executor,
		ExceptionRunnable<Exception> runnable) {
		return executor.submit(() -> execute(logger, runnable));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> List<R> create(Logger logger,
		ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return create(logger, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an immutable list of closeable instances from each input object and the
	 * constructor. If any exception occurs the already created instances will be closed.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception, T, R extends AutoCloseable> List<R> create(Logger logger,
		ExceptionFunction<E, T, R> constructor, Collection<T> inputs) throws E {
		List<R> results = new ArrayList<>();
		try {
			for (T input : inputs)
				results.add(constructor.apply(input));
			return Collections.unmodifiableList(results);
		} catch (Exception e) {
			close(logger, results);
			throw e;
		}
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(Logger logger,
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, T... inputs) throws E {
		return createArray(logger, arrayFn, constructor, Arrays.asList(inputs));
	}

	/**
	 * Constructs an array of closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <E extends Exception, T, R extends AutoCloseable> R[] createArray(Logger logger,
		IntFunction<R[]> arrayFn, ExceptionFunction<E, T, R> constructor, Collection<T> inputs)
		throws E {
		R[] results = arrayFn.apply(inputs.size());
		int i = 0;
		try {
			for (T input : inputs)
				results[i++] = constructor.apply(input);
			return results;
		} catch (Exception e) {
			close(logger, results);
			throw e;
		}
	}

	/**
	 * Convert multiple closeables to a single closeable.
	 */
	public static AutoCloseable closeable(Logger logger, AutoCloseable... closeables) {
		return () -> close(logger, closeables);
	}

	/**
	 * Convert multiple closeables to a single closeable.
	 */
	public static AutoCloseable closeable(Logger logger,
		Iterable<? extends AutoCloseable> closeables) {
		return () -> close(logger, closeables);
	}

	/**
	 * Shuts down a process, and waits for shutdown to complete, up to a default time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, Process process) {
		return close(logger, process, TIMEOUT_MS_DEF);
	}

	/**
	 * Shuts down a process, and waits for shutdown to complete, up to given time in milliseconds.
	 */
	public static boolean close(Logger logger, Process process, int timeoutMs) {
		if (process == null) return false;
		process.destroy();
		try {
			return process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | RuntimeInterruptedException e) {
			if (logger != null) logger.info(e);
		}
		return false;
	}

	/**
	 * Shuts down an executor service, and waits for shutdown to complete, up to a default time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, ExecutorService executor) {
		return close(logger, executor, TIMEOUT_MS_DEF);
	}

	/**
	 * Shuts down an executor service, and waits for shutdown to complete, up to given time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, ExecutorService executor, int timeoutMs) {
		if (executor == null) return false;
		executor.shutdownNow();
		try {
			return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | RuntimeInterruptedException e) {
			if (logger != null) logger.info(e);
		}
		return false;
	}

	/**
	 * Shuts down a future request, and waits for shutdown to complete, up to default time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, Future<?> future) {
		return close(logger, future, TIMEOUT_MS_DEF);
	}

	/**
	 * Shuts down a future request, and waits for shutdown to complete, up to given time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, Future<?> future, int timeoutMs) {
		if (future == null) return false;
		future.cancel(true);
		try {
			future.get(timeoutMs, TimeUnit.MILLISECONDS);
			return true;
		} catch (CancellationException e) {
			return true;
		} catch (InterruptedException | RuntimeInterruptedException e) {
			if (logger != null) logger.info(e);
		} catch (ExecutionException | TimeoutException e) {
			if (logger != null) logger.catching(Level.WARN, e);
		}
		return false;
	}

	/**
	 * Closes a collection of closeables, and logs thrown exceptions as a warnings. Returns true
	 * only if all closeables are successfully closed.
	 */
	public static boolean close(Logger logger, AutoCloseable... closeables) {
		return close(logger, Arrays.asList(closeables));
	}

	/**
	 * Closes a collection of closeables, and logs thrown exceptions as a warnings. Returns true
	 * only if all closeables are successfully closed.
	 */
	public static boolean close(Logger logger, Iterable<? extends AutoCloseable> closeables) {
		if (closeables == null) return false;
		boolean closed = true;
		for (AutoCloseable closeable : closeables)
			if (!close(logger, closeable, AutoCloseable::close)) closed = false;
		return closed;
	}

	/**
	 * Closes a closeable, and logs a thrown exception as a warning.
	 */
	public static <T> boolean close(Logger logger, T subject,
		ExceptionConsumer<? extends Exception, T> closeFunction) {
		if (subject == null) return false;
		try {
			closeFunction.accept(subject);
			return true;
		} catch (Exception e) {
			logger.catching(Level.WARN, e);
			return false;
		}
	}

	public static void fatalf(Logger logger, String format, Object... args) {
		logf(logger, Level.FATAL, format, args);
	}

	public static void errorf(Logger logger, String format, Object... args) {
		logf(logger, Level.ERROR, format, args);
	}

	public static void warnf(Logger logger, String format, Object... args) {
		logf(logger, Level.WARN, format, args);
	}

	public static void infof(Logger logger, String format, Object... args) {
		logf(logger, Level.INFO, format, args);
	}

	public static void debugf(Logger logger, String format, Object... args) {
		logf(logger, Level.DEBUG, format, args);
	}

	public static void tracef(Logger logger, String format, Object... args) {
		logf(logger, Level.TRACE, format, args);
	}

	public static void logf(Logger logger, Level level, String format, Object... args) {
		logger.log(level, toFormat(format, args));
	}

	/**
	 * Returns an object whose toString() executes the supplier method. Used for logging lazy string
	 * instantiations.
	 */
	public static Object toString(final ExceptionSupplier<?, String> stringSupplier) {
		return new ToString(stringSupplier);
	}

	/**
	 * Returns an object whose toString() executes the conversion method on the given object. Used
	 * for logging lazy string instantiations.
	 */
	public static <T> Object toString(T t, ExceptionFunction<?, T, String> converter) {
		return toString(() -> converter.apply(t));
	}

	/**
	 * Returns an object whose toString() returns the hex hash code.
	 */
	public static Object hashId(final Object obj) {
		return toString(() -> ReflectUtil.hashId(obj));
	}

	/**
	 * Returns an object whose toString() returns the hex hash code if toString has not been
	 * overridden.
	 */
	public static Object toStringOrHash(final Object obj) {
		return toString(() -> ReflectUtil.toStringOrHash(obj));
	}

	/**
	 * Returns an object whose toString() returns the hex string of the given integer value.
	 */
	public static Object toHex(final int value) {
		return toString(() -> Integer.toHexString(value));
	}

	/**
	 * Returns an object whose toString() returns the formatted string.
	 */
	public static Object toFormat(String format, Object... args) {
		return toString(() -> String.format(format, args));
	}

	/**
	 * Returns an object with a compact toString(), replacing multiple whitespace with a single
	 * space.
	 */
	public static Object compact(final Object obj) {
		return toString(() -> StringUtil.compact(String.valueOf(obj)));
	}

	/**
	 * Returns an object with an escaped toString(), replacing unprintable chars with literals.
	 */
	public static Object escaped(final Object obj) {
		return toString(() -> StringUtil.escape(String.valueOf(obj)));
	}

	/**
	 * Intro logging message.
	 */
	public static String introMessage(String title) {
		if (title.length() > TITLE_MAX_WIDTH) title = title.substring(0, TITLE_MAX_WIDTH);
		return "\n" +
			"================================================================================\n" +
			"|                                                                              |\n" +
			"| " + StringUtil.pad(title, TITLE_MAX_WIDTH, Align.H.center) + " |\n" +
			"|                                                                              |\n" +
			"================================================================================";
	}

	private static Logger logger(Logger logger) {
		return logger != null ? logger : LogUtil.logger;
	}

}
