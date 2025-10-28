package ceri.common.concurrent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class TaskQueueBehavior {

	@Test
	public void shouldProcessTasks() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		// Start 2 threads and wait for one to generate an error
		try (var _ = Testing.runRepeat(queue::processNext)) {
			queue.execute(() -> {});
			queue.execute(() -> {}, 1000, TimeUnit.MILLISECONDS);
			Assert.equal(queue.executeGet(() -> "test"), "test");
			Assert.equal(queue.executeGet(() -> "test", 1000, TimeUnit.MILLISECONDS), "test");
		}
	}

	@Test
	public void shouldReturnFalseOnQueueTimeout() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(10);
		Assert.no(queue.processNext(1, TimeUnit.MICROSECONDS));
	}

	@Test
	public void shouldReturnNullOnTimeout() throws Exception {
		TaskQueue<?> queue = TaskQueue.of(1);
		Assert.isNull(queue.executeGet(() -> {
			Concurrent.delay(100000);
			return 0;
		}, 1, TimeUnit.MICROSECONDS));
	}

	@Test
	public void shouldThrowTypedExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(() -> Assert.thrown(() -> queue.processNext()))) {
			Assert.thrown(() -> queue.execute(() -> Assert.throwIo()));
		}
	}

	@Test
	public void shouldThrowRuntimeExceptionInBothThreads() {
		TaskQueue<IOException> queue = TaskQueue.of(1);
		// Start 2 threads and wait for one to generate an error
		try (var _ = SimpleExecutor.run(() -> Assert.thrown(() -> queue.processNext()))) {
			Assert.thrown(() -> queue.executeGet(() -> Assert.throwRuntime()));
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

	private Excepts.Runnable<?> task(TaskQueue<?> queue, BoolCondition error) {
		return () -> {
			try {
				queue.execute(() -> Concurrent.delay(100000));
			} finally {
				error.signal();
			}
		};
	}
}
