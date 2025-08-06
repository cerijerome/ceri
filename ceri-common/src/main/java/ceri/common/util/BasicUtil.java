package ceri.common.util;

import java.util.Arrays;
import java.util.Objects;
import ceri.common.exception.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.reflect.Reflect;

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
	 * Verifies all arguments and the argument array are non-null.
	 */
	@SafeVarargs
	public static <T> void requireNonNull(T... args) {
		Objects.requireNonNull(args);
		for (var arg : args)
			Objects.requireNonNull(arg);
	}

	/**
	 * Throws an exception if the value is equal to one of the disallowed values.
	 */
	@SafeVarargs
	public static <T> T requireNot(T t, T... disallowed) {
		for (var disallow : disallowed) {
			if (t == null && disallow == null) throw Exceptions.nullPtr("Cannot be null");
			else if (Objects.equals(t, disallow))
				throw Exceptions.illegalArg("%s cannot be one of %s: %s", Reflect.className(t),
					Arrays.toString(disallowed), t);
		}
		return t;
	}

	/**
	 * Throws an exception if the value is not equal to one of the allowed values.
	 */
	@SafeVarargs
	public static <T> T requireAny(T t, T... allowed) {
		for (var allow : allowed)
			if (Objects.equals(t, allow)) return t;
		throw Exceptions.illegalArg("%s must be one of %s: %s", Reflect.className(t),
			Arrays.toString(allowed), t);
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
	 * Returns true if any values are null, and the varargs array is not null.
	 */
	@SafeVarargs
	public static <T> boolean anyNull(T... args) {
		if (args == null) return false;
		for (T arg : args)
			if (arg == null) return true;
		return false;
	}

	/**
	 * Returns true if any values are null, or the varargs array is null.
	 */
	@SafeVarargs
	public static <T> boolean noneNull(T... args) {
		return !anyNull(args);
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
	public static <E extends Exception> int defInt(Integer value,
		Excepts.IntSupplier<E> defSupplier) throws E {
		return value != null ? value : Objects.requireNonNull(defSupplier).getAsInt();
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
	public static <E extends Exception> long defLong(Long value,
		Excepts.LongSupplier<E> defSupplier) throws E {
		return value != null ? value : Objects.requireNonNull(defSupplier).getAsLong();
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
		Excepts.DoubleSupplier<E> defSupplier) throws E {
		return value != null ? value : Objects.requireNonNull(defSupplier).getAsDouble();
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception, T> T defGet(Excepts.Supplier<E, ? extends T> supplier,
		Excepts.Supplier<E, ? extends T> defSupplier) throws E {
		return def(supplier == null ? null : supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> int defGetInt(
		Excepts.Supplier<E, ? extends Integer> supplier, Excepts.IntSupplier<E> defSupplier)
		throws E {
		return defInt(supplier == null ? null : supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> long defGetLong(
		Excepts.Supplier<E, ? extends Long> supplier, Excepts.LongSupplier<E> defSupplier)
		throws E {
		return defLong(supplier == null ? null : supplier.get(), defSupplier);
	}

	/**
	 * Returns default value if main value is null.
	 */
	public static <E extends Exception> double defGetDouble(
		Excepts.Supplier<E, ? extends Double> supplier, Excepts.DoubleSupplier<E> defSupplier)
		throws E {
		return defDouble(supplier == null ? null : supplier.get(), defSupplier);
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
