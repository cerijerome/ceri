/*
 * Created on Jul 31, 2005
 */
package ceri.common.util;

import ceri.common.exception.ExceptionAdapter;
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
	 * Returns default value if main value is null.
	 */
	public static <T> T defaultValue(T value, T def) {
		return value != null ? value : def;
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception, T> T defaultValue(T value,
		ExceptionSupplier<E, ? extends T> defSupplier) throws E {
		return value != null ? value : defSupplier.get();
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T conditionalGet(boolean condition,
		ExceptionSupplier<E, ? extends T> trueSupplier) throws E {
		return conditionalGet(condition, trueSupplier, null);
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T conditionalGet(boolean condition,
		ExceptionSupplier<E, ? extends T> trueSupplier,
		ExceptionSupplier<E, ? extends T> falseSupplier) throws E {
		var supplier = conditional(condition, trueSupplier, falseSupplier);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Supplies a value based on condition, which may be null.
	 */
	public static <E extends Exception, T> T conditionalGet(Boolean condition,
		ExceptionSupplier<E, ? extends T> trueSupplier,
		ExceptionSupplier<E, ? extends T> falseSupplier,
		ExceptionSupplier<E, ? extends T> nullSupplier) throws E {
		var supplier = conditional(condition, trueSupplier, falseSupplier, nullSupplier);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Returns a value based on condition.
	 */
	public static <T> T conditional(boolean condition, T trueValue, T falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
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
	 * Returns a value based on condition, which may be null.
	 */
	public static int conditionalInt(Boolean condition, int trueValue, int falseValue,
		int nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static long conditionalLong(boolean condition, long trueValue, long falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
	 */
	public static long conditionalLong(Boolean condition, long trueValue, long falseValue,
		long nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
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
