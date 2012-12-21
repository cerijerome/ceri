package ceri.common.test;

import static org.junit.Assert.assertTrue;

public class TestTimer {
	private long snapShot;
	
	public TestTimer() {
		reset();
	}
	
	public void reset() {
		snapShot = System.currentTimeMillis();
	}
	
	public void assertMoreThan(long ms) {
		long t = ms();
		assertTrue("Time passed is less than " + ms + "ms: " + t, t > ms);
	}
	
	public void assertLessThan(long ms) {
		long t = ms();
		assertTrue("Time passed is more than " + ms + "ms: " + t, t < ms);
	}
	
	public long ms() {
		return System.currentTimeMillis() - snapShot;
	}
}
