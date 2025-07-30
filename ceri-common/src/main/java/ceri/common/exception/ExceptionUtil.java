package ceri.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Predicate;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Exception support and convenience methods.
 */
public class ExceptionUtil {
	private ExceptionUtil() {}

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
		if (exceptionCls.isInstance(t)) throw BasicUtil.<E>unchecked(t);
	}
}
