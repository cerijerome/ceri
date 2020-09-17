package ceri.log.concurrent;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;

public class CloseableExecutorBehavior {

	@Test
	public void shouldExecuteRunnable() throws InterruptedException {
		BooleanCondition sync = BooleanCondition.of();
		try (CloseableExecutor exec = CloseableExecutor.single()) {
			exec.execute(sync::signal);
			sync.await();
		}
	}

	@Test
	public void shouldSubmitTask() throws InterruptedException, ExecutionException {
		try (CloseableExecutor exec = CloseableExecutor.single()) {
			assertThat(exec.submit(() -> "test1").get(), is("test1"));
			assertThat(exec.submit(() -> {}, "test2").get(), is("test2"));
			assertNull(exec.submit(() -> {}).get());
		}
	}

	@Test
	public void shouldInvokeAllTasks() throws InterruptedException, ExecutionException {
		try (CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"));
			assertThat(result.get(0).get(), is("test1"));
			assertThat(result.get(1).get(), is("test2"));
		}
	}

	@Test
	public void shouldInvokeAllTasksWithTimeout() throws InterruptedException, ExecutionException {
		try (CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAll(List.of(() -> "test1", () -> "test2"), 1, TimeUnit.SECONDS);
			assertThat(result.get(0).get(), is("test1"));
			assertThat(result.get(1).get(), is("test2"));
		}
	}

	@Test
	public void shouldInvokeAnyTasks() throws InterruptedException, ExecutionException {
		try (CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")));
			assertThat(result, is("test2"));
		}
	}

	@Test
	public void shouldInvokeAnyTasksWithTimeout()
		throws InterruptedException, ExecutionException, TimeoutException {
		try (CloseableExecutor exec = CloseableExecutor.of(Executors.newCachedThreadPool())) {
			var result = exec.invokeAny(List.of(() -> call(10000, "test1"), () -> call(0, "test2")),
				1, TimeUnit.SECONDS);
			assertThat(result, is("test2"));
		}
	}

	private static String call(int delayMs, String response) {
		ConcurrentUtil.delay(delayMs);
		return response;
	}
	
}
