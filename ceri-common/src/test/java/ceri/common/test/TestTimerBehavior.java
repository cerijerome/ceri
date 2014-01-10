package ceri.common.test;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestTimerBehavior {

	@Test
	public void should() throws InterruptedException {
		TestTimer timer = new TestTimer();
		timer.assertLessThan(1000);
		Thread.sleep(2);
		timer.assertMoreThan(1);
		assertTrue(timer.ms() > 0);
	}

}
