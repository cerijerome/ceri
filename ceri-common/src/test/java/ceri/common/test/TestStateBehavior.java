package ceri.common.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.ExceptionRunnable;
import ceri.common.util.BasicUtil;

public class TestStateBehavior {
	private TestThread<IOException> thread = null;
	private TestState<String> state;

	@Before
	public void init() {
		state = new TestState<>();
	}

	@After
	public void tidyUp() {
		try {
			if (thread != null) thread.stop();
		} catch (Exception e) {
			// ignore
		}
	}

	@Test
	public void shouldThrowExceptionWhenInterrupted() {
		start(() -> state.waitFor("x", 0));
		thread.interrupt();
		TestUtil.assertThrown(() -> thread.join());
	}

	@Test
	public void shouldWaitForValue() throws IOException {
		start(() -> state.waitFor("x", 0));
		BasicUtil.delay(1);
		state.set("x");
		thread.join();
	}

	private void start(ExceptionRunnable<IOException> runnable) {
		thread = TestThread.create(runnable);
		thread.start();
	}

}
