package ceri.common.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestThreadBehavior {

	@Test
	public void shouldHandleInterruption() {
		TestThread thread = new TestThread() {
			@Override
			protected void run() throws Exception {
				throw new InterruptedException();
			}
		};
		thread.start();
		try {
			thread.stop();
			fail();
		} catch (Exception e) {
			assertTrue(e.getCause() instanceof InterruptedException);
		}
	}

}
