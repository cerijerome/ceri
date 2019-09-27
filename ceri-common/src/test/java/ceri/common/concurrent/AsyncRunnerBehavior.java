package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertThrown;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.TestUtil;
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
		TestUtil.assertThrown(() -> runner2.join(-1));
		TestUtil.assertThrown(() -> runner1.join(1000));
	}

	@Test
	public void shouldOnlyThrowSpecifiedExceptions() {
		AsyncRunner<IOException> runner = AsyncRunner.create(IOException.class, () -> {
			throw new FileNotFoundException();
		}).start();
		assertThrown(IOException.class, () -> runner.join(0));
	}

	@Test
	public void shouldNotWrapRuntimeExceptions() {
		AsyncRunner<IOException> runner = AsyncRunner.create(IOException.class, () -> {
			throw new IllegalStateException();
		}).start();
		assertThrown(IllegalStateException.class, () -> runner.join(0));
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
		assertThrown(RuntimeException.class, () -> runner.join(0));
	}

}
