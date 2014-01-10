package ceri.common.test;

import java.io.PrintStream;
import ceri.common.reflect.ReflectUtil;

/**
 * Implements TestRunAdapter to print all methods to given print stream.
 */
public class TestRunPrinter extends TestRunAdapter {
	private final PrintStream out;
	
	public TestRunPrinter(PrintStream out) {
		this.out = out;
	}
	
	@Override
	public void testEnded(String arg0) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testFailed(int arg0, String arg1, String arg2) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0 + ", " + arg1 + ", " +
			arg2);
	}

	@Override
	public void testRunEnded(long arg0) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testRunStarted(String arg0, int arg1) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0 + ", " + arg1);
	}

	@Override
	public void testRunStopped(long arg0) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

	@Override
	public void testStarted(String arg0) {
		out.println(ReflectUtil.currentMethodName() + ": " + arg0);
	}

}
