package ceri.log.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ExceptionRunnable;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	private static final int TIMEOUT_MS_DEF = 1000;
	static final Pattern SPACE_REGEX = Pattern.compile("\\s+");

	private LogUtil() {}

	/**
	 * Execute runnable and log any exception as an error.
	 */
	public static void execute(Logger logger, ExceptionRunnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			logger.catching(e);
		}
	}

	/**
	 * Constructs a list of Closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	@SafeVarargs
	public static <T, R extends Closeable> List<R> create(Logger logger,
		Function<T, R> constructor, T...inputs) {
		return create(logger, constructor, Arrays.asList(inputs));
	}
	
	/**
	 * Constructs a list of Closeable instances from each input object and the constructor. If any
	 * exception occurs the already created instances will be closed.
	 */
	public static <T, R extends Closeable> List<R> create(Logger logger,
		Function<T, R> constructor, Collection<T> inputs) {
		List<R> results = new ArrayList<>();
		try {
			for (T input : inputs)
				results.add(constructor.apply(input));
			return results;
		} catch (RuntimeException e) {
			close(logger, results);
			throw e;
		}
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
		executor.shutdownNow();
		try {
			return executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
		return false;
	}

	/**
	 * Closes a collection of closeables, and logs thrown exceptions as a warnings. Returns true
	 * only if all closeables are successfully closed.
	 */
	public static boolean close(Logger logger, Closeable...closeables) {
		return close(logger, Arrays.asList(closeables));
	}
	
	/**
	 * Closes a collection of closeables, and logs thrown exceptions as a warnings. Returns true
	 * only if all closeables are successfully closed.
	 */
	public static boolean close(Logger logger, Collection<? extends Closeable> closeables) {
		if (closeables == null) return false;
		boolean closed = true;
		for (Closeable closeable : closeables)
			if (!closeWithLogging(logger, closeable)) closed = false;
		return closed;
	}

	/**
	 * Closes a closeable, and logs a thrown exception as a warning.
	 */
	private static boolean closeWithLogging(Logger logger, Closeable closeable) {
		if (closeable == null) return false;
		try {
			closeable.close();
			return true;
		} catch (IOException | RuntimeException e) {
			logger.catching(Level.WARN, e);
			return false;
		}
	}

	/**
	 * Returns an object whose toString() executes the supplier method. Used for logging lazy string
	 * instantiations.
	 */
	public static Object toString(final Callable<String> stringSupplier) {
		return new Object() {
			@Override
			public String toString() {
				try {
					return stringSupplier.call();
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	/**
	 * Returns an object whose toString() returns the hex string of the given integer value.
	 */
	public static Object toHex(final int value) {
		return toString(() -> Integer.toHexString(value));
	}

	/**
	 * Returns an object with a compact toString(), replacing multiple whitespace with a single
	 * space.
	 */
	public static Object compact(final Object obj) {
		return toString(() -> SPACE_REGEX.matcher(String.valueOf(obj)).replaceAll(" "));
	}

}