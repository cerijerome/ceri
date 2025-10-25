package ceri.common.test;

import static ceri.common.test.Assert.assertEquals;

/**
 * Utility to check elapsed time during tests.
 */
public class TestTimer {
	private long snapShot;

	/**
	 * Creates the timer and resets the timer.
	 */
	public TestTimer() {
		reset();
	}

	/**
	 * Resets the timer.
	 */
	public void reset() {
		snapShot = System.currentTimeMillis();
	}

	/**
	 * Asserts the given number of milliseconds have passed since the last reset.
	 */
	public void assertMoreThan(long ms) {
		long t = ms();
		assertEquals(t > ms, true, "Time passed is less than " + ms + "ms: " + t);
	}

	/**
	 * Asserts the given number of milliseconds have not passed since the last reset.
	 */
	public void assertLessThan(long ms) {
		long t = ms();
		assertEquals(t < ms, true, "Time passed is more than " + ms + "ms: " + t);
	}

	/**
	 * Returns the number of milliseconds passed since the last reset.
	 */
	public long ms() {
		return System.currentTimeMillis() - snapShot;
	}
}
