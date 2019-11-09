/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionRunnable;

/**
 * Basic utility methods.
 */
public class BasicUtil {
	private static final Pattern PACKAGE_REGEX =
		Pattern.compile("(?<![\\w$])([a-z$])[a-z0-9_$]+\\.");
	private static final int MICROS_IN_MILLIS = (int) TimeUnit.MILLISECONDS.toMicros(1);
	private static final int NANOS_IN_MICROS = (int) TimeUnit.MICROSECONDS.toNanos(1);
	private final static Map<Class<?>, Object> loadedClasses = new WeakHashMap<>();

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
	 * Gets the throwable message, or class name if no message.
	 */
	public static String message(Throwable t) {
		if (t == null) return null;
		return defaultValue(t.getMessage(), t.getClass().getSimpleName());
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
	 * Abbreviates package names, e.g. abc.def.MyClass -> a.d.MyClass
	 */
	public static String abbreviatePackages(String stackTrace) {
		if (stackTrace == null) return null;
		return PACKAGE_REGEX.matcher(stackTrace).replaceAll(m -> "$1.");
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
	 * Creates an exception with formatted message.
	 */
	public static IllegalArgumentException exceptionf(String format, Object... args) {
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
	 * Returns a value based on condition of null, true, false.
	 */
	public static <T> T conditional(Boolean condition, T trueValue, T falseValue) {
		return conditional(condition, trueValue, falseValue, null);
	}

	/**
	 * Returns a value based on condition of null, true, false.
	 */
	public static <T> T conditional(Boolean condition, T trueValue, T falseValue, T nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Make a system beep sound
	 */
	public static void beep() {
		// Disabled to avoid annoying unit test sound...
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
	 * Copy string from the clipboard
	 */
	public static String copyFromClipBoard() {
		return (String) shouldNotThrow(() -> Toolkit.getDefaultToolkit().getSystemClipboard()
			.getData(DataFlavor.stringFlavor));
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
		return find(cls, filter, null);
	}

	/**
	 * Finds the first enum matching the filter.
	 */
	public static <T extends Enum<T>> T find(Class<T> cls, Predicate<T> filter, T def) {
		return BasicUtil.enums(cls).stream().filter(filter).findFirst().orElse(def);
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

	public static boolean matches(Throwable t, Class<?> cls) {
		return matches(t, cls, null);
	}

	public static boolean matches(Throwable t, Predicate<String> msgTest) {
		return matches(t, null, msgTest);
	}

	public static boolean matches(Throwable t, Class<?> cls, Predicate<String> msgTest) {
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
		ConcurrentUtil.executeInterruptible(() -> Thread.sleep(delayMs));
	}

	/**
	 * Sleeps for given microseconds, or not if 0. Throws RuntimeInterruptedException if
	 * interrupted.
	 */
	public static void delayMicros(long delayMicros) {
		long ms = delayMicros / MICROS_IN_MILLIS;
		int ns = (int) ((delayMicros % MICROS_IN_MILLIS) * NANOS_IN_MICROS);
		if (ms == 0 && ns == 0) return;
		ConcurrentUtil.executeInterruptible(() -> Thread.sleep(ms, ns));
	}

	public static long microTime() {
		return System.nanoTime() / NANOS_IN_MICROS;
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
		return str == null || str.isBlank();
	}

	/**
	 * Execute runnable and convert non-runtime exceptions to runtime.
	 */
	public static void runtimeRun(ExceptionRunnable<?> runnable) {
		ExceptionAdapter.RUNTIME.run(runnable);
	}

	/**
	 * Execute callable and convert non-runtime exceptions to runtime.
	 */
	public static <T> T runtimeCall(Callable<T> callable) {
		return ExceptionAdapter.RUNTIME.call(callable);
	}

	public static void shouldNotThrow(ExceptionRunnable<Exception> runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	public static <T> T shouldNotThrow(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new IllegalStateException("Should not happen", e);
		}
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
