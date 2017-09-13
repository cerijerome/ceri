package ceri.log.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.text.StringUtil;
import ceri.common.text.StringUtil.Align;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	private static final int TIMEOUT_MS_DEF = 1000;
	private static final int TITLE_MAX_WIDTH = 76;

	private LogUtil() {}

	/**
	 * Execute runnable and log any exception as an error.
	 */
	public static void execute(Logger logger, ExceptionRunnable<Exception> runnable) {
		try {
			if (runnable != null) runnable.run();
		} catch (Exception e) {
			if (logger != null) logger.catching(e);
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
	 * Shuts down a process, and waits for shutdown to complete, up to a default time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, Process process) {
		return close(logger, process, TIMEOUT_MS_DEF);
	}
	
	/**
	 * Shuts down a process, and waits for shutdown to complete, up to given time in
	 * milliseconds.
	 */
	public static boolean close(Logger logger, Process process, int timeoutMs) {
		if (process == null) return false;
		process.destroy();
		try {
			return process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			if (logger != null) logger.catching(Level.INFO, e);
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
		} catch (InterruptedException e) {
			if (logger != null) logger.catching(Level.INFO, e);
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
		} catch (ExecutionException | TimeoutException | InterruptedException e) {
			if (logger != null) logger.catching(Level.WARN, e);
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
		return toString(() -> StringUtil.compact(String.valueOf(obj)));
	}

	/**
	 * Intro logging message.
	 */
	public static String introMessage(String title) {
		if (title.length() > TITLE_MAX_WIDTH) title = title.substring(0, TITLE_MAX_WIDTH);
		return "\n" +
			"================================================================================\n" +
			"|                                                                              |\n" +
			"| " + StringUtil.pad(title, TITLE_MAX_WIDTH, Align.CENTER) + " |\n" +
			"|                                                                              |\n" +
			"================================================================================";
	}

}
