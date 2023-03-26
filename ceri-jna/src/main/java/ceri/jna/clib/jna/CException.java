package ceri.jna.clib.jna;

import java.io.IOException;
import java.util.function.IntConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.text.StringUtil;

public class CException extends IOException {
	private static final long serialVersionUID = -1L;
	private static final int GENERAL_ERROR_CODE = -1;
	public final CError error;
	public final int code;

	public static class Runtime extends RuntimeException {
		private static final long serialVersionUID = -1L;
		public final int code;

		private Runtime(CException e) {
			super(e.getMessage(), e);
			code = e.code;
		}
	}

	/**
	 * Capture the error code to a consumer and rethrow the error.
	 */
	public static <E extends CException> void intercept(ExceptionRunnable<E> runnable,
		IntConsumer consumer) throws E {
		try {
			runnable.run();
			consumer.accept(0);
		} catch (CException e) {
			consumer.accept(e.code);
			throw e;
		}
	}

	/**
	 * Capture the error code or 0 if successful.
	 */
	public static int capture(ExceptionRunnable<? extends CException> runnable) {
		try {
			runnable.run();
			return 0;
		} catch (CException e) {
			return e.code;
		}
	}

	/**
	 * Create exception formatted message.
	 */
	public static CException of(CError error, String format, Object... args) {
		return new CException(StringUtil.format(format, args), code(error), error);
	}

	/**
	 * Create exception formatted message.
	 */
	public static CException of(int code, String format, Object... args) {
		return new CException(StringUtil.format(format, args), code, CError.from(code));
	}

	/**
	 * Create exception with general purpose error code and formatted message.
	 */
	public static CException general(String format, Object... args) {
		return of(GENERAL_ERROR_CODE, format, args);
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static CException full(String message, int code) {
		return full(message, code, CError.from(code));
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static CException full(String message, CError error) {
		return full(message, code(error), error);
	}

	private static CException full(String message, int code, CError error) {
		return new CException(error == null ? String.format("%s: %d", message, code)
			: String.format("%s: %d (%s)", message, code, error), code, error);
	}

	protected CException(String message, int code, CError error) {
		super(message);
		this.error = error;
		this.code = code;
	}

	/**
	 * Convert to runtime exception.
	 */
	public CException.Runtime runtime() {
		return new CException.Runtime(this);
	}

	private static int code(CError error) {
		return error != null ? error.code : GENERAL_ERROR_CODE;
	}
}
