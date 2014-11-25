package ceri.common.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestStateBehavior {

	@Test
	public void shouldHandleInterruption() {
		final TestState<Integer> state = new TestState<>();
		final Thread current = Thread.currentThread();
		TestThread<?> thread = TestThread.create(() -> {
			state.set(0);
			current.interrupt();
		});
		thread.start();
		state.waitFor(0, 0);
		try {
			state.waitFor(1, 0);
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof InterruptedException);
		}
	}

}
