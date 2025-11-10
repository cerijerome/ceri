package ceri.ffm.core;

import java.util.regex.Pattern;
import ceri.common.function.Excepts;
import ceri.common.text.Strings;

@SuppressWarnings("serial")
public class LastErrorException extends RuntimeException {
	private static final Pattern MESSAGE_REGEX = Pattern.compile("^\\[\\d+\\]\\s*");
	public final int errNo;

	/**
	 * Extracts the message without error code from the exception. Returns empty string if the
	 * message is just the number.
	 */
	public static String message(LastErrorException e) {
		if (e == null) return "";
		var message = e.getMessage();
		if (message == null) return "";
		return MESSAGE_REGEX.matcher(e.getMessage()).replaceFirst("").trim();
	}

	/**
	 * Captures the error code, or 0 if successful.
	 */
	public static int capture(Excepts.Runnable<? extends LastErrorException> runnable) {
		try {
			runnable.run();
			return 0;
		} catch (LastErrorException e) {
			return e.errNo;
		}
	}

	public static LastErrorException full(int errNo, String format, Object... args) {
		return of(errNo, "[" + errNo + "] " + Strings.format(format, args));
	}

	public static LastErrorException of(int errNo, String format, Object... args) {
		return new LastErrorException(errNo, Strings.format(format, args));
	}

	public LastErrorException(int errNo) {
		this(errNo, "errno");
	}

	public LastErrorException(int errNo, String message) {
		this(errNo, message, null);
	}

	public LastErrorException(int errNo, String message, Throwable cause) {
		super(message, cause);
		this.errNo = errNo;
	}
}
