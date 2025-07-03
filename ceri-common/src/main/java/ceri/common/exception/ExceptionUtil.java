package ceri.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.function.ExceptionRunnable;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Exception support and convenience methods.
 */
public class ExceptionUtil {

	private ExceptionUtil() {}

	/**
	 * A stub that can be used for shorter generic type declarations.
	 */
	@SuppressWarnings("serial")
	public static class Rte extends RuntimeException {}

	/**
	 * Call to generate an IllegalStateException. Use for lambdas that shouldn't be called.
	 */
	public static void doNotCall(Object... args) {
		BasicUtil.unused(args);
		throw new IllegalStateException();
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IllegalArgumentException illegalArg(String format, Object... args) {
		return illegalArg((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IllegalArgumentException illegalArg(Throwable cause, String format,
		Object... args) {
		return exceptionf(IllegalArgumentException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IllegalStateException illegalState(String format, Object... args) {
		return illegalState((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IllegalStateException illegalState(Throwable cause, String format,
		Object... args) {
		return exceptionf(IllegalStateException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static UnsupportedOperationException unsupportedOp(String format, Object... args) {
		return unsupportedOp((Throwable) null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static UnsupportedOperationException unsupportedOp(Throwable cause, String format,
		Object... args) {
		return exceptionf(UnsupportedOperationException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E exceptionf(Function<String, E> fn, String format,
		Object... args) {
		return exceptionf(fn, null, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E exceptionf(Function<String, E> fn, Throwable cause,
		String format, Object... args) {
		String message = StringUtil.format(format, args);
		return initCause(fn.apply(message), cause);
	}

	/**
	 * Use to avoid declaring a thrown checked exception.
	 */
	public static <E extends Throwable> void throwUnchecked(Throwable e) throws E {
		throw BasicUtil.<E>uncheckedCast(e);
	}

	/**
	 * Gets the root cause of a throwable.
	 */
	public static Throwable rootCause(Throwable t) {
		if (t == null) return null;
		while (true) {
			Throwable cause = t.getCause();
			if (cause == null) return t;
			t = cause;
		}
	}

	/**
	 * Gets the throwable message, or class name if no message.
	 */
	public static String message(Throwable t) {
		Throwable t0 = t;
		while (t != null) {
			String message = t.getMessage();
			if (!StringUtil.blank(message)) return message;
			t = t.getCause();
		}
		return t0 == null ? null : t0.getClass().getSimpleName();
	}

	/**
	 * Gets the stack trace as a string.
	 */
	public static String stackTrace(Throwable t) {
		if (t == null) return null;
		StringWriter w = new StringWriter();
		t.printStackTrace(new PrintWriter(w));
		return w.toString();
	}

	/**
	 * Gets the first stack trace element of the throwable.
	 */
	public static StackTraceElement firstStackElement(Throwable t) {
		if (t == null) return null;
		StackTraceElement[] elements = t.getStackTrace();
		return elements != null && elements.length > 0 ? elements[0] : null;
	}

	/**
	 * Limits the stack trace elements of a throwable. Returns true if truncated.
	 */
	public static boolean limitStackTrace(Throwable t, int max) {
		if (t == null) return false;
		StackTraceElement[] elements = t.getStackTrace();
		if (elements.length <= max) return false;
		elements = Arrays.copyOf(elements, max);
		t.setStackTrace(elements);
		return true;
	}

	/**
	 * Attaches a throwable cause to an exception without losing type. If the given cause is null it
	 * will not be initialized. Use as: throw initCause(new MyException(...), cause);
	 */
	public static <E extends Exception> E initCause(E e, Throwable cause) {
		if (cause != null) e.initCause(cause);
		return e;
	}

	/**
	 * Returns true if the throwable matches the class.
	 */
	public static boolean matches(Throwable t, Class<? extends Throwable> cls) {
		return matches(t, cls, null);
	}

	/**
	 * Returns true if the throwable matches the message predicate.
	 */
	public static boolean matches(Throwable t, Predicate<String> msgTest) {
		return matches(t, null, msgTest);
	}

	/**
	 * Returns true if the throwable matches the class and message predicate.
	 */
	public static boolean matches(Throwable t, Class<? extends Throwable> cls,
		Predicate<String> msgTest) {
		if (t == null) return false;
		if (cls != null && !cls.isInstance(t)) return false;
		if (msgTest == null) return true;
		String msg = t.getMessage();
		if (msg == null) return false;
		return msgTest.test(msg);
	}

	/**
	 * Throws the exception if it is assignable to given type.
	 */
	public static <E extends Exception> void throwIfType(Class<E> exceptionCls, Throwable t)
		throws E {
		if (exceptionCls.isInstance(t)) throw BasicUtil.<E>uncheckedCast(t);
	}

	/**
	 * Execute the runnable; any unexpected error is wrapped as runtime.
	 */
	public static void shouldNotThrow(ExceptionRunnable<? extends Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	/**
	 * Execute the callable and return result; any unexpected error is wrapped as runtime.
	 */
	public static <T> T shouldNotThrow(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}
}
