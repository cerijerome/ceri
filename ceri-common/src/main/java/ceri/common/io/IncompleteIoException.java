package ceri.common.io;

import java.io.IOException;

/**
 * An exception for read/write that did not transfer the expected number of bytes.
 */
@SuppressWarnings("serial")
public class IncompleteIoException extends IOException {
	public final int actual;
	public final int expected;

	public static void verify(int actual, int expected) throws IncompleteIoException {
		if (actual < expected) throw of(actual, expected);
	}

	public static IncompleteIoException of(int actual, int expected) {
		return of(actual, expected, message(actual, expected));
	}

	public static IncompleteIoException of(int actual, int expected, String message) {
		return of(actual, expected, message, null);
	}

	public static IncompleteIoException of(int actual, int expected, Throwable cause) {
		return of(actual, expected, message(actual, expected), cause);
	}

	public static IncompleteIoException of(int actual, int expected, String message,
		Throwable cause) {
		return new IncompleteIoException(actual, expected, message, cause);
	}

	private static String message(int actual, int expected) {
		return actual + "/" + expected;
	}

	private IncompleteIoException(int actual, int expected, String message, Throwable cause) {
		super(message, cause);
		this.actual = actual;
		this.expected = expected;
	}
}
