package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class AsyncRunnerBehavior {

	@Test
	public void shouldOnlyThrowSpecifiedExceptions() throws Exception {
		AsyncRunner<IOException> runner = runner(IOException.class, () -> {
			throw new FileNotFoundException();
		});
		runner.start();
		assertException(IOException.class, () -> runner.join(0));
	}

	@Test
	public void shouldNotWrapRuntimeExceptions() throws Exception {
		AsyncRunner<IOException> runner = runner(IOException.class, () -> {
			throw new IllegalStateException();
		});
		runner.start();
		assertException(IllegalStateException.class, () -> runner.join(0));
	}

	@Test
	public void shouldNotThrowExceptionIfJoinIsInterrupted() {
		Thread thread = Thread.currentThread();
		AsyncRunner<RuntimeException> runner = runner(() -> Thread.sleep(1000000));
		AsyncRunner<RuntimeException> runner2 = runner(() -> thread.interrupt());
		runner.start();
		runner2.start();
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
