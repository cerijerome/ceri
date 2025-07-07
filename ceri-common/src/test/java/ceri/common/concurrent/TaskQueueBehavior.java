package ceri.common.concurrent;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwIo;
import static ceri.common.test.AssertUtil.throwRuntime;
import static ceri.common.test.TestUtil.runRepeat;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Excepts.Runnable;

public class TaskQueueBehavior {

	@Test
	public void shouldProcessTasks() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		// Start 2 threads and wait for one to generate an error
		try (var _ = runRepeat(queue::processNext)) {
			queue.execute(() -> {});
			queue.execute(() -> {}, 1000, MILLISECONDS);
			assertEquals(queue.executeGet(() -> "test"), "test");
			assertEquals(queue.executeGet(() -> "test", 1000, MILLISECONDS), "test");
		}
	}

	@Test
	public void shouldReturnFalseOnQueueTimeout() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		assertFalse(queue.processNext(1, MICROSECONDS));
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
		try (var _ = SimpleExecutor.run(() -> assertThrown(() -> queue.processNext()))) {
			assertThrown(() -> queue.execute(() -> throwIo()));
		}
	}

	@Test
	public void shouldThrowRuntimeExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(() -> assertThrown(() -> queue.processNext()))) {
			assertThrown(() -> queue.executeGet(() -> throwRuntime()));
		}
	}

	@Test
	public void shouldFailToExecuteTaskIfQueueIsFull() throws Exception {
		BooleanCondition error = BooleanCondition.of();
		TaskQueue<?> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(task(queue, error))) {
			try (var _ = SimpleExecutor.run(task(queue, error))) {
				error.await();
			}
		}
	}

	private Runnable<?> task(TaskQueue<?> queue, BooleanCondition error) {
		return () -> {
			try {
				queue.execute(() -> ConcurrentUtil.delay(100000));
			} finally {
				error.signal();
			}
		};
	}

}
