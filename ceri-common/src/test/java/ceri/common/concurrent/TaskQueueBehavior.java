package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.runRepeat;
import static ceri.common.test.TestUtil.throwIt;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.ExceptionRunnable;

public class TaskQueueBehavior {

	@Test
	public void shouldProcessTasks() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		// Start 2 threads and wait for one to generate an error
		try (var exec = runRepeat(queue::processNext)) {
			queue.execute(() -> {});
			queue.execute(() -> {}, 1000, MILLISECONDS);
			assertThat(queue.executeGet(() -> "test"), is("test"));
			assertThat(queue.executeGet(() -> "test", 1000, MILLISECONDS), is("test"));
		}
	}

	@Test
	public void shouldReturnFalseOnQueueTimeout() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		assertThat(queue.processNext(1, MICROSECONDS), is(false));
	}

	@Test
	public void shouldReturnNullOnTimeout() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(1);
		assertNull(queue.executeGet(() -> {
			ConcurrentUtil.delay(100000);
			return 0;
		}, 1, MICROSECONDS));
	}

	@Test
	public void shouldThrowTypedExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var exec = SimpleExecutor.run(() -> assertThrown(() -> queue.processNext()))) {
			assertThrown(() -> queue.execute(() -> throwIt(new IOException("test"))));
		}
	}

	@Test
	public void shouldThrowRuntimeExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var exec = SimpleExecutor.run(() -> assertThrown(() -> queue.processNext()))) {
			assertThrown(() -> queue.executeGet(() -> throwIt(new RuntimeException("test"))));
		}
	}

	@Test
	public void shouldFailToExecuteTaskIfQueueIsFull() throws Exception {
		BooleanCondition error = BooleanCondition.of();
		TaskQueue<?> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var exec0 = SimpleExecutor.run(task(queue, error))) {
			try (var exec1 = SimpleExecutor.run(task(queue, error))) {
				error.await();
			}
		}
	}

	private ExceptionRunnable<?> task(TaskQueue<?> queue, BooleanCondition error) {
		return () -> {
			try {
				queue.execute(() -> ConcurrentUtil.delay(100000));
			} finally {
				error.signal();
			}
		};
	}

}
