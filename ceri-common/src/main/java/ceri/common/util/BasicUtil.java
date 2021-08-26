/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;

/**
 * Basic utility methods.
 */
public class BasicUtil {

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
	 * Executes a try-with-resources from type, action function, and close function.
	 */
	public static <E extends Exception, T> void tryWithResources(T t,
		ExceptionConsumer<E, T> actionFn, ExceptionConsumer<E, T> closeFn) throws E {
		try {
			if (t != null) actionFn.accept(t);
		} finally {
			if (t != null) closeFn.accept(t);
		}
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
	 * Returns a value based on condition.
	 */
	public static int conditionalInt(boolean condition, int trueValue, int falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static long conditionalLong(boolean condition, long trueValue, long falseValue) {
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
	 * Makes an iterator compatible with a for-each loop.
	 */
	public static <T> Iterable<T> forEach(final Iterator<T> iterator) {
		return () -> iterator;
	}

	/**
	 * Casts object to given type or returns null if not compatible.
	 */
	public static <T> T castOrNull(Class<T> cls, Object obj) {
		if (!cls.isInstance(obj)) return null;
		return cls.cast(obj);
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
	 * Execute runnable and convert non-runtime exceptions to runtime.
	 */
	public static void runtimeRun(ExceptionRunnable<?> runnable) {
		ExceptionAdapter.RUNTIME.run(runnable);
	}

	/**
	 * Execute callable and convert non-runtime exceptions to runtime.
	 */
	public static <T> T runtimeCall(ExceptionSupplier<?, T> callable) {
		return ExceptionAdapter.RUNTIME.get(callable);
	}

}
