package ceri.common.test;

import java.io.PrintStream;
import ceri.common.reflect.Reflect;
import junit.runner.TestRunListener;

/**
 * Implements TestRunListener with no-op methods. Override this class to implement specific
 * overrides.
 */
public class TestRunAdapter implements TestRunListener {

	/**
	 * Implementation to print all methods to given print stream.
	 */
	public static TestRunAdapter printer(PrintStream out) {
		return new Printer(out);
	}

	@Override
	public void testEnded(String arg0) {}

	@Override
	public void testFailed(int arg0, String arg1, String arg2) {}

	@Override
	public void testRunEnded(long arg0) {}

	@Override
	public void testRunStarted(String arg0, int arg1) {}

	@Override
	public void testRunStopped(long arg0) {}

	@Override
	public void testStarted(String arg0) {}

	private static class Printer extends TestRunAdapter {
		private final PrintStream out;

		private Printer(PrintStream out) {
			this.out = out;
		}

		@Override
		public void testEnded(String arg0) {
			out.println(Reflect.currentMethodName() + ": " + arg0);
		}

		@Override
		public void testFailed(int arg0, String arg1, String arg2) {
			out.println(Reflect.currentMethodName() + ": " + arg0 + ", " + arg1 + ", " + arg2);
		}

		@Override
		public void testRunEnded(long arg0) {
			out.println(Reflect.currentMethodName() + ": " + arg0);
		}

		@Override
		public void testRunStarted(String arg0, int arg1) {
			out.println(Reflect.currentMethodName() + ": " + arg0 + ", " + arg1);
		}

		@Override
		public void testRunStopped(long arg0) {
			out.println(Reflect.currentMethodName() + ": " + arg0);
		}

		@Override
		public void testStarted(String arg0) {
			out.println(Reflect.currentMethodName() + ": " + arg0);
		}
	}
}
