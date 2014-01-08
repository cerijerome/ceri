package ceri.common.test;

import ceri.common.reflect.ReflectUtil;

/**
 * Implements TestRunListener with no-op methods.
 * Override this class to implement specific overrides.
 */
public class TestRunPrinter extends TestRunAdapter {

	@Override
	public void testEnded(String arg0) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testFailed(int arg0, String arg1, String arg2) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0 + ", " + arg1 + ", " + arg2);
	}

	@Override
	public void testRunEnded(long arg0) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testRunStarted(String arg0, int arg1) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0 + ", " + arg1);
	}

	@Override
	public void testRunStopped(long arg0) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testStarted(String arg0) {
		System.out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}


}
