package ceri.common.test;

import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.fail;
import org.junit.Test;

public class TestTimerBehavior {

	@Test
	public void shouldAssertBasedOnCurrentTime() throws InterruptedException {
		TestTimer timer = new TestTimer();
		timer.assertLessThan(1000);
		Thread.sleep(2);
		timer.assertMoreThan(1);
		assertTrue(timer.ms() > 0);
	}

	@Test
	public void shouldAssertLessThan() throws InterruptedException {
		TestTimer timer = new TestTimer();
		Thread.sleep(1);
		try {
			timer.assertLessThan(1);
			fail();
		} catch (AssertionError e) {
			// Success
		}
	}

	@Test
	public void shouldAssertMoreThan() {
		TestTimer timer = new TestTimer();
		try {
			timer.assertMoreThan(1000000);
			fail();
		} catch (AssertionError e) {
			// Success
		}
	}

}
