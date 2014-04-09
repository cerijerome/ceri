package ceri.common.ee;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * ExecutorService utility methods with logging.
 */
public class ExecutorUtil {
	private static final Logger logger = LogManager.getLogger();
	private static final Object OBJ = new Object();

	private ExecutorUtil() {}

	public static Future<?> execute(ExecutorService service, final Runnable runnable) {
		return execute(service, runnable, OBJ);
	}

	public static <T> Future<T> execute(ExecutorService service, final Runnable runnable, T result) {
		if (service.isShutdown()) throw new RuntimeInterruptedException("Executor is shut down");
		return service.submit(() -> {
			logger.debug("Thread started");
			try {
				runnable.run();
			} catch (RuntimeInterruptedException e) {
				logger.info("Thread interrupted");
			} catch (RuntimeException e) {
				logger.catching(e);
			}
			logger.debug("Thread complete");
		}, result);
	}

	public static void close(ExecutorService service, long timeoutMs) {
		logger.info("Shutting down any running threads");
		service.shutdownNow();
		awaitTermination(service, timeoutMs);
	}

	public static boolean awaitTermination(ExecutorService service, long timeoutMs) {
		try {
			logger.debug("Awaiting termination of executor service");
			boolean complete = service.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
			if (!complete) logger.warn("Executor service did not shut down in {}ms", timeoutMs);
			else logger.debug("Executor service shut down successfully");
			return complete;
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		}
	}

	public static <T> T awaitFuture(Future<T> future) {
		if (future == null) return null;
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeException(e.getCause());
		}
	}

}
