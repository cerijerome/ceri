package ceri.common.io;

import java.io.IOException;
import ceri.common.text.Strings;

/**
 * IO exceptions.
 */
public class IoExceptions {

	private IoExceptions() {}

	/**
	 * A runtime general IO exception.
	 */
	public static class Runtime extends RuntimeException {
		private static final long serialVersionUID = 3922679551995652749L;

		public Runtime(String message, Throwable e) {
			super(message, e);
		}

		public Runtime(String message) {
			super(message);
		}

		public Runtime(Throwable e) {
			super(e);
		}
	}

	/**
	 * A runtime end-of-file exception.
	 */
	@SuppressWarnings("serial")
	public static class RuntimeEof extends Runtime {

		public static RuntimeEof of() {
			return new RuntimeEof(null, null);
		}

		public static RuntimeEof of(String format, Object... args) {
			return of(null, format, args);
		}

		public static RuntimeEof of(Throwable t, String format, Object... args) {
			return new RuntimeEof(Strings.format(format, args), t);
		}

		private RuntimeEof(String message, Throwable e) {
			super(message, e);
		}
	}

	/**
	 * An IO timeout exception.
	 */
	@SuppressWarnings("serial")
	public static class Timeout extends IOException {

		public Timeout(String message) {
			super(message);
		}

		public Timeout(Throwable e) {
			super(e);
		}
	}

	/**
	 * An exception for a read/write that did not transfer the expected number of bytes.
	 */
	@SuppressWarnings("serial")
	public static class Incomplete extends IOException {
		public final int actual;
		public final int expected;

		public static void verify(int actual, int expected) throws Incomplete {
			if (actual < expected) throw of(actual, expected);
		}

		public static Incomplete of(int actual, int expected) {
			return of(actual, expected, message(actual, expected));
		}

		public static Incomplete of(int actual, int expected, String message) {
			return of(actual, expected, message, null);
		}

		public static Incomplete of(int actual, int expected, Throwable cause) {
			return of(actual, expected, message(actual, expected), cause);
		}

		public static Incomplete of(int actual, int expected, String message, Throwable cause) {
			return new Incomplete(actual, expected, message, cause);
		}

		private static String message(int actual, int expected) {
			return actual + "/" + expected;
		}

		private Incomplete(int actual, int expected, String message, Throwable cause) {
			super(message, cause);
			this.actual = actual;
			this.expected = expected;
		}
	}
}
