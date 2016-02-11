package ceri.common.test;

import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestThreadBehavior {

	@Test
	public void shouldFailToStopANonStartedThread() {
		TestThread<RuntimeException> t = TestThread.create(() -> {});
		assertException(() -> t.stop());
	}

	@Test
	public void shouldFailToStopAnIncompleteThread() {
		TestThread<Exception> t = TestThread.create(() -> {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				Thread.sleep(30000);
			}
		});
		t.start();
		assertException(() -> t.stop(1));
	}

	@Test
	public void shouldOnlyAllowStartingOnce() {
		TestThread<?> t = TestThread.create(() -> {});
		t.start();
		assertException(() -> t.start());
	}

	@Test
	public void shouldHandleInterruption() {
		TestThread<?> thread = TestThread.create(() -> {
			throw new InterruptedException();
		});
		thread.start();
		try {
			thread.stop(1000);
			fail();
		} catch (Throwable e) {
			assertTrue(e.getCause() instanceof InterruptedException);
		}
	}

}
