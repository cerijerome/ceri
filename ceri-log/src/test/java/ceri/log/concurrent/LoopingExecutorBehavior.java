package ceri.log.concurrent;

import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.concurrent.ValueCondition;

public class LoopingExecutorBehavior {
	private int count;
	private ValueCondition<Integer> condition;

	@Before
	public void init() {
		count = 0;
		condition = ValueCondition.create();
	}

	@Test
	public void shouldAllowCloseToBeInterrupted() {
		BooleanCondition flag = new BooleanCondition();
		try (LoopingExecutor loop = LoopingExecutor.create(100000, () -> {
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) { // happens when close() is called
				flag.signal();
				Thread.sleep(100000);
			}
		})) {
			loop.start();
			Thread current = Thread.currentThread();
			Executors.newSingleThreadExecutor().execute(() -> {
				waitFor(flag); // wait for loop to be interrupted
				current.interrupt(); // interrupt calling thread
			});
			loop.close(); // interrupt loop
		}
	}

	@Test
	public void shouldLoop() throws InterruptedException {
		try (LoopingExecutor loop = new LoopingExecutor() {
			@Override
			protected void loop() {
				count();
			}
		}) {
			loop.start();
			while (condition.await() < 10) {}
		}
	}

	@Test
	public void shouldStopOnException() throws InterruptedException {
		try (LoopingExecutor loop = LoopingExecutor.create(() -> countWithException(10))) {
			loop.start();
			while (condition.await() < 9) {}
			loop.start();
		}
	}

	private void waitFor(BooleanCondition flag) {
		try {
			flag.await();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	void count() {
		condition.signal(count++);
	}

	private void countWithException(int max) throws Exception {
		condition.signal(count++);
		if (count == max) throw new Exception();
	}

}
