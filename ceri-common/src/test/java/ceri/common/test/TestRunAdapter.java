package ceri.common.test;

import junit.runner.TestRunListener;

/**
 * Implements TestRunListener with no-op methods.
 * Override this class to implement specific overrides.
 */
public class TestRunAdapter implements TestRunListener {

	@Override
	public void testEnded(String arg0) {
	}

	@Override
	public void testFailed(int arg0, String arg1, String arg2) {
	}

	@Override
	public void testRunEnded(long arg0) {
	}

	@Override
	public void testRunStarted(String arg0, int arg1) {
	}

	@Override
	public void testRunStopped(long arg0) {
	}

	@Override
	public void testStarted(String arg0) {
	}


}
