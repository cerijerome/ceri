package ceri.ffm.clib.ffm;

import java.io.IOException;
import ceri.common.except.ExceptionAdapter;
import ceri.common.except.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.text.Strings;
import ceri.ffm.core.LastError;

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
	 * Throws a detailed exception if last error code is set.
	 */
	public static void lastError() throws CException {
		int code = LastError.get();
		if (code != LastError.OK) throw full(code, "");
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
	 * Create exception with code prefix, enum, error message, and formatted message.
	 */
	public static CException full(int code, String format, Object... args) {
		return of(code, fullMessage(code, format, args));
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

	// support

	private static CException adapt(Throwable e) {
		String message = e.getMessage();
		if (message == null) message = "Error";
		return Exceptions.initCause(general(message), e);
	}

	private static String fullMessage(int code, String format, Object... args) {
		var b = new StringBuilder("[").append(code).append(']');
		int n = b.length();
		var errNo = CErrNo.from(code);
		var errMsg = LastError.message(code);
		var message = Strings.format(format, args);
		if (errNo.defined()) b.append(' ').append(errNo.name());
		if (Strings.nonEmpty(errMsg)) b.append(' ').append(errMsg);
		if (Strings.nonEmpty(message)) b.append(n < b.length() ? "; " : " ").append(message);
		return b.toString();
	}
}
