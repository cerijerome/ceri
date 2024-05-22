package ceri.jna.clib.jna;

import java.io.IOException;
import java.util.function.IntConsumer;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionRunnable;
import ceri.common.text.StringUtil;
import ceri.jna.clib.ErrNo;

@SuppressWarnings("serial")
public class CException extends IOException {
	public static final ExceptionAdapter<CException> ADAPTER =
		ExceptionAdapter.of(CException.class, CException::adapt);
	public static final int GENERAL_ERROR_CODE = -1;
	public final int code;

	public static class Runtime extends RuntimeException {
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
	 * Create exception with formatted message.
	 */
	public static CException of(int code, String format, Object... args) {
		return new CException(code, StringUtil.format(format, args));
	}

	/**
	 * Create exception with general purpose error code and formatted message.
	 */
	public static CException general(String format, Object... args) {
		return of(GENERAL_ERROR_CODE, format, args);
	}

	/**
	 * Create exception with code prefix and formatted message.
	 */
	public static CException full(int code, String format, Object... args) {
		var message = StringUtil.format(format, args);
		var errNo = ErrNo.from(code);
		return new CException(code,
			"[" + code + "] " + (errNo.defined() ? errNo + " " : "") + message);
	}

	protected CException(int code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Convert to runtime exception.
	 */
	public CException.Runtime runtime() {
		return new CException.Runtime(this);
	}

	private static CException adapt(Throwable e) {
		String message = e.getMessage();
		if (message == null) message = "Error";
		return ExceptionUtil.initCause(general(message), e);
	}
}
