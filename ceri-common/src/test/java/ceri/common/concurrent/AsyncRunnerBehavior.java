package ceri.common.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class AsyncRunnerBehavior {

	@Test(expected = IOException.class)
	public void shouldOnlyThrowSpecifiedExceptions() throws Exception {
		AsyncRunner<IOException> runner = new AsyncRunner<IOException>(IOException.class) {
			@Override
			protected void run() throws Exception {
				throw new FileNotFoundException();
			}
		};
		runner.start();
		runner.join(0);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldNotWrapRuntimeExceptions() throws Exception {
		AsyncRunner<IOException> runner = new AsyncRunner<IOException>(IOException.class) {
			@Override
			protected void run() throws Exception {
				throw new IllegalStateException();
			}
		};
		runner.start();
		runner.join(0);
	}

	@Test(expected = RuntimeException.class)
	public void shouldInterrupt() {
		AsyncRunner<RuntimeException> runner =
			new AsyncRunner<RuntimeException>(RuntimeException.class) {
				@Override
				protected void run() throws Exception {
					Thread.sleep(1000);
				}
			};
		runner.start();
		runner.interrupt();
		runner.join(0);
	}

	@Test
	public void should() {
		final BooleanCondition flag = new BooleanCondition();
		AsyncRunner<RuntimeException> runner =
			new AsyncRunner<RuntimeException>(RuntimeException.class) {
				@Override
				protected void run() {
					flag.signal();
				}
			};
		assertFalse(flag.value());
		runner.start();
		runner.join(0);
		assertTrue(flag.value());
	}

}
