package ceri.common.util;

import ceri.common.function.Excepts.DoubleSupplier;
import ceri.common.function.Excepts.IntSupplier;
import ceri.common.function.Excepts.LongSupplier;
import ceri.common.function.Excepts.Supplier;

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
	public static <T> T unchecked(Object o) {
		return (T) o;
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <T> T def(T value, T def) {
		return value != null ? value : def;
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception, T> T def(T value, Supplier<E, ? extends T> defSupplier)
		throws E {
		return value != null ? value : defSupplier.get();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static int defInt(Integer value, int def) {
		return value != null ? value : def;
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> int defInt(Integer value, IntSupplier<E> defSupplier)
		throws E {
		return value != null ? value : defSupplier.getAsInt();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static long defLong(Long value, long def) {
		return value != null ? value : def;
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> long defLong(Long value, LongSupplier<E> defSupplier)
		throws E {
		return value != null ? value : defSupplier.getAsLong();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static double defDouble(Double value, double def) {
		return value != null ? value : def;
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> double defDouble(Double value,
		DoubleSupplier<E> defSupplier) throws E {
		return value != null ? value : defSupplier.getAsDouble();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception, T> T defGet(Supplier<E, ? extends T> supplier,
		Supplier<E, ? extends T> defSupplier) throws E {
		return def(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> int defGetInt(Supplier<E, ? extends Integer> supplier,
		IntSupplier<E> defSupplier) throws E {
		return defInt(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> long defGetLong(Supplier<E, ? extends Long> supplier,
		LongSupplier<E> defSupplier) throws E {
		return defLong(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> double defGetDouble(Supplier<E, ? extends Double> supplier,
		DoubleSupplier<E> defSupplier) throws E {
		return defDouble(supplier.get(), defSupplier);
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T ternaryGet(boolean condition,
		Supplier<E, ? extends T> trueSupplier) throws E {
		return ternaryGet(condition, trueSupplier, null);
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T ternaryGet(boolean condition,
		Supplier<E, ? extends T> trueSupplier, Supplier<E, ? extends T> falseSupplier) throws E {
		var supplier = ternary(condition, trueSupplier, falseSupplier);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Supplies a value based on condition, which may be null.
	 */
	public static <E extends Exception, T> T ternaryGet(Boolean condition,
		Supplier<E, ? extends T> trueSupplier, Supplier<E, ? extends T> falseSupplier,
		Supplier<E, ? extends T> nullSupplier) throws E {
		var supplier = ternary(condition, trueSupplier, falseSupplier, nullSupplier);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Returns a value based on condition.
	 */
	public static <T> T ternary(boolean condition, T trueValue, T falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
	 */
	public static <T> T ternary(Boolean condition, T trueValue, T falseValue, T nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static int ternaryInt(boolean condition, int trueValue, int falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
	 */
	public static int ternaryInt(Boolean condition, int trueValue, int falseValue, int nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static long ternaryLong(boolean condition, long trueValue, long falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
	 */
	public static long ternaryLong(Boolean condition, long trueValue, long falseValue,
		long nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition.
	 */
	public static double ternaryDouble(boolean condition, double trueValue, double falseValue) {
		return condition ? trueValue : falseValue;
	}

	/**
	 * Returns a value based on condition, which may be null.
	 */
	public static double ternaryDouble(Boolean condition, double trueValue, double falseValue,
		double nullValue) {
		if (condition == null) return nullValue;
		return condition ? trueValue : falseValue;
	}
}
