package ceri.common.util;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import ceri.common.function.Excepts;

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
	 * Returns true if all values are null, or the varargs array is null.
	 */
	@SafeVarargs
	public static <T> boolean allNull(T... args) {
		if (args == null) return true; 
		for (T arg : args)
			if (arg != null) return false;
		return true;
	}

	/**
	 * Returns true if any values are null, or the varargs array is null.
	 */
	@SafeVarargs
	public static <T> boolean anyNull(T... args) {
		if (args == null) return true; 
		for (T arg : args)
			if (arg == null) return true;
		return false;
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
	public static <E extends Exception, T> T def(T value,
		Excepts.Supplier<E, ? extends T> defSupplier) throws E {
		if (value != null) return value;
		return defSupplier == null ? null : defSupplier.get();
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
	public static int defInt(Integer value, IntSupplier defSupplier) {
		return value != null ? value : defSupplier.getAsInt();
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> int defInt(Integer value,
		Excepts.IntSupplier<E> defSupplier) throws E {
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
	public static long defLong(Long value, LongSupplier defSupplier) {
		return value != null ? value : defSupplier.getAsLong();
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> long defLong(Long value,
		Excepts.LongSupplier<E> defSupplier) throws E {
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
	public static double defDouble(Double value, DoubleSupplier defSupplier) {
		return value != null ? value : defSupplier.getAsDouble();
	}

	/**
	 * Returns default supplied value if main value is null.
	 */
	public static <E extends Exception> double defDouble(Double value,
		Excepts.DoubleSupplier<E> defSupplier) throws E {
		return value != null ? value : defSupplier.getAsDouble();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception, T> T defGet(Excepts.Supplier<E, ? extends T> supplier,
		Excepts.Supplier<E, ? extends T> defSupplier) throws E {
		return def(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static int defGetInt(Supplier<? extends Integer> supplier, IntSupplier defSupplier) {
		return defInt(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> int defGetInt(
		Excepts.Supplier<E, ? extends Integer> supplier, Excepts.IntSupplier<E> defSupplier)
		throws E {
		return defInt(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static long defGetLong(Supplier<? extends Long> supplier, LongSupplier defSupplier) {
		return defLong(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> long defGetLong(
		Excepts.Supplier<E, ? extends Long> supplier, Excepts.LongSupplier<E> defSupplier)
		throws E {
		return defLong(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static double defGetDouble(Supplier<? extends Double> supplier,
		DoubleSupplier defSupplier) {
		return defDouble(supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> double defGetDouble(
		Excepts.Supplier<E, ? extends Double> supplier, Excepts.DoubleSupplier<E> defSupplier)
		throws E {
		return defDouble(supplier.get(), defSupplier);
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T ternaryGet(boolean condition,
		Excepts.Supplier<E, ? extends T> trueSupplier) throws E {
		return ternaryGet(condition, trueSupplier, null);
	}

	/**
	 * Supplies a value based on condition.
	 */
	public static <E extends Exception, T> T ternaryGet(boolean condition,
		Excepts.Supplier<E, ? extends T> trueSupplier,
		Excepts.Supplier<E, ? extends T> falseSupplier) throws E {
		var supplier = ternary(condition, trueSupplier, falseSupplier);
		return supplier == null ? null : supplier.get();
	}

	/**
	 * Supplies a value based on condition, which may be null.
	 */
	public static <E extends Exception, T> T ternaryGet(Boolean condition,
		Excepts.Supplier<E, ? extends T> trueSupplier,
		Excepts.Supplier<E, ? extends T> falseSupplier,
		Excepts.Supplier<E, ? extends T> nullSupplier) throws E {
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
