package ceri.log.concurrent;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.Concurrent;
import ceri.common.test.Assert;

public class CloseableExecutorBehavior {

	@Test
	public void shouldExecuteRunnable() throws InterruptedException {
		BoolCondition sync = BoolCondition.of();
		try (CloseableExecutor exec = CloseableExecutor.single()) {
			exec.execute(sync::signal);
			sync.await();
		}
	}

	@Test
	public void shouldSubmitTask() throws InterruptedException, ExecutionException {
		try (CloseableExecutor exec = CloseableExecutor.single()) {
			Assert.equal(exec.submit(() -> "test1").get(), "test1");
			Assert.equal(exec.submit(() -> {}, "test2").get(), "test2");
			Assert.isNull(exec.submit(() -> {}).get());
		}
	}

	@Test
	public void shouldInvokeAllTasks() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"));
			Assert.equal(result.get(0).get(), "test1");
			Assert.equal(result.get(1).get(), "test2");
		}
	}

	@Test
	public void shouldInvokeAllTasksWithTimeout() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"), 1, TimeUnit.SECONDS);
			Assert.equal(result.get(0).get(), "test1");
			Assert.equal(result.get(1).get(), "test2");
		}
	}

	@Test
	public void shouldInvokeAnyTasks() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result =
				exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")));
			Assert.equal(result, "test2");
		}
	}

	@Test
	public void shouldInvokeAnyTasksWithTimeout()
		throws InterruptedException, ExecutionException, TimeoutException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")),
				1, TimeUnit.SECONDS);
			Assert.equal(result, "test2");
		}
	}

	private static String call(int delayMs, String response) {
		Concurrent.delay(delayMs);
		return response;
	}

}
