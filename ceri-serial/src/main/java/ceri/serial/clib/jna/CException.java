package ceri.serial.clib.jna;

import java.io.IOException;
import java.util.function.IntConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.text.StringUtil;

public class CException extends IOException {
	private static final long serialVersionUID = -1L;
	private static final int GENERAL_ERROR_CODE = -1;
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
	 * Create exception without adding the error code to the message.
	 */
	public static CException of(int code, String format, Object... args) {
		return new CException(StringUtil.format(format, args), code);
	}

	/**
	 * Create exception with general purpose error code.
	 */
	public static CException general(String format, Object... args) {
		return of(GENERAL_ERROR_CODE, format, args);
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static CException full(String message, int code) {
		return new CException(message + ": " + code, code);
	}

	protected CException(String message, int code) {
		super(message);
		this.code = code;
	}

	/**
	 * Convert to runtime exception.
	 */
	public CException.Runtime runtime() {
		return new CException.Runtime(this);
	}
}
