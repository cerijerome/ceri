package ceri.common.exception;

import java.io.IOException;
import java.util.function.Function;
import ceri.common.text.StringUtil;

/**
 * Exception generation convenience methods.
 */
public class Exceptions {

	private Exceptions() {}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E from(Function<String, E> fn, String format,
		Object... args) {
		return from(fn, null, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E from(Function<String, E> fn, Throwable cause,
		String format, Object... args) {
		String message = StringUtil.format(format, args);
		return ExceptionUtil.initCause(fn.apply(message), cause);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException io(String format, Object... args) {
		return new IOException(StringUtil.format(format, args));
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException io(Throwable cause, String format, Object... args) {
		return new IOException(StringUtil.format(format, args), cause);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IllegalArgumentException illegalArg(String format, Object... args) {
		return illegalArg((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IllegalArgumentException illegalArg(Throwable cause, String format,
		Object... args) {
		return from(IllegalArgumentException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IllegalStateException illegalState(String format, Object... args) {
		return illegalState((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IllegalStateException illegalState(Throwable cause, String format,
		Object... args) {
		return from(IllegalStateException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static UnsupportedOperationException unsupportedOp(String format, Object... args) {
		return unsupportedOp((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static UnsupportedOperationException unsupportedOp(Throwable cause, String format,
		Object... args) {
		return from(UnsupportedOperationException::new, cause, format, args);
	}
}
