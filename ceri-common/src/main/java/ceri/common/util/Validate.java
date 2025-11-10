package ceri.common.util;

import java.util.Collection;
import java.util.Objects;
import ceri.common.collect.Lists;
import ceri.common.except.Exceptions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.text.Format;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;

/**
 * Common validation methods. IllegalArgumentException is thrown for a failed validation. Specified
 * names will always be at the start of the message, so the caller may wish to capitalize the first
 * letter.
 */
public class Validate {
	private static final int PRECISION_DEF = 3;

	private Validate() {}

	// general

	/**
	 * Returns a runtime exception with failure message.
	 */
	public static RuntimeException failed(String format, Object... args) {
		return Exceptions.illegalArg(format, args);
	}

	/**
	 * Fails if the condition is false.
	 */
	public static boolean condition(boolean condition) {
		return condition(condition, "Condition failed");
	}

	/**
	 * Fails if the condition is false.
	 */
	public static boolean condition(boolean condition, String format, Object... args) {
		if (condition) return condition;
		throw failed(format, args);
	}

	// objects

	/**
	 * Fails if the object is not an instance of the type.
	 */
	public static <T> T instance(Object obj, Class<T> cls) {
		if (cls.isInstance(obj)) return Reflect.unchecked(obj);
		throw failed("Must be %s: %s", Reflect.name(cls), obj);
	}
	
	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T equals(T actual, T expected) {
		return equals(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T equals(T actual, T expected, String format, Object... args) {
		if (Objects.equals(actual, expected)) return actual;
		throw failed("%s must equal %s: %s", f(format, args), expected, actual);
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T notEqual(T actual, T unexpected) {
		return notEqual(actual, unexpected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T notEqual(T actual, T unexpected, String format, Object... args) {
		if (!Objects.equals(actual, unexpected)) return actual;
		throw failed("%s must not equal %s", f(format, args), unexpected);
	}

	/**
	 * Fails if the value is null.
	 */
	public static <T> T nonNull(T actual) {
		return nonNull(actual, "");
	}

	/**
	 * Fails if the value is null.
	 */
	public static <T> T nonNull(T actual, String format, Object... args) {
		return notEqual(actual, null, format, args);
	}

	/**
	 * Fails if the value does not equal any of the given values.
	 */
	@SafeVarargs
	public static <T> T equalAnyOf(T actual, T... values) {
		return equalAny(actual, Lists.wrap(values));
	}

	/**
	 * Fails if the value does not equal any of the given values.
	 */
	public static <T> T equalAny(T actual, Iterable<? extends T> values) {
		return equalAny(actual, values, "");
	}

	/**
	 * Fails if the value does not equal any of the given values.
	 */
	public static <T> T equalAny(T actual, Iterable<? extends T> values, String format,
		Object... args) {
		for (var value : values)
			if (Objects.equals(actual, value)) return actual;
		throw failed("%s must equal one of %s: %s", f(format, args), join(values), actual);
	}

	/**
	 * Fails if the value equals any of the given values.
	 */
	@SafeVarargs
	public static <T> T equalNoneOf(T actual, T... values) {
		return equalNone(actual, Lists.wrap(values));
	}

	/**
	 * Fails if the value equals any of the given values.
	 */
	public static <T> T equalNone(T actual, Iterable<? extends T> values) {
		return equalNone(actual, values, "");
	}

	/**
	 * Fails if the value equals any of the given values.
	 */
	public static <T> T equalNone(T actual, Iterable<? extends T> values, String format,
		Object... args) {
		for (var value : values)
			if (Objects.equals(actual, value)) throw failed("%s must not equal any of %s: %s",
				f(format, args), join(values), actual);
		return actual;
	}

	// arrays

	/**
	 * Fails if any value is null.
	 */
	public static void allNonNull(Object... actuals) {
		nonNull(actuals, "Values");
		for (int i = 0; i < actuals.length; i++)
			nonNull(actuals[i], "Value %d", i);
	}

	/**
	 * Fails if the index is out of range for the size.
	 */
	public static int index(int size, int index) {
		min(size, 0, "size");
		return range(index, 0, size - 1, "index");
	}

	/**
	 * Fails if the slice offset or length are out of range for the size. Returns true if the slice
	 * is the full range.
	 */
	public static boolean slice(int size, int offset, int length) {
		min(size, 0, "size");
		range(offset, 0, size, "offset");
		range(length, 0, size - offset, "length");
		return offset == 0 && length == size;
	}

	// collections

	/**
	 * Fails if the collection is null or empty.
	 */
	public static <T, C extends Collection<T>> C nonEmpty(C collection) {
		return nonEmpty(collection, "");
	}

	/**
	 * Fails if the collection is null or empty.
	 */
	public static <T, C extends Collection<T>> C nonEmpty(C collection, String format,
		Object... args) {
		nonNull(collection, format, args);
		if (!collection.isEmpty()) return collection;
		throw failed("%s is empty", f(format, args));
	}

	// integers

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static int equal(int actual, int expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static int equal(int actual, int expected, String format, Object... args) {
		if (actual == expected) return actual;
		throw failed("%s must equal %s: %s", f(format, args), n(expected), n(actual));
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static long equal(long actual, long expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static long equal(long actual, long expected, String format, Object... args) {
		if (actual == expected) return actual;
		throw failed("%s must equal %s: %s", f(format, args), n(expected), n(actual));
	}

	/**
	 * Fails if the value equals the expected value.
	 */
	public static int notEqual(int actual, int unexpected) {
		return notEqual(actual, unexpected, "");
	}

	/**
	 * Fails if the value equals the expected value.
	 */
	public static int notEqual(int actual, int unexpected, String format, Object... args) {
		if (actual != unexpected) return actual;
		throw failed("%s must not equal %s", f(format, args), n(unexpected));
	}

	/**
	 * Fails if the value equals the expected value.
	 */
	public static long notEqual(long actual, long unexpected) {
		return notEqual(actual, unexpected, "");
	}

	/**
	 * Fails if the value equals the expected value.
	 */
	public static long notEqual(long actual, long unexpected, String format, Object... args) {
		if (actual != unexpected) return actual;
		throw failed("%s must not equal %s", f(format, args), n(unexpected));
	}

	/**
	 * Fails if the value is less than min.
	 */
	public static int min(int actual, int min) {
		return min(actual, min, "");
	}

	/**
	 * Fails if the value is less than min.
	 */
	public static int min(int actual, int min, String format, Object... args) {
		if (actual >= min) return actual;
		throw failed("%s must be >= %s: %s", f(format, args), n(min), n(actual));
	}

	/**
	 * Fails if the value is less than min.
	 */
	public static long min(long actual, long min) {
		return min(actual, min, "");
	}

	/**
	 * Fails if the value is less than min.
	 */
	public static long min(long actual, long min, String format, Object... args) {
		if (actual >= min) return actual;
		throw failed("%s must be >= %s: %s", f(format, args), n(min), n(actual));
	}

	/**
	 * Fails if the value is greater than max.
	 */
	public static int max(int actual, int max) {
		return max(actual, max, "");
	}

	/**
	 * Fails if the value is greater than max.
	 */
	public static int max(int actual, int max, String format, Object... args) {
		if (actual <= max) return actual;
		throw failed("%s must be <= %s: %s", f(format, args), n(max), n(actual));
	}

	/**
	 * Fails if the value is greater than max.
	 */
	public static long max(long actual, long max) {
		return max(actual, max, "");
	}

	/**
	 * Fails if the value is greater than max.
	 */
	public static long max(long actual, long max, String format, Object... args) {
		if (actual <= max) return actual;
		throw failed("%s must be <= %s: %s", f(format, args), n(max), n(actual));
	}

	/**
	 * Fails if the value is not between inclusive min and max values.
	 */
	public static int range(int actual, int min, int max) {
		return range(actual, min, max, "");
	}

	/**
	 * Fails if the value is not between inclusive min and max values.
	 */
	public static int range(int actual, int min, int max, String format, Object... args) {
		min(actual, min, format, args);
		return max(actual, max, format, args);
	}

	/**
	 * Fails if the value is not between inclusive min and max values.
	 */
	public static long range(long actual, long min, long max) {
		return range(actual, min, max, "");
	}

	/**
	 * Fails if the value is not between inclusive min and max values.
	 */
	public static long range(long actual, long min, long max, String format, Object... args) {
		min(actual, min, format, args);
		return max(actual, max, format, args);
	}

	// unsigned integers

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static int ubyte(long actual) {
		return ubyte(actual, "");
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static int ubyte(long actual, String format, Object... args) {
		return (int) range(actual, 0, Maths.MAX_UBYTE, format, args);
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static int ubyte(long actual, long expected) {
		return ubyte(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static int ubyte(long actual, long expected, String format, Object... args) {
		return equal(Maths.ubyte(actual), Maths.ubyte(expected), format, args);
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static int ushort(long actual) {
		return ushort(actual, "");
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static int ushort(long actual, String format, Object... args) {
		return (int) range(actual, 0, Maths.MAX_USHORT, format, args);
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static int ushort(long actual, long expected) {
		return ushort(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static int ushort(long actual, long expected, String format, Object... args) {
		return equal(Maths.ushort(actual), Maths.ushort(expected), format, args);
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static long uint(long actual) {
		return uint(actual, "");
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static long uint(long actual, String format, Object... args) {
		return range(actual, 0L, Maths.MAX_UINT, format, args);
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static long uint(long actual, long expected) {
		return uint(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static long uint(long actual, long expected, String format, Object... args) {
		return equal(Maths.uint(actual), Maths.uint(expected), format, args);
	}

	/**
	 * Fails if the unsigned value is less than min.
	 */
	public static long umin(long actual, long min) {
		return umin(actual, min, "");
	}

	/**
	 * Fails if the unsigned value is less than min.
	 */
	public static long umin(long actual, long min, String format, Object... args) {
		if (Long.compareUnsigned(actual, min) >= 0) return actual;
		throw failed("%s must be >= %s: %s", f(format, args), u(min), u(actual));
	}

	/**
	 * Fails if the unsigned value is greater than max.
	 */
	public static long umax(long actual, long max) {
		return umax(actual, max, "");
	}

	/**
	 * Fails if the unsigned value is greater than max.
	 */
	public static long umax(long actual, long max, String format, Object... args) {
		if (Long.compareUnsigned(actual, max) <= 0) return actual;
		throw failed("%s must be <= %s: %s", f(format, args), u(max), u(actual));
	}

	/**
	 * Fails if the unsigned value is not between inclusive min and max values.
	 */
	public static long urange(long actual, long min, long max) {
		return urange(actual, min, max, "");
	}

	/**
	 * Fails if the unsigned value is not between inclusive min and max values.
	 */
	public static long urange(long actual, long min, long max, String format, Object... args) {
		umin(actual, min, format, args);
		return umax(actual, max, format, args);
	}

	// floating point

	/**
	 * Fails if the value does not equal the expected value. Equality includes infinity, NaN, and
	 * +/-0.0.
	 */
	public static double equal(double actual, double expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value. Equality includes infinity, NaN, and
	 * +/-0.0.
	 */
	public static double equal(double actual, double expected, String format, Object... args) {
		if (doubleEqual(actual, expected)) return actual + 0.0;
		throw failed("%s must equal %s: %s", f(format, args), expected, actual);
	}

	/**
	 * Fails if the value equals the expected value. Equality includes infinity and NaN.
	 */
	public static double notEqual(double actual, double unexpected) {
		return notEqual(actual, unexpected, "");
	}

	/**
	 * Fails if the value equals the expected value. Equality includes infinity and NaN.
	 */
	public static double notEqual(double actual, double unexpected, String format, Object... args) {
		if (!doubleEqual(actual, unexpected)) return actual;
		throw failed("%s must not equal %s", f(format, args), unexpected);
	}

	/**
	 * Fails if the value does not equal the expected value within simple precision decimal places.
	 */
	public static double approx(double actual, double expected) {
		return approx(actual, expected, PRECISION_DEF);
	}

	/**
	 * Fails if the value does not equal the expected value within simple precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision) {
		return approx(actual, expected, precision, "");
	}

	/**
	 * Fails if the value does not equal the expected value within simple precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision, String format,
		Object... args) {
		if (!Double.isFinite(expected)) return equal(actual, expected, format, args);
		equal(Maths.simpleRound(precision, actual), Maths.simpleRound(precision, expected), format,
			args);
		return actual;
	}

	/**
	 * Fails if the value is less than min. Infinity is permitted.
	 */
	public static double min(double actual, double min) {
		return min(actual, min, "");
	}

	/**
	 * Fails if the value is less than min. Infinity is permitted.
	 */
	public static double min(double actual, double min, String format, Object... args) {
		if (doubleEqual(actual, min) || actual >= min) return actual;
		throw failed("%s must be >= %s: %s", f(format, args), min, actual);
	}

	/**
	 * Fails if the value is greater than max. -Infinity is permitted.
	 */
	public static double max(double actual, double max) {
		return max(actual, max, "");
	}

	/**
	 * Fails if the value is greater than max. -Infinity is permitted.
	 */
	public static double max(double actual, double max, String format, Object... args) {
		if (doubleEqual(actual, max) || actual <= max) return actual;
		throw failed("%s must be <= %s: %s", f(format, args), max, actual);
	}

	/**
	 * Fails if the value is not between inclusive min and max values. Infinity is permitted.
	 */
	public static double range(double actual, double min, double max) {
		return range(actual, min, max, "");
	}

	/**
	 * Fails if the value is not between inclusive min and max values. Infinity is permitted.
	 */
	public static double range(double actual, double min, double max, String format,
		Object... args) {
		min(actual, min, format, args);
		return max(actual, max, format, args);
	}

	/**
	 * Fails if the actual is NaN.
	 */
	public static double notNaN(double actual) {
		return notNaN(actual, "");
	}

	/**
	 * Fails if the actual is NaN.
	 */
	public static double notNaN(double actual, String format, Object... args) {
		return notEqual(actual, Double.NaN, format, args);
	}

	/**
	 * Fails if the actual is infinite or NaN.
	 */
	public static double finite(double actual) {
		return finite(actual, "");
	}

	/**
	 * Fails if the actual is infinite or NaN.
	 */
	public static double finite(double actual, String format, Object... args) {
		if (Double.isFinite(actual)) return actual;
		throw failed("%s must be finite: %s", f(format, args), actual);
	}

	/**
	 * Fails if the actual is infinite, NaN, or less then min.
	 */
	public static double finiteMin(double actual, double min) {
		return finiteMin(actual, min, "");
	}

	/**
	 * Fails if the actual is infinite, NaN, or less then min.
	 */
	public static double finiteMin(double actual, double min, String format, Object... args) {
		finite(actual);
		return min(actual, min, format, args);
	}

	/**
	 * Fails if the actual is greater than max. -Infinity is not permitted.
	 */
	public static double finiteMax(double actual, double max) {
		return finiteMax(actual, max, "");
	}

	/**
	 * Fails if the actual is greater than max. -Infinity is not permitted.
	 */
	public static double finiteMax(double actual, double max, String format, Object... args) {
		finite(actual, format, args);
		return max(actual, max, format, args);
	}

	// support

	private static String f(String format, Object... args) {
		return f("Value", format, args);
	}

	private static String f(String def, String format, Object... args) {
		if (Strings.isEmpty(format)) return def;
		return Strings.format(format, args);
	}

	private static String join(Iterable<?> values) {
		return Joiner.LIST.join(values);
	}

	private static String n(int value) {
		return value + "|" + Format.hex(value);
	}

	private static String n(long value) {
		return value + "|" + Format.hex(value);
	}

	private static String u(long value) {
		return Long.toUnsignedString(value) + "|" + Format.hex(value);
	}

	private static boolean doubleEqual(double value, double other) {
		return Double.doubleToRawLongBits(value + 0.0) == Double.doubleToRawLongBits(other + 0.0);
	}
}
