package ceri.log.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import ceri.common.concurrent.BoolCondition;
import ceri.common.concurrent.Concurrent;

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
			assertEquals(exec.submit(() -> "test1").get(), "test1");
			assertEquals(exec.submit(() -> {}, "test2").get(), "test2");
			assertNull(exec.submit(() -> {}).get());
		}
	}

	@Test
	public void shouldInvokeAllTasks() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"));
			assertEquals(result.get(0).get(), "test1");
			assertEquals(result.get(1).get(), "test2");
		}
	}

	@Test
	public void shouldInvokeAllTasksWithTimeout() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"), 1, TimeUnit.SECONDS);
			assertEquals(result.get(0).get(), "test1");
			assertEquals(result.get(1).get(), "test2");
		}
	}

	@Test
	public void shouldInvokeAnyTasks() throws InterruptedException, ExecutionException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result =
				exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")));
			assertEquals(result, "test2");
		}
	}

	@Test
	public void shouldInvokeAnyTasksWithTimeout()
		throws InterruptedException, ExecutionException, TimeoutException {
		try (@SuppressWarnings("resource")
		CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")),
				1, TimeUnit.SECONDS);
			assertEquals(result, "test2");
		}
	}

	private static String call(int delayMs, String response) {
		Concurrent.delay(delayMs);
		return response;
	}

}
