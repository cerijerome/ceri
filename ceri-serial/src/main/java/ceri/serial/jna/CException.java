package ceri.serial.jna;

import java.io.IOException;
import java.util.function.IntConsumer;
import ceri.common.function.ExceptionRunnable;

public class CException extends IOException {
	private static final long serialVersionUID = -7274377830953987618L;
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
		throw fullMessage(name + " failed", result);
	}

	public static CException fullMessage(String message, int code) {
		return new CException(message + ": " + code, code);
	}

	public CException(int code) {
		this.code = code;
	}

	public CException(String message, int code) {
		super(message);
		this.code = code;
	}

	public CException(String message, int code, Throwable t) {
		super(message, t);
		this.code = code;
	}

}
