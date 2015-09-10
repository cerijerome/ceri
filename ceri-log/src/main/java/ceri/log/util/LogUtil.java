package ceri.log.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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

	public static void execute(Logger logger, ExceptionRunnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			logger.catching(e);
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
	 * Closes a closeable, and logs a thrown exception as a warning.
	 */
	public static boolean close(Logger logger, Closeable closeable) {
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
	 * Interface used with toString to lazily generate a string.
	 */
	public static interface ToLazyString {
		String toLazyString() throws Exception;
	}

	/**
	 * Returns an object whose toString() executes the given toLazyString() method. Used for logging
	 * lazy string instantiations.
	 */
	public static Object toString(final ToLazyString toLazyString) {
		return new Object() {
			@Override
			public String toString() {
				try {
					return toLazyString.toLazyString();
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
