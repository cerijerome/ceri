package ceri.log.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.log.util.LogUtil;

/**
 * Encapsulates a standard executor service as a Closeable resource.
 */
public class CloseableExecutor implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	public final ExecutorService executor;

	public static CloseableExecutor single() {
		return of(Executors.newSingleThreadExecutor());
	}

	public static CloseableExecutor of(ExecutorService executor) {
		return new CloseableExecutor(executor);
	}

	private CloseableExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void execute(Runnable command) {
		executor.execute(command);
	}

	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}

	public <T> Future<T> submit(Runnable task, T result) {
		return executor.submit(task, result);
	}

	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
		throws InterruptedException {
		return executor.invokeAll(tasks);
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
		TimeUnit unit) throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
		throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
	}

	@Override
	public void close() {
		LogUtil.close(logger, executor);
	}
}
