/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.collection.StreamUtil;
import ceri.common.concurrent.RuntimeInterruptedException;

/**
 * Basic utility methods.
 */
public class BasicUtil {
	private static Map<Class<?>, Object> loadedClasses = new WeakHashMap<>();

	private BasicUtil() {}

	/**
	 * Stops the warning for an unused parameter. Use only when absolutely necessary.
	 */
	public static void unused(@SuppressWarnings("unused") Object... o) {}

	/**
	 * Are you really sure you need to call this? If you're not sure why you need to call this
	 * method you may be hiding a coding error. Performs an unchecked cast from an object to the
	 * given type, preventing a warning. Sometimes necessary for collections, etc. Will not prevent
	 * a runtime cast exception.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object o) {
		return (T) o;
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
	 * Creates an exception with formatted message.
	 */
	public static IllegalArgumentException exceptionf(String format,
		Object... args) {
		return exceptionf(IllegalArgumentException::new, format, args);
	}
	
	/**
	 * Creates an exception with formatted message.
	 */
	public static <E extends Exception> E exceptionf(Function<String, E> fn, String format,
		Object... args) {
		String message = String.format(format, args);
		return fn.apply(message);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <T> T defaultValue(T value, T def) {
		return value != null ? value : def;
	}

	/**
	 * Make a system beep sound
	 */
	public static void beep() {
		// Toolkit.getDefaultToolkit().beep();
	}

	/**
	 * Copy string to the clipboard
	 */
	public static void copyToClipBoard(String s) {
		StringSelection selection = new StringSelection(s);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
	}

	/**
	 * Convenience method that calls Enum.valueOf and returns null if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value) {
		return valueOf(cls, value, null);
	}

	/**
	 * Convenience method that calls Enum.valueOf and returns default value if no match.
	 */
	public static <T extends Enum<T>> T valueOf(Class<T> cls, String value, T def) {
		if (value == null || cls == null) return def;
		try {
			return Enum.valueOf(cls, value);
		} catch (IllegalArgumentException e) {
			return def;
		}
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter) {
		return StreamUtil.first(BasicUtil.enums(cls).stream().filter(filter));
	}

	/**
	 * Convenience method that returns all enum constants as a list.
	 */
	public static <T extends Enum<T>> List<T> enums(Class<T> cls) {
		return Arrays.asList(cls.getEnumConstants());
	}

	/**
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> forEach(final Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Throws the exception if it is assignable to given type.
	 */
	public static <E extends Exception> void throwIfType(Class<E> exceptionCls, Throwable t)
		throws E {
		if (exceptionCls.isInstance(t)) throw BasicUtil.<E>uncheckedCast(t);
	}

	/**
	 * Casts object to given type or returns null if not compatible.
	 */
	public static <T> T castOrNull(Class<T> cls, Object obj) {
		if (!cls.isInstance(obj)) return null;
		return cls.cast(obj);
	}

	/**
	 * Sleeps for given milliseconds, or not if 0. Throws RuntimeInterruptedException if
	 * interrupted.
	 */
	public static void delay(long delayMs) {
		if (delayMs == 0) return;
		try {
			Thread.sleep(delayMs);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Gets the stack trace as a string.
	 */
	public static String stackTrace(Throwable t) {
		StringWriter w = new StringWriter();
		t.printStackTrace(new PrintWriter(w));
		return w.toString();
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
	 * Checks if the given map is null or empty.
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * Checks if the given collection is null or empty.
	 */
	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Checks if the given object array is null or empty.
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Checks if the given string is null or empty or contains only whitespace.
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0 || str.trim().length() == 0;
	}

	/**
	 * Makes sure a Class<?> is loaded. Use when static initialization is required but only the
	 * Class<?> is referenced.
	 */
	public static <T> Class<T> forceInit(Class<T> cls) {
		if (loadedClasses.containsKey(cls)) return cls;
		load(cls.getName(), cls.getClassLoader());
		loadedClasses.put(cls, null);
		return cls;
	}

	static void load(String className, ClassLoader classLoader) {
		try {
			Class.forName(className, true, classLoader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find Class<?> " + className, e);
		}
	}

}
