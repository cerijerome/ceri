package ceri.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.text.Format;
import ceri.common.text.Strings;

/**
 * Common validation methods. IllegalArgumentException is thrown for a failed validation. Specified
 * names will always be at the start of the message, so the caller may wish to capitalize the first
 * letter.
 */
public class Validate {
	public static final String VALUE = "Value";
	private static final String EXPRESSION = "Expression";
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
	public static boolean condition(boolean condition, String format, Object... args) {
		if (condition) return condition;
		throw failed("%s is false", f(format, args));
	}
	
	// objects
	
	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T equal(T actual, T expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value.
	 */
	public static <T> T equal(T actual, T expected, String format, Object... args) {
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
	
	// arrays

	/**
	 * Fails if any value is null.
	 */
	public static void allNonNull(Object... actuals) {
		for (int i = 0; i < actuals.length; i++)
			nonNull(actuals[i], VALUE + " " + i);
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
	public static byte ubyte(long actual, long expected) {
		return ubyte(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static byte ubyte(long actual, long expected, String format, Object... args) {
		return (byte) equal(Maths.ubyte(actual), Maths.ubyte(expected), format, args);
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
	public static short ushort(long actual, long expected) {
		return ushort(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static short ushort(long actual, long expected, String format, Object... args) {
		return (short) equal(Maths.ushort(actual), Maths.ushort(expected), format, args);
	}

	/**
	 * Fails if the value is outside the unsigned range.
	 */
	public static long uint(int actual) {
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
	public static int uint(int actual, long expected) {
		return uint(actual, expected, "");
	}

	/**
	 * Fails if the unsigned value does not equal the expected value.
	 */
	public static int uint(int actual, long expected, String format, Object... args) {
		return (int) equal(Maths.uint(actual), Maths.uint(expected), format, args);
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
	 * Fails if the value does not equal the expected value. Equality includes infinity and NaN.
	 */
	public static double equal(double actual, double expected) {
		return equal(actual, expected, "");
	}

	/**
	 * Fails if the value does not equal the expected value. Equality includes infinity and NaN.
	 */
	public static double equal(double actual, double expected, String format, Object... args) {
		if (doubleEqual(actual, expected)) return actual;
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
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected) {
		return approx(actual, expected, PRECISION_DEF);
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision) {
		return approx(actual, expected, precision, "");
	}

	/**
	 * Fails if the value does not equal the expected value within precision decimal places.
	 */
	public static double approx(double actual, double expected, int precision, String format,
		Object... args) {
		if (!Double.isFinite(expected)) return equal(actual, expected, format, args);
		return equal(Maths.round(precision, actual), Maths.round(precision, expected), format,
			args);
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
	public static double nonNaN(double actual) {
		return nonNaN(actual, "");
	}

	/**
	 * Fails if the actual is NaN.
	 */
	public static double nonNaN(double actual, String format, Object... args) {
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
		if (Strings.isEmpty(format)) return VALUE;
		return Strings.format(format, args);
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
		return Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(other);
	}

	// ---------------------------------------------------------------------------------
	// Original methods start here:
	// ---------------------------------------------------------------------------------

	/* Expression validation */

	/**
	 * Validates that the predicate is true for the value.
	 */
	public static <T> T validate(Functions.Predicate<T> predicate, T value) {
		return validate(predicate, value, VALUE);
	}

	/**
	 * Validates that the predicate is true for the value.
	 */
	public static <T> T validate(Functions.Predicate<T> predicate, T value, String name) {
		if (!predicate.test(value)) throw exceptionf("%s is invalid: %s", name(name), value);
		return value;
	}

	/**
	 * Validates that the expression is true.
	 */
	public static void validate(boolean expr) {
		validate(expr, EXPRESSION);
	}

	/**
	 * Validates that the expression is true.
	 */
	public static void validate(boolean expr, String name) {
		if (!expr) throw exceptionf("%s failed", name == null ? EXPRESSION : name);
	}

	/**
	 * Validates that the expression is true, with formatted exception text if not.
	 */
	public static void validatef(boolean expr, String format, Object... args) {
		if (!expr) throw exceptionf(format, args);
	}

	/* (Not) null validation */

	/**
	 * Validates that the object is not null.
	 */
	public static <T> T validateSupported(T value, String name) {
		if (value != null) return value;
		throw new UnsupportedOperationException(name + " is not supported");
	}

	/**
	 * Validates that the object is not null.
	 */
	public static <T> T validateNotNull(T value) {
		return validateNotNull(value, VALUE);
	}

	/**
	 * Validates that the object is not null.
	 */
	public static <T> T validateNotNull(T value, String name) {
		if (value == null) throw exceptionf("%s == null", name);
		return value;
	}

	/**
	 * Validates that the objects are not null.
	 */
	public static void validateAllNotNull(Object... values) {
		Validate.validateNotNull(values);
		for (int i = 0; i < values.length; i++)
			if (values[i] == null) throw exceptionf("Value [%d] == null", i);
	}

	/**
	 * Validates that the object is null.
	 */
	public static <T> T validateNull(T value) {
		return validateNull(value, VALUE);
	}

	/**
	 * Validates that the object is null.
	 */
	public static <T> T validateNull(T value, String name) {
		if (value != null) throw exceptionf("%s != null: %s", name(name), value);
		return value;
	}

	/* Object (in-)equality validation */

	/**
	 * Validates that the objects are equal.
	 */
	public static <T> T validateEqualObj(T value, Object expected) {
		return validateEqualObj(value, expected, VALUE);
	}

	/**
	 * Validates that the objects are equal.
	 */
	public static <T> T validateEqualObj(T value, Object expected, String name) {
		if (!Objects.equals(value, expected))
			throw exceptionf("%s != %s: %s", name(name), expected, value);
		return value;
	}

	/**
	 * Validates that the objects are not equal.
	 */
	public static <T> T validateNotEqualObj(T value, Object unexpected) {
		return validateNotEqualObj(value, unexpected, VALUE);
	}

	/**
	 * Validates that the objects are not equal.
	 */
	public static <T> T validateNotEqualObj(T value, Object unexpected, String name) {
		if (Objects.equals(value, unexpected)) throw exceptionf("%s = %s", name(name), value);
		return value;
	}

	/* Text validation */

	/**
	 * Validate the string matches the pattern.
	 */
	public static Matcher validateMatch(String value, Pattern pattern) {
		var m = pattern.matcher(value);
		if (m.matches()) return m;
		throw exceptionf("Regex did not match \"%s\": %s", pattern.pattern(), value);
	}

	/**
	 * Validate the string contains the pattern.
	 */
	public static Matcher validateFind(String value, Pattern pattern) {
		var m = pattern.matcher(value);
		if (m.find()) return m;
		throw exceptionf("Regex not found \"%s\": %s", pattern.pattern(), value);
	}

	/* Collection validation */

	/**
	 * Validate the collection is empty.
	 */
	public static <T extends Collection<?>> T validateEmpty(T collection) {
		if (collection.isEmpty()) return collection;
		throw exceptionf("Collection is not empty");
	}

	/**
	 * Validate the map is empty.
	 */
	public static <T extends Map<?, ?>> T validateEmpty(T map) {
		if (map.isEmpty()) return map;
		throw exceptionf("Map is not empty");
	}

	/**
	 * Validate the collection is not empty.
	 */
	public static <T extends Collection<?>> T validateNotEmpty(T collection) {
		if (!collection.isEmpty()) return collection;
		throw exceptionf("Collection is empty");
	}

	/**
	 * Validate the map is not empty.
	 */
	public static <T extends Map<?, ?>> T validateNotEmpty(T map) {
		if (!map.isEmpty()) return map;
		throw exceptionf("Map is empty");
	}

	/**
	 * Validate the collection contains the value.
	 */
	public static <T, C extends Collection<T>> C validateContains(C collection, T value) {
		if (collection.contains(value)) return collection;
		throw exceptionf("Collection does not contain %s", value);
	}

	/**
	 * Validate the map contains the key.
	 */
	public static <K, M extends Map<K, ?>> M validateContainsKey(M map, K key) {
		if (map.containsKey(key)) return map;
		throw exceptionf("Map does not contain key %s", key);
	}

	/**
	 * Validate the map contains the key.
	 */
	public static <V, M extends Map<?, V>> M validateContainsValue(M map, V value) {
		if (map.containsValue(value)) return map;
		throw exceptionf("Map does not contain value %s", value);
	}

	/* Support methods */

	private static IllegalArgumentException exceptionf(String format, Object... args) {
		return new IllegalArgumentException(Strings.format(format, args));
	}

	private static String name(String name) {
		return name == null ? VALUE : name;
	}
}
