/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * Basic utility methods.
 */
public class BasicUtil {
	private static Map<Class<?>, Object> loadedClasses = new WeakHashMap<>();

	private BasicUtil() {
	}

	/**
	 * Stops the warning for an unused parameter.
	 * Use only when absolutely necessary.
	 */
	public static void unused(@SuppressWarnings("unused") Object... o) {
	}

	/**
	 * Are you really sure you need to call this? If you're not sure why you
	 * need to call this method you may be hiding a coding error.
	 * Performs an unchecked cast from an object to the given type,
	 * preventing a warning. Sometimes necessary for collections, etc.
	 * Will not prevent a runtime cast exception.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T uncheckedCast(Object o) {
		return (T)o;
	}

	/**
	 * Make a beep sound
	 */
	public static void beep() {
		java.awt.Toolkit.getDefaultToolkit().beep();
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
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> forEach(final Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
	
	/**
	 * Casts object to given type or returns null if not compatible.
	 */
	public static <T> T castOrNull(Class<T> cls, Object obj) {
		if (!cls.isInstance(obj)) return null;
		return cls.cast(obj);
	}

	/**
	 * Sleeps for given milliseconds, or not if 0.
	 * Throws RuntimeInterruptedException if interrupted.
	 */
	public static void delay(int delayMs) {
		try {
			if (delayMs > 0) Thread.sleep(delayMs);
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
	 * Attaches a throwable cause to an exception without losing type.
	 * Use as: throw initCause(new MyException(...), cause);
	 */
	public static <E extends Exception> E initCause(E e, Throwable cause) {
		e.initCause(cause);
		return e;
	}
	
	/**
	 * Picks the first one if not null, otherwise the second one.
	 */
	public static <T> T chooseNonNull(T lhs, T rhs) {
		return lhs != null ? lhs : rhs;
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
	 * Makes sure a Class<?> is loaded. Use when static initialization
	 * is required but only the Class<?> is referenced.
	 */
	public static <T> Class<T> forceInit(Class<T> cls) {
		if (loadedClasses.containsKey(cls)) return cls;
		try {
			Class.forName(cls.getName(), true, cls.getClassLoader());
			loadedClasses.put(cls, null);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find Class<?> " + cls, e);
		}
		return cls;
	}

}
