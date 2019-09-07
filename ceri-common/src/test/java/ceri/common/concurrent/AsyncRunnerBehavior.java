package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.util.BasicUtil;

public class AsyncRunnerBehavior {

	@Test
	public void shouldShutDownGracefully() throws Exception {
		AsyncRunner<?> runner1 = AsyncRunner.create(() -> BasicUtil.delay(100000)).start();
		AsyncRunner<?> runner2 = AsyncRunner.create(() -> runner1.join(100000)).start();
		AsyncRunner<?> runner3 = AsyncRunner.create(() -> {
			runner1.interrupt();
			runner2.interrupt();
		}).start();
		runner3.join(1000);
		assertException(() -> runner2.join(-1));
		assertException(() -> runner1.join(1000));
	}

	@Test
	public void shouldOnlyThrowSpecifiedExceptions() {
		AsyncRunner<IOException> runner = AsyncRunner.create(IOException.class, () -> {
			throw new FileNotFoundException();
		}).start();
		assertException(IOException.class, () -> runner.join(0));
	}

	@Test
	public void shouldNotWrapRuntimeExceptions() {
		AsyncRunner<IOException> runner = AsyncRunner.create(IOException.class, () -> {
			throw new IllegalStateException();
		}).start();
		assertException(IllegalStateException.class, () -> runner.join(0));
	}

	@Test
	public void shouldExecuteRunnerTask() throws InterruptedException {
		BooleanCondition flag = BooleanCondition.of();
		AsyncRunner<RuntimeException> runner = AsyncRunner.create(flag::signal).start();
		flag.await();
		runner.join(0);
	}

	@Test
	public void shouldThrowExceptionIfInterrupted() {
		AsyncRunner<RuntimeException> runner =
			AsyncRunner.create(() -> Thread.sleep(1000000)).start().interrupt();
		assertException(RuntimeException.class, () -> runner.join(0));
	}

}
