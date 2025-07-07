package ceri.jna.util;

import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import com.sun.jna.LastErrorException;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.Excepts;

/**
 * Utility to call C functions and check status codes.
 */
public class Caller<E extends Exception> {
	public final JnaArgs args;
	public final ToException<E> exceptionFn;

	/**
	 * Capture the error code or 0 if successful.
	 */
	public static int capture(Excepts.Runnable<? extends LastErrorException> runnable) {
		try {
			runnable.run();
			return 0;
		} catch (LastErrorException e) {
			return e.getErrorCode();
		}
	}

	public static interface ToException<E extends Exception> {
		E apply(int code, String message);
	}

	public static <E extends Exception> Caller<E> of(ToException<E> exceptionFn) {
		return of(JnaArgs.DEFAULT, exceptionFn);
	}

	public static <E extends Exception> Caller<E> of(JnaArgs args, ToException<E> exceptionFn) {
		return new Caller<>(args, exceptionFn);
	}

	private Caller(JnaArgs args, ToException<E> exceptionFn) {
		this.args = args;
		this.exceptionFn = exceptionFn;
	}

	/**
	 * Creates a failure message for given method and arguments.
	 */
	public String failMessage(String name, Object... args) {
		return name + "(" + argString(args) + ") failed";
	}

	/**
	 * Creates a string representation of given method and arguments.
	 */
	public String functionString(String name, Object... args) {
		return name + "(" + argString(args) + ")";
	}

	/**
	 * Creates a comma-separated string from given arguments.
	 */
	public String argString(Object... args) {
		return this.args.args(args);
	}

	/**
	 * Call void library function. Throws an exception on LastErrorException.
	 */
	public void call(Excepts.Runnable<E> fn, String name, Object... args) throws E {
		call(fn, messageFn(name, args));
	}

	/**
	 * Call void library function. Throws an exception on LastErrorException.
	 */
	public void call(Excepts.Runnable<E> fn, Supplier<String> msgFn) throws E {
		try {
			fn.run();
		} catch (LastErrorException e) {
			throw lastError(e, msgFn.get());
		}
	}

	/**
	 * Call library function and return int result. Throws an exception on LastErrorException.
	 */
	public int callInt(Excepts.IntSupplier<E> fn, String name, Object... args) throws E {
		return callInt(fn, messageFn(name, args));
	}

	/**
	 * Call library function and return int result. Throws an exception on LastErrorException.
	 */
	public int callInt(Excepts.IntSupplier<E> fn, Supplier<String> msgFn) throws E {
		try {
			return fn.getAsInt();
		} catch (LastErrorException e) {
			throw lastError(e, msgFn.get());
		}
	}

	/**
	 * Call library function and return type result. Throws an exception on LastErrorException.
	 */
	public <R> R callType(Excepts.Supplier<E, R> fn, String name, Object... args) throws E {
		return callType(fn, messageFn(name, args));
	}

	/**
	 * Call library function and return type result. Throws an exception on LastErrorException.
	 */
	public <R> R callType(Excepts.Supplier<E, R> fn, Supplier<String> msgFn) throws E {
		try {
			return fn.get();
		} catch (LastErrorException e) {
			throw lastError(e, msgFn.get());
		}
	}

	/**
	 * Verify result. Throws an exception on non-zero status code.
	 */
	public void verify(int result, String name, Object... args) throws E {
		verifyInt(result, r -> r == 0, name, args);
	}

	/**
	 * Verify and return int result. Throws an exception on negative status code.
	 */
	public int verifyInt(int result, String name, Object... args) throws E {
		return verifyInt(result, r -> r >= 0, name, args);
	}

	/**
	 * Verify and return int result. Throws an exception on failed status code verification.
	 */
	public int verifyInt(int result, IntPredicate verifyFn, String name, Object... args) throws E {
		if (!verifyFn.test(result)) throw exceptionFn.apply(result, failMessage(name, args));
		return result;
	}

	/**
	 * Call library function, and verify result. Throws an exception on non-zero status code, or
	 * LastErrorException.
	 */
	public void verify(Excepts.IntSupplier<E> fn, String name, Object... args) throws E {
		verify(fn, messageFn(name, args));
	}

	/**
	 * Call library function, and verify result. Throws an exception on non-zero status code, or
	 * LastErrorException.
	 */
	public void verify(Excepts.IntSupplier<E> fn, Supplier<String> msgFn) throws E {
		verifyInt(fn, r -> r == 0, msgFn);
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on negative status
	 * code, or LastErrorException.
	 */
	public int verifyInt(Excepts.IntSupplier<E> fn, String name, Object... args) throws E {
		return verifyInt(fn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on negative status
	 * code, or LastErrorException.
	 */
	public int verifyInt(Excepts.IntSupplier<E> fn, Supplier<String> msgFn) throws E {
		return verifyInt(fn, r -> r >= 0, msgFn);
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on failed status
	 * code verification, or LastErrorException.
	 */
	public int verifyInt(Excepts.IntSupplier<E> fn, IntPredicate verifyFn, String name,
		Object... args) throws E {
		return verifyInt(fn, verifyFn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on failed status
	 * code verification, or LastErrorException.
	 */
	public int verifyInt(Excepts.IntSupplier<E> fn, IntPredicate verifyFn, Supplier<String> msgFn)
		throws E {
		int result = callInt(fn, msgFn);
		if (!verifyFn.test(result)) throw exceptionFn.apply(result, failMessage(msgFn));
		return result;
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on null result with
	 * given error code, and on LastErrorException.
	 */
	public <R> R verifyType(Excepts.Supplier<E, R> fn, int errorCode, String name, Object... args)
		throws E {
		return verifyType(fn, errorCode, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on null result with
	 * given error code, and on LastErrorException.
	 */
	public <R> R verifyType(Excepts.Supplier<E, R> fn, int errorCode, Supplier<String> msgFn)
		throws E {
		return verifyType(fn, r -> r != null ? 0 : errorCode, msgFn);
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-zero status
	 * verification, or LastErrorException.
	 */
	public <R> R verifyType(Excepts.Supplier<E, R> fn, ToIntFunction<R> statusFn, String name,
		Object... args) throws E {
		return verifyType(fn, statusFn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-zero status
	 * verification, or LastErrorException.
	 */
	public <R> R verifyType(Excepts.Supplier<E, R> fn, ToIntFunction<R> statusFn,
		Supplier<String> msgFn) throws E {
		R result = callType(fn, msgFn);
		int status = statusFn.applyAsInt(result);
		if (status != 0) throw exceptionFn.apply(status, failMessage(msgFn));
		return result;
	}

	private E lastError(LastErrorException e, String message) {
		int code = e.getErrorCode();
		String lastErrorMsg = JnaUtil.message(e);
		if (!lastErrorMsg.isEmpty()) message = lastErrorMsg + ": " + message;
		return ExceptionUtil.initCause(exceptionFn.apply(code, message), e);
	}

	private Supplier<String> messageFn(String name, Object... args) {
		return () -> functionString(name, args);
	}

	private static String failMessage(Supplier<String> msgFn) {
		return msgFn.get() + " failed";
	}
}
