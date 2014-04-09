package ceri.common.ee;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Convenience class to run tasks, wait for completion, and log any problems.
 */
public class LoggingExecutor implements Closeable {
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
		ExecutorUtil.close(service, shutdownTimeoutMs);
	}

	/**
	 * Executes a task using an executor service thread.
	 */
	public void execute(final Runnable runnable) {
		futures.add(ExecutorUtil.execute(service, runnable));
	}

	/**
	 * Waits for all tasks to complete.
	 */
	public void awaitCompletion() {
		Iterator<Future<?>> i = futures.iterator();
		while (i.hasNext()) {
			ExecutorUtil.awaitFuture(i.next());
			i.remove();
		}
	}

}
