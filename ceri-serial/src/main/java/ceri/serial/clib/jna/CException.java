package ceri.serial.clib.jna;

import java.io.IOException;
import java.util.function.IntConsumer;
import com.sun.jna.LastErrorException;
import ceri.common.function.ExceptionRunnable;
import ceri.common.text.StringUtil;

public class CException extends IOException {
	private static final long serialVersionUID = -7274377830953987618L;
	private static final int GENERAL_ERROR_CODE = -1;
	public final int code;

	public static void intercept(ExceptionRunnable<CException> runnable, IntConsumer consumer)
		throws CException {
		try {
			runnable.run();
			if (consumer != null) consumer.accept(0);
		} catch (CException e) {
			if (consumer != null) consumer.accept(e.code);
			throw e;
		}
	}

	public static int capture(ExceptionRunnable<CException> runnable) {
		try {
			runnable.run();
			return 0;
		} catch (CException e) {
			return e.code;
		}
	}

	public static int verify(int result, String name) throws CException {
		if (result >= 0) return result;
		throw full(result, name + " failed");
	}

	/**
	 * Create exception without adding the error code to the message.
	 */
	public static CException of(int code, String format, Object... args) {
		return new CException(code, StringUtil.format(format, args));
	}

	/**
	 * Create exception adding the error code to the message.
	 */
	public static CException full(int code, String format, Object... args) {
		return new CException(code, StringUtil.format(format, args) + ": " + code);
	}

	/**
	 * Create exception with general purpose error code.
	 */
	public static CException general(String format, Object... args) {
		return new CException(GENERAL_ERROR_CODE, StringUtil.format(format, args));
	}

	/**
	 * Create exception from errno and message.
	 */
	public static CException from(LastErrorException e, String format, Object... args) {
		return new CException(e.getErrorCode(),
			e.getMessage() + ": " + StringUtil.format(format, args));
	}

	public CException(int code) {
		this.code = code;
	}

	public CException(int code, String message) {
		super(message);
		this.code = code;
	}

	public CException(int code, String message, Throwable t) {
		super(message, t);
		this.code = code;
	}

	public CRuntimeException runtime() {
		return new CRuntimeException(this);
	}

}
