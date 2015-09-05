package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class AsyncRunnerBehavior {

	@Test
	public void shouldOnlyThrowSpecifiedExceptions() {
		AsyncRunner<IOException> runner = runner(IOException.class, () -> {
			throw new FileNotFoundException();
		});
		runner.start();
		assertException(IOException.class, () -> runner.join(0));
	}

	@Test
	public void shouldNotWrapRuntimeExceptions() {
		AsyncRunner<IOException> runner = runner(IOException.class, () -> {
			throw new IllegalStateException();
		});
		runner.start();
		assertException(IllegalStateException.class, () -> runner.join(0));
	}
	
	@Test
	public void shouldExecuteRunnerTask() throws InterruptedException {
		BooleanCondition flag = BooleanCondition.create();
		AsyncRunner<RuntimeException> runner = runner(() -> flag.signal());
		runner.start();
		flag.await();
		runner.join(0);
	}

	@Test
	public void shouldThrowExceptionIfInterrupted() {
		AsyncRunner<RuntimeException> runner = runner(() -> Thread.sleep(1000000));
		runner.start();
		runner.interrupt();
		assertException(RuntimeException.class, () -> runner.join(0));
	}

	private AsyncRunner<RuntimeException> runner(ExceptionRunnable<?> runnable) {
		return runner(RuntimeException.class, runnable);
	}

	private <E extends Exception> AsyncRunner<E>
		runner(Class<E> cls, ExceptionRunnable<?> runnable) {
		return new AsyncRunner<E>(cls) {
			@Override
			protected void run() throws Exception {
				runnable.run();
			}
		};
	}

}
