package ceri.serial.clib.jna;

import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import com.sun.jna.LastErrorException;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.ObjIntFunction;
import ceri.serial.jna.JnaArgs;

/**
 * Utility to call functions and check status codes.
 */
public class CCaller<E extends Exception> {
	public final JnaArgs args;
	public final ObjIntFunction<String, E> exceptionFn;

	/**
	 * Capture the error code or 0 if successful.
	 */
	public static int capture(ExceptionRunnable<? extends LastErrorException> runnable) {
		try {
			runnable.run();
			return 0;
		} catch (LastErrorException e) {
			return e.getErrorCode();
		}
	}

	public static CCaller<CException> of() {
		return of(CException::full);
	}

	public static <E extends Exception> CCaller<E> of(ObjIntFunction<String, E> exceptionFn) {
		return of(JnaArgs.DEFAULT, exceptionFn);
	}

	public static <E extends Exception> CCaller<E> of(JnaArgs args,
		ObjIntFunction<String, E> exceptionFn) {
		return new CCaller<>(args, exceptionFn);
	}

	private CCaller(JnaArgs args, ObjIntFunction<String, E> exceptionFn) {
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
	public void call(ExceptionRunnable<E> fn, String name, Object... args) throws E {
		call(fn, messageFn(name, args));
	}

	/**
	 * Call void library function. Throws an exception on LastErrorException.
	 */
	public void call(ExceptionRunnable<E> fn, Supplier<String> msgFn) throws E {
		try {
			fn.run();
		} catch (LastErrorException e) {
			throw lastError(e, msgFn.get());
		}
	}

	/**
	 * Call library function and return int result. Throws an exception on LastErrorException.
	 */
	public int callInt(ExceptionIntSupplier<E> fn, String name, Object... args) throws E {
		return callInt(fn, messageFn(name, args));
	}

	/**
	 * Call library function and return int result. Throws an exception on LastErrorException.
	 */
	public int callInt(ExceptionIntSupplier<E> fn, Supplier<String> msgFn) throws E {
		try {
			return fn.getAsInt();
		} catch (LastErrorException e) {
			throw lastError(e, msgFn.get());
		}
	}

	/**
	 * Call library function and return type result. Throws an exception on LastErrorException.
	 */
	public <R> R callType(ExceptionSupplier<E, R> fn, String name, Object... args) throws E {
		return callType(fn, messageFn(name, args));
	}

	/**
	 * Call library function and return type result. Throws an exception on LastErrorException.
	 */
	public <R> R callType(ExceptionSupplier<E, R> fn, Supplier<String> msgFn) throws E {
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
		if (!verifyFn.test(result)) throw exceptionFn.apply(failMessage(name, args), result);
		return result;
	}

	/**
	 * Call library function, and verify result. Throws an exception on non-zero status code, or
	 * LastErrorException.
	 */
	public void verify(ExceptionIntSupplier<E> fn, String name, Object... args) throws E {
		verify(fn, messageFn(name, args));
	}

	/**
	 * Call library function, and verify result. Throws an exception on non-zero status code, or
	 * LastErrorException.
	 */
	public void verify(ExceptionIntSupplier<E> fn, Supplier<String> msgFn) throws E {
		verifyInt(fn, r -> r == 0, msgFn);
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on negative status
	 * code, or LastErrorException.
	 */
	public int verifyInt(ExceptionIntSupplier<E> fn, String name, Object... args) throws E {
		return verifyInt(fn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on negative status
	 * code, or LastErrorException.
	 */
	public int verifyInt(ExceptionIntSupplier<E> fn, Supplier<String> msgFn) throws E {
		return verifyInt(fn, r -> r >= 0, msgFn);
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on failed status
	 * code verification, or LastErrorException.
	 */
	public int verifyInt(ExceptionIntSupplier<E> fn, IntPredicate verifyFn, String name,
		Object... args) throws E {
		return verifyInt(fn, verifyFn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return int result. Throws an exception on failed status
	 * code verification, or LastErrorException.
	 */
	public int verifyInt(ExceptionIntSupplier<E> fn, IntPredicate verifyFn, Supplier<String> msgFn)
		throws E {
		int result = callInt(fn, msgFn);
		if (!verifyFn.test(result)) throw exceptionFn.apply(failMessage(msgFn), result);
		return result;
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-null result with
	 * given error code, and on LastErrorException.
	 */
	public <R> R verifyType(ExceptionSupplier<E, R> fn, int errorCode, String name, Object... args)
		throws E {
		return verifyType(fn, errorCode, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-null result with
	 * given error code, and on LastErrorException.
	 */
	public <R> R verifyType(ExceptionSupplier<E, R> fn, int errorCode, Supplier<String> msgFn)
		throws E {
		return verifyType(fn, r -> r != null ? 0 : errorCode, msgFn);
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-zero status
	 * verification, or LastErrorException.
	 */
	public <R> R verifyType(ExceptionSupplier<E, R> fn, ToIntFunction<R> statusFn, String name,
		Object... args) throws E {
		return verifyType(fn, statusFn, messageFn(name, args));
	}

	/**
	 * Call library function, verify, and return result. Throws an exception on non-zero status
	 * verification, or LastErrorException.
	 */
	public <R> R verifyType(ExceptionSupplier<E, R> fn, ToIntFunction<R> statusFn,
		Supplier<String> msgFn) throws E {
		R result = callType(fn, msgFn);
		int status = statusFn.applyAsInt(result);
		if (status != 0) throw exceptionFn.apply(failMessage(msgFn), status);
		return result;
	}

	private E lastError(LastErrorException e, String message) {
		return ExceptionUtil
			.initCause(exceptionFn.apply(e.getMessage() + ": " + message, e.getErrorCode()), e);
	}

	private Supplier<String> messageFn(String name, Object... args) {
		return () -> functionString(name, args);
	}

	private String failMessage(Supplier<String> msgFn) {
		return msgFn.get() + " failed";
	}
}
