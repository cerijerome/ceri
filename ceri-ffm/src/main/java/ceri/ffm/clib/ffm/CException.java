package ceri.ffm.clib.ffm;

import java.io.IOException;
import ceri.common.except.ExceptionAdapter;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.text.Strings;

@SuppressWarnings("serial")
public class CException extends IOException {
	public static final ExceptionAdapter<CException> ADAPTER =
		ExceptionAdapter.of(CException.class, CException::adapt);
	public static final int GENERAL_ERROR_CODE = CErrNo.UNDEFINED.code;
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
	public static <E extends CException> void intercept(Excepts.Runnable<E> runnable,
		Functions.IntConsumer consumer) throws E {
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
	public static int capture(Excepts.Runnable<? extends CException> runnable) {
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
		return new CException(code, Strings.format(format, args));
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
		var message = Strings.format(format, args);
		var errNo = CErrNo.from(code);
		if (errNo.defined() && !message.startsWith(errNo.name())) message = errNo + " " + message;
		return new CException(code, "[" + code + "] " + message);
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
		return Exceptions.initCause(general(message), e);
	}
}
