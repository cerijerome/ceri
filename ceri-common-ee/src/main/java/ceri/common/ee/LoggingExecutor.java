package ceri.common.ee;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Convenience class to run tasks, wait for completion, and log any problems.
 */
public class LoggingExecutor implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final long SHUTDOWN_TIMEOUT_MS_DEF = 3000;
	private final ExecutorService service;
	private final long shutdownTimeoutMs;
	private Collection<Future<?>> futures = new ArrayList<>();

	public LoggingExecutor(ExecutorService service) {
		this(service, SHUTDOWN_TIMEOUT_MS_DEF);
	}

	public LoggingExecutor(ExecutorService service, long shutdownTimeoutMs) {
		this.service = service;
		this.shutdownTimeoutMs = shutdownTimeoutMs;
	}

	/**
	 * Shuts down the executor, interrupting any running tasks.
	 * Waits for completion up to time limit.
	 */
	@Override
	public void close() throws IOException {
		logger.info("Shutting down any running threads");
		service.shutdownNow();
		awaitTermination(shutdownTimeoutMs);
	}

	/**
	 * Executes a task using an executor service thread.
	 */
	public void execute(final Runnable runnable) {
		if (service.isShutdown()) throw new RuntimeInterruptedException("Executor is shut down");
		Future<?> future = service.submit(() -> {
			logger.debug("Thread started");
			try {
				runnable.run();
			} catch (RuntimeInterruptedException e) {
				logger.info("Thread interrupted");
			} catch (RuntimeException e) {
				logger.catching(e);
			}
			logger.debug("Thread complete");
		});
		futures.add(future);
	}

	/**
	 * Waits for all tasks to complete.
	 */
	public void awaitCompletion() {
		Iterator<Future<?>> i = futures.iterator();
		while (i.hasNext()) {
			awaitFuture(i.next());
			i.remove();
		}
	}

	private void awaitFuture(Future<?> future) {
		if (future == null) return;
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeException(e.getCause());
		}
	}

	private boolean awaitTermination(long timeoutMs) {
		try {
			logger.debug("Awaiting termination of threads");
			boolean complete = service.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
			if (!complete) logger.warn("Threads did not shut down in {}ms", timeoutMs);
			else logger.debug("Threads shut down successfully");
			return complete;
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		}
	}

}
