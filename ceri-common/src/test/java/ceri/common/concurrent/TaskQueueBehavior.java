package ceri.common.concurrent;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.throwIo;
import static ceri.common.test.Assert.throwRuntime;
import static ceri.common.test.Assert.thrown;
import static ceri.common.test.TestUtil.runRepeat;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.io.IOException;
import org.junit.Test;
import ceri.common.function.Excepts.Runnable;
import ceri.common.test.Assert;

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
		Assert.isNull(queue.executeGet(() -> {
			Concurrent.delay(100000);
			return 0;
		}, 1, MICROSECONDS));
	}

	@Test
	public void shouldThrowTypedExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(() -> thrown(() -> queue.processNext()))) {
			Assert.thrown(() -> queue.execute(() -> throwIo()));
		}
	}

	@Test
	public void shouldThrowRuntimeExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(() -> thrown(() -> queue.processNext()))) {
			Assert.thrown(() -> queue.executeGet(() -> throwRuntime()));
		}
	}

	@Test
	public void shouldFailToExecuteTaskIfQueueIsFull() throws Exception {
		BoolCondition error = BoolCondition.of();
		TaskQueue<?> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(task(queue, error))) {
			try (var _ = SimpleExecutor.run(task(queue, error))) {
				error.await();
			}
		}
	}

	private Runnable<?> task(TaskQueue<?> queue, BoolCondition error) {
		return () -> {
			try {
				queue.execute(() -> Concurrent.delay(100000));
			} finally {
				error.signal();
			}
		};
	}

}
