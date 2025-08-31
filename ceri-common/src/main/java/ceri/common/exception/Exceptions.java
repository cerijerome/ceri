package ceri.common.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
import ceri.common.text.Strings;
import ceri.common.util.BasicUtil;

/**
 * Exception support and generation convenience methods.
 */
public class Exceptions {

	private Exceptions() {}

	/**
	 * Exception filters.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Returns true if the throwable matches the class.
		 */
		public static <E extends Exception> Excepts.Predicate<E, Throwable>
			message(Excepts.Predicate<E, String> predicate) {
			return Predicates.nullNo(Predicates.testing(Throwable::getMessage, predicate));
		}

		private static <E extends Exception> boolean matchesMessage(Throwable t,
			Excepts.Predicate<E, String> predicate) throws E {
			if (t == null) return predicate == null;
			if (predicate == null) return true;
			String msg = t.getMessage();
			if (msg == null) return false;
			return predicate.test(msg);
		}
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E from(Functions.Function<String, E> fn, String format,
		Object... args) {
		return from(fn, null, format, args);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E from(Functions.Function<String, E> fn, Throwable cause,
		String format, Object... args) {
		String message = Strings.format(format, args);
		return Exceptions.initCause(fn.apply(message), cause);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException io(String format, Object... args) {
		return new IOException(Strings.format(format, args));
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static IOException io(Throwable cause, String format, Object... args) {
		return new IOException(Strings.format(format, args), cause);
	}

	/**
	 * Creates an exception with formatted message.
	 */
	public static NullPointerException nullPtr(String format, Object... args) {
		return from(NullPointerException::new, null, format, args);
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
		return from(IllegalArgumentException::new, cause, format, args);
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
		return from(IllegalStateException::new, cause, format, args);
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
		return from(UnsupportedOperationException::new, cause, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IndexOutOfBoundsException indexOob(String format, Object... args) {
		return from(IndexOutOfBoundsException::new, null, format, args);
	}

	/**
	 * Creates an exception with formatted message and cause.
	 */
	public static IndexOutOfBoundsException indexOob(String name, Number index, Number min,
		Number max) {
		return indexOob("%s out of bounds: %s [%s, %s]", name, index, BasicUtil.def(min, "?"),
			BasicUtil.def(max, "?"));
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
			if (!Strings.isBlank(message)) return message;
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
	public static <E extends Exception> boolean matches(Throwable t,
		Excepts.Predicate<E, String> msgTest) throws E {
		return matches(t, null, msgTest);
	}

	/**
	 * Returns true if the throwable matches the class and message predicate.
	 */
	public static <E extends Exception> boolean matches(Throwable t, Class<? extends Throwable> cls,
		Excepts.Predicate<E, String> msgTest) throws E {
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
