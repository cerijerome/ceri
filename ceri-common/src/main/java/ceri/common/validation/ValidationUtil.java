package ceri.common.validation;

import static ceri.common.data.ByteUtil.BYTE_MASK;
import static ceri.common.data.ByteUtil.INT_MASK;
import static ceri.common.data.ByteUtil.SHORT_MASK;
import static ceri.common.math.MathUtil.ubyte;
import static ceri.common.math.MathUtil.uint;
import static ceri.common.math.MathUtil.ushort;
import static ceri.common.validation.DisplayLong.hex;
import static ceri.common.validation.DisplayLong.hex16;
import static ceri.common.validation.DisplayLong.hex2;
import static ceri.common.validation.DisplayLong.hex4;
import static ceri.common.validation.DisplayLong.hex8;
import static ceri.common.validation.DisplayLong.udec;
import java.util.function.Predicate;
import ceri.common.math.Interval;
import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;

/**
 * Common validation methods. IllegalArgumentException is thrown for a failed validation. Specified
 * names will always be at the start of the message, so the caller may wish to capitalize the first
 * letter.
 */
public class ValidationUtil {
	private static final String VALUE = "Value";
	private static final String EXPRESSION = "Expression";

	private ValidationUtil() {}

	/* Expression validation */

	/**
	 * Validates that the predicate is true for the value.
	 */
	public static <T> T validate(Predicate<T> predicate, T value) {
		return validate(predicate, value, VALUE);
	}

	/**
	 * Validates that the predicate is true for the value.
	 */
	public static <T> T validate(Predicate<T> predicate, T value, String name) {
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
	public static <T> T validateNotNull(T value) {
		return validateNotNull(value, VALUE);
	}

	/**
	 * Validates that the object is not null.
	 */
	public static <T> T validateNotNull(T value, String name) {
		if (value == null) throw exceptionf("%s = null", name);
		return value;
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
	public static <T> T validateEquals(T value, Object expected) {
		return validateEquals(value, expected, VALUE);
	}

	/**
	 * Validates that the objects are equal.
	 */
	public static <T> T validateEquals(T value, Object expected, String name) {
		if (!EqualsUtil.equals(value, expected))
			throw exceptionf("%s != %s: %s", name(name), expected, value);
		return value;
	}

	/**
	 * Validates that the objects are not equal.
	 */
	public static <T> T validateNotEquals(T value, Object unexpected) {
		return validateNotEquals(value, unexpected, VALUE);
	}

	/**
	 * Validates that the objects are not equal.
	 */
	public static <T> T validateNotEquals(T value, Object unexpected, String name) {
		if (EqualsUtil.equals(value, unexpected))
			throw exceptionf("%s = %s: %s", name(name), unexpected, value);
		return value;
	}

	/* Number (in-)equality validation */

	/**
	 * Validates that the integer values are equal.
	 */
	public static long validateEqualL(long value, long expected, DisplayLong... flags) {
		return validateEqualL(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the integer values are equal.
	 */
	public static long validateEqualL(long value, long expected, String name, DisplayLong... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(value, flags),
			format(expected, flags));
		return value;
	}

	/**
	 * Validates that the integer values are not equal.
	 */
	public static long validateNotEqualL(long value, long expected, DisplayLong... flags) {
		return validateNotEqualL(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the integer values are not equal.
	 */
	public static long validateNotEqualL(long value, long expected, String name,
		DisplayLong... flags) {
		if (value == expected) throw exceptionf("%s = %s: %s", name(name), format(value, flags),
			format(expected, flags));
		return value;
	}

	/**
	 * Validates that the floating point values are equal.
	 */
	public static double validateEqualD(double value, double expected, DisplayDouble... flags) {
		return validateEqualD(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the floating point values are equal.
	 */
	public static double validateEqualD(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(value, flags),
			format(expected, flags));
		return value;
	}

	/**
	 * Validates that the floating point values are not equal.
	 */
	public static double validateNotEqualD(double value, double expected, DisplayDouble... flags) {
		return validateNotEqualD(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the floating point values are not equal.
	 */
	public static double validateNotEqualD(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value == expected) throw exceptionf("%s = %s: %s", name(name), format(value, flags),
			format(expected, flags));
		return value;
	}

	/* Unsigned equality validation */

	/**
	 * Validates that unsigned byte values are equal.
	 */
	public static long validateUbyte(long value, long expected, DisplayLong... flags) {
		return validateUbyte(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned byte values are equal.
	 */
	public static long validateUbyte(long value, long expected, String name, DisplayLong... flags) {
		return validateEqualL(ubyte(value), ubyte(expected), name(name), def(flags, hex2, udec));
	}

	/**
	 * Validates that unsigned short values are equal.
	 */
	public static long validateUshort(long value, long expected, DisplayLong... flags) {
		return validateUshort(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned short values are equal.
	 */
	public static long validateUshort(long value, long expected, String name,
		DisplayLong... flags) {
		return validateEqualL(ushort(value), ushort(expected), name(name), def(flags, hex4, udec));
	}

	/**
	 * Validates that unsigned int values are equal.
	 */
	public static long validateUint(long value, long expected, DisplayLong... flags) {
		return validateUint(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned int values are equal.
	 */
	public static long validateUint(long value, long expected, String name, DisplayLong... flags) {
		return validateEqualL(uint(value), uint(expected), name(name), def(flags, hex8, udec));
	}

	/**
	 * Validates that unsigned long values are equal.
	 */
	public static long validateUlong(long value, long expected, DisplayLong... flags) {
		return validateUlong(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned long values are equal.
	 */
	public static long validateUlong(long value, long expected, String name, DisplayLong... flags) {
		return validateEqualL(value, expected, name(name), def(flags, hex16, udec));
	}

	/* Interval validation */

	/**
	 * Validates that the value is within the interval.
	 */
	public static <T> T validateWithin(T value, Interval<T> interval) {
		return validateWithin(value, interval, VALUE);
	}

	/**
	 * Validates that the value is within the interval.
	 */
	public static <T> T validateWithin(T value, Interval<T> interval, String name) {
		if (!interval.contains(value))
			throw exceptionf("%s is not within %s: %s", name(name), interval, value);
		return value;
	}

	/**
	 * Validates that the value is not within the interval.
	 */
	public static <T> T validateWithout(T value, Interval<T> interval) {
		return validateWithout(value, interval, VALUE);
	}

	/**
	 * Validates that the value is not within the interval.
	 */
	public static <T> T validateWithout(T value, Interval<T> interval, String name) {
		if (interval.contains(value))
			throw exceptionf("%s is within %s: %s", name(name), interval, value);
		return value;
	}

	/**
	 * Validates that the integer value is within the interval.
	 */
	public static long validateWithinL(long value, Interval<Long> interval, DisplayLong... flags) {
		return validateWithinL(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the integer value is within the interval.
	 */
	public static long validateWithinL(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the integer value is not within the interval.
	 */
	public static long validateWithoutL(long value, Interval<Long> interval, DisplayLong... flags) {
		return validateWithoutL(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the integer value is not within the interval.
	 */
	public static long validateWithoutL(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point value is within the interval.
	 */
	public static double validateWithinD(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		return validateWithinD(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the floating point value is within the interval.
	 */
	public static double validateWithinD(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point value is not within the interval.
	 */
	public static double validateWithoutD(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		return validateWithoutD(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the floating point value is not within the interval.
	 */
	public static double validateWithoutD(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/* Number min/max/range validation */

	/**
	 * Validates that integer value is >= minimum.
	 */
	public static long validateMinL(long value, long min, DisplayLong... flags) {
		return validateMinL(value, min, VALUE, flags);
	}

	/**
	 * Validates that integer value is >= minimum.
	 */
	public static long validateMinL(long value, long min, String name, DisplayLong... flags) {
		if (value < min)
			throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that floating point value is >= minimum.
	 */
	public static double validateMinD(double value, double min, DisplayDouble... flags) {
		return validateMinD(value, min, VALUE, flags);
	}

	/**
	 * Validates that floating point value is >= minimum.
	 */
	public static double validateMinD(double value, double min, String name,
		DisplayDouble... flags) {
		if (value < min)
			throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that integer value is <= maximum.
	 */
	public static long validateMaxL(long value, long max, DisplayLong... flags) {
		return validateMaxL(value, max, VALUE, flags);
	}

	/**
	 * Validates that integer value is <= maximum.
	 */
	public static long validateMaxL(long value, long max, String name, DisplayLong... flags) {
		if (value > max)
			throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that floating point value is <= maximum.
	 */
	public static double validateMaxD(double value, double max, DisplayDouble... flags) {
		return validateMaxD(value, max, VALUE, flags);
	}

	/**
	 * Validates that floating point value is <= maximum.
	 */
	public static double validateMaxD(double value, double max, String name,
		DisplayDouble... flags) {
		if (value > max)
			throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that integer value is inclusively between minimum and maximum.
	 */
	public static long validateRangeL(long value, long min, long max, DisplayLong... flags) {
		return validateRangeL(value, min, max, VALUE, flags);
	}

	/**
	 * Validates that integer value is inclusively between minimum and maximum.
	 */
	public static long validateRangeL(long value, long min, long max, String name,
		DisplayLong... flags) {
		if (value < min || value > max) throw exceptionf("%s is not within [%s, %s]: %s",
			name(name), format(min, flags), format(max, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that floating point value is inclusively between minimum and maximum.
	 */
	public static double validateRangeD(double value, double min, double max,
		DisplayDouble... flags) {
		return validateRangeD(value, min, max, VALUE, flags);
	}

	/**
	 * Validates that floating point value is inclusively between minimum and maximum.
	 */
	public static double validateRangeD(double value, double min, double max, String name,
		DisplayDouble... flags) {
		if (value < min || value > max) throw exceptionf("%s is not within [%s, %s]: %s",
			name(name), format(min, flags), format(max, flags), format(value, flags));
		return value;
	}

	/* Unsigned long min/max/range validation */

	/**
	 * Validates that unsigned integer value >= minimum.
	 */
	public static long validateUmin(long value, long min, DisplayLong... flags) {
		return validateUmin(value, min, VALUE, flags);
	}

	/**
	 * Validates that unsigned integer value >= minimum.
	 */
	public static long validateUmin(long value, long min, String name, DisplayLong... flags) {
		if (uwithin(value, min, null)) return value;
		flags = def(flags, hex);
		throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
	}

	/**
	 * Validates that unsigned integer value <= maximum.
	 */
	public static long validateUmax(long value, long max, DisplayLong... flags) {
		return validateUmax(value, max, VALUE, flags);
	}

	/**
	 * Validates that unsigned integer value <= maximum.
	 */
	public static long validateUmax(long value, long max, String name, DisplayLong... flags) {
		if (uwithin(value, null, max)) return value;
		flags = def(flags, hex);
		throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
	}

	/**
	 * Validates that unsigned integer value is inclusively between minimum and maximum.
	 */
	public static long validateUrange(long value, long min, long max, DisplayLong... flags) {
		return validateUrange(value, min, max, VALUE, flags);
	}

	/**
	 * Validates that unsigned integer value is inclusively between minimum and maximum.
	 */
	public static long validateUrange(long value, long min, long max, String name,
		DisplayLong... flags) {
		if (uwithin(value, min, max)) return value;
		flags = def(flags, hex);
		throw exceptionf("%s is not within [%s, %s]: %s", name(name), format(min, flags),
			format(max, flags), format(value, flags));
	}

	/* Unsigned type range validation */

	/**
	 * Validates value is within unsigned byte range.
	 */
	public static long validateUbyte(long value, DisplayLong... flags) {
		return validateUbyte(value, VALUE, flags);
	}

	/**
	 * Validates value is within unsigned byte range.
	 */
	public static long validateUbyte(long value, String name, DisplayLong... flags) {
		return validateRangeL(value, 0, BYTE_MASK, name(name), def(flags, hex));
	}

	/**
	 * Validates value is within unsigned short range.
	 */
	public static long validateUshort(long value, DisplayLong... flags) {
		return validateUshort(value, VALUE, flags);
	}

	/**
	 * Validates value is within unsigned short range.
	 */
	public static long validateUshort(long value, String name, DisplayLong... flags) {
		return validateRangeL(value, 0, SHORT_MASK, name(name), def(flags, hex));
	}

	/**
	 * Validates value is within unsigned int range.
	 */
	public static long validateUint(long value, DisplayLong... flags) {
		return validateUint(value, VALUE, flags);
	}

	/**
	 * Validates value is within unsigned int range.
	 */
	public static long validateUint(long value, String name, DisplayLong... flags) {
		return validateRangeL(value, 0, INT_MASK, name(name), def(flags, hex));
	}

	/* Byte validations */
	// TODO: add or remove
	//
	// public static int validateAscii(String value, ByteProvider data, int offset) {
	// ImmutableByteArray expected = ByteUtil.toAscii(value);
	// if (expected.matches(data, offset, expected.length())) return offset + expected.length();
	// throw ExceptionUtil.exceptionf("Expected %s: %s", escape(value),
	// escape(fromAscii(data, offset, expected.length())));
	// }
	//
	// public static int validateString(String value, ByteProvider data, int offset) {
	// ImmutableByteArray expected = ByteUtil.toAscii(value);
	// if (expected.matches(data, offset, expected.length())) return offset + expected.length();
	// throw ExceptionUtil.exceptionf("Expected %s: %s", escape(value),
	// escape(fromAscii(data, offset, expected.length())));
	// }

	/* Support methods */

	private static IllegalArgumentException exceptionf(String format, Object... args) {
		return new IllegalArgumentException(StringUtil.format(format, args));
	}

	private static boolean uwithin(long value, Long min, Long max) {
		if (min != null && Long.compareUnsigned(value, min) < 0) return false;
		if (max != null && Long.compareUnsigned(value, max) > 0) return false;
		return true;
	}

	@SafeVarargs
	private static <T> T[] def(T[] values, T... def) {
		return values.length > 0 ? values : def;
	}

	private static String name(String name) {
		return name == null ? VALUE : name;
	}

	private static String format(long value, DisplayLong... flags) {
		return DisplayLong.format(value, flags);
	}

	private static String format(double value, DisplayDouble... flags) {
		return DisplayDouble.format(value, flags);
	}

	private static String format(Interval<Long> interval, DisplayLong... flags) {
		return StringUtil.format("%s%s, %s%s", interval.lower.type.left,
			format(interval.lower.value, flags), format(interval.upper.value, flags),
			interval.upper.type.right);
	}

	private static String format(Interval<Double> interval, DisplayDouble... flags) {
		return StringUtil.format("%s%s, %s%s", interval.lower.type.left,
			format(interval.lower.value, flags), format(interval.upper.value, flags),
			interval.upper.type.right);
	}

}
