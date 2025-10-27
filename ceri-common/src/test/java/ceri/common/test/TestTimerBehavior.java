package ceri.common.test;

import org.junit.Test;

public class TestTimerBehavior {

	@Test
	public void shouldAssertBasedOnCurrentTime() throws InterruptedException {
		TestTimer timer = new TestTimer();
		timer.assertLessThan(1000);
		Thread.sleep(2);
		timer.assertMoreThan(1);
		Assert.yes(timer.ms() > 0);
	}

	@Test
	public void shouldAssertLessThan() throws InterruptedException {
		TestTimer timer = new TestTimer();
		Thread.sleep(1);
		try {
			timer.assertLessThan(1);
			Assert.fail();
		} catch (AssertionError e) {
			// Success
		}
	}

	@Test
	public void shouldAssertMoreThan() {
		TestTimer timer = new TestTimer();
		try {
			timer.assertMoreThan(1000000);
			Assert.fail();
		} catch (AssertionError e) {
			// Success
		}
	}
}
