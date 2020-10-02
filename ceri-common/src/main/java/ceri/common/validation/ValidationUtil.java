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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import ceri.common.math.Bound;
import ceri.common.math.Interval;
import ceri.common.text.StringUtil;

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

	/* Lookup validation */

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T, R> R validateLookup(Function<T, R> lookup, T value) {
		return validateLookup(lookup, value, VALUE);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T, R> R validateLookup(Function<T, R> lookup, T value, String name) {
		R r = lookup.apply(value);
		if (r != null) return r;
		throw exceptionf("%s is invalid: %s", name, value, value);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T> T validateIntLookup(IntFunction<T> lookup, int value) {
		return validateIntLookup(lookup, value, VALUE);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T> T validateIntLookup(IntFunction<T> lookup, int value, String name) {
		T t = lookup.apply(value);
		if (t != null) return t;
		throw exceptionf("%s is invalid: %d (0x%x)", name, value, value);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T, R> R validateLookupEquals(Function<T, R> lookup, T value, R expected) {
		return validateLookupEquals(lookup, value, expected, VALUE);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T, R> R validateLookupEquals(Function<T, R> lookup, T value, R expected,
		String name) {
		R r = validateLookup(lookup, value, name);
		return validateEqualObj(r, expected);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T> T validateIntLookupEquals(IntFunction<T> lookup, int value, T expected) {
		return validateIntLookupEquals(lookup, value, expected, VALUE);
	}

	/**
	 * Validates and returns a value found by lookup function.
	 */
	public static <T> T validateIntLookupEquals(IntFunction<T> lookup, int value, T expected,
		String name) {
		T t = validateIntLookup(lookup, value, name);
		return validateEqualObj(t, expected);
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

	/* Number (in-)equality validation */

	/**
	 * Validates that the integer values are equal.
	 */
	public static long validateEqual(long value, long expected, DisplayLong... flags) {
		return validateEqual(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the integer values are equal.
	 */
	public static long validateEqual(long value, long expected, String name, DisplayLong... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(expected, flags),
			format(value, flags));
		return value;
	}

	/**
	 * Validates that the integer values are not equal.
	 */
	public static long validateNotEqual(long value, long expected, DisplayLong... flags) {
		return validateNotEqual(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the integer values are not equal.
	 */
	public static long validateNotEqual(long value, long expected, String name,
		DisplayLong... flags) {
		if (value == expected) throw exceptionf("%s = %s", name(name), format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point values are equal.
	 */
	public static double validateEqualFp(double value, double expected, DisplayDouble... flags) {
		return validateEqualFp(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the floating point values are equal.
	 */
	public static double validateEqualFp(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(expected, flags),
			format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point values are not equal.
	 */
	public static double validateNotEqualFp(double value, double expected, DisplayDouble... flags) {
		return validateNotEqualFp(value, expected, VALUE, flags);
	}

	/**
	 * Validates that the floating point values are not equal.
	 */
	public static double validateNotEqualFp(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value == expected) throw exceptionf("%s = %s", name(name), format(value, flags));
		return value;
	}

	/* Unsigned equality validation */

	/**
	 * Validates that unsigned byte values are equal.
	 */
	public static short validateUbyte(long value, long expected, DisplayLong... flags) {
		return validateUbyte(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned byte values are equal.
	 */
	public static short validateUbyte(long value, long expected, String name,
		DisplayLong... flags) {
		short ubyte = ubyte(value);
		validateEqual(ubyte, ubyte(expected), name(name), def(flags, hex2, udec));
		return ubyte;
	}

	/**
	 * Validates that unsigned short values are equal.
	 */
	public static int validateUshort(long value, long expected, DisplayLong... flags) {
		return validateUshort(value, expected, VALUE, flags);
	}

	/**
	 * Validates that unsigned short values are equal.
	 */
	public static int validateUshort(long value, long expected, String name, DisplayLong... flags) {
		int ushort = ushort(value);
		validateEqual(ushort, ushort(expected), name(name), def(flags, hex4, udec));
		return ushort;
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
		return validateEqual(uint(value), uint(expected), name(name), def(flags, hex8, udec));
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
		return validateEqual(value, expected, name(name), def(flags, hex16, udec));
	}

	/* Interval validation */

	/**
	 * Validates that the value is within the interval.
	 */
	public static <T> T validateWithinObj(T value, Interval<T> interval) {
		return validateWithinObj(value, interval, VALUE);
	}

	/**
	 * Validates that the value is within the interval.
	 */
	public static <T> T validateWithinObj(T value, Interval<T> interval, String name) {
		if (interval.contains(value)) return value;
		throw exceptionf("%s is not within %s: %s", name(name), interval, value);
	}

	/**
	 * Validates that the value is not within the interval.
	 */
	public static <T> T validateWithoutObj(T value, Interval<T> interval) {
		return validateWithoutObj(value, interval, VALUE);
	}

	/**
	 * Validates that the value is not within the interval.
	 */
	public static <T> T validateWithoutObj(T value, Interval<T> interval, String name) {
		if (interval.contains(value))
			throw exceptionf("%s is within %s: %s", name(name), interval, value);
		return value;
	}

	/**
	 * Validates that the integer value is within the interval.
	 */
	public static long validateWithin(long value, Interval<Long> interval, DisplayLong... flags) {
		return validateWithin(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the integer value is within the interval.
	 */
	public static long validateWithin(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the integer value is not within the interval.
	 */
	public static long validateWithout(long value, Interval<Long> interval, DisplayLong... flags) {
		return validateWithout(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the integer value is not within the interval.
	 */
	public static long validateWithout(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point value is within the interval.
	 */
	public static double validateWithinFp(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		return validateWithinFp(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the floating point value is within the interval.
	 */
	public static double validateWithinFp(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that the floating point value is not within the interval.
	 */
	public static double validateWithoutFp(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		return validateWithoutFp(value, interval, VALUE, flags);
	}

	/**
	 * Validates that the floating point value is not within the interval.
	 */
	public static double validateWithoutFp(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
		return value;
	}

	/* Number min/max/range validation */

	/**
	 * Validates that integer value is >= minimum.
	 */
	public static long validateMin(long value, long min, DisplayLong... flags) {
		return validateMin(value, min, VALUE, flags);
	}

	/**
	 * Validates that integer value is >= minimum.
	 */
	public static long validateMin(long value, long min, String name, DisplayLong... flags) {
		return validateMin(value, min, name, null, flags);
	}

	/**
	 * Validates value is > or >= minimum. A null bound is treated as inclusive.
	 */
	public static long validateMin(long value, long min, Bound.Type bound, DisplayLong... flags) {
		return validateMin(value, min, VALUE, bound, flags);
	}

	/**
	 * Validates value is > or >= minimum. A null bound is treated as inclusive.
	 */
	public static long validateMin(long value, long min, String name, Bound.Type bound,
		DisplayLong... flags) {
		if (bound == null) bound = Bound.Type.inclusive;
		if (bound.isLower(value, min)) return value;
		throw exceptionf("%s must be %s %s: %s", name(name), bound.lower, format(min, flags),
			format(value, flags));
	}

	/**
	 * Validates that floating point value is >= minimum.
	 */
	public static double validateMinFp(double value, double min, DisplayDouble... flags) {
		return validateMinFp(value, min, VALUE, flags);
	}

	/**
	 * Validates that floating point value is >= minimum.
	 */
	public static double validateMinFp(double value, double min, String name,
		DisplayDouble... flags) {
		return validateMinFp(value, min, name, null, flags);
	}

	/**
	 * Validates value is > or >= minimum. A null bound is treated as inclusive.
	 */
	public static double validateMinFp(double value, double min, Bound.Type bound,
		DisplayDouble... flags) {
		return validateMinFp(value, min, VALUE, bound, flags);
	}

	/**
	 * Validates value is > or >= minimum. A null bound is treated as inclusive.
	 */
	public static double validateMinFp(double value, double min, String name, Bound.Type bound,
		DisplayDouble... flags) {
		if (bound == null) bound = Bound.Type.inclusive;
		if (bound.isLower(value, min)) return value;
		throw exceptionf("%s must be %s %s: %s", name(name), bound.lower, format(min, flags),
			format(value, flags));
	}

	/**
	 * Validates that integer value is <= maximum.
	 */
	public static long validateMax(long value, long max, DisplayLong... flags) {
		return validateMax(value, max, VALUE, flags);
	}

	/**
	 * Validates that integer value is <= maximum.
	 */
	public static long validateMax(long value, long max, String name, DisplayLong... flags) {
		return validateMax(value, max, name, null, flags);
	}

	/**
	 * Validates value is < or <= maximum. A null bound is treated as inclusive.
	 */
	public static double validateMax(long value, long max, Bound.Type bound, DisplayLong... flags) {
		return validateMax(value, max, VALUE, bound, flags);
	}

	/**
	 * Validates value is < or <= maximum. A null bound is treated as inclusive.
	 */
	public static long validateMax(long value, long max, String name, Bound.Type bound,
		DisplayLong... flags) {
		if (bound == null) bound = Bound.Type.inclusive;
		if (bound.isUpper(value, max)) return value;
		throw exceptionf("%s must be %s %s: %s", name(name), bound.lower, format(max, flags),
			format(value, flags));
	}

	/**
	 * Validates that floating point value is <= maximum.
	 */
	public static double validateMaxFp(double value, double max, DisplayDouble... flags) {
		return validateMaxFp(value, max, VALUE, flags);
	}

	/**
	 * Validates that floating point value is <= maximum.
	 */
	public static double validateMaxFp(double value, double max, String name,
		DisplayDouble... flags) {
		return validateMaxFp(value, max, name, null, flags);
	}

	/**
	 * Validates value is < or <= maximum. A null bound is treated as inclusive.
	 */
	public static double validateMaxFp(double value, double max, Bound.Type bound,
		DisplayDouble... flags) {
		return validateMaxFp(value, max, VALUE, bound, flags);
	}

	/**
	 * Validates value is < or <= maximum. A null bound is treated as inclusive.
	 */
	public static double validateMaxFp(double value, double max, String name, Bound.Type bound,
		DisplayDouble... flags) {
		if (bound == null) bound = Bound.Type.inclusive;
		if (bound.isUpper(value, max)) return value;
		throw exceptionf("%s must be %s %s: %s", name(name), bound.lower, format(max, flags),
			format(value, flags));
	}

	/**
	 * Validates that integer value is inclusively between minimum and maximum.
	 */
	public static long validateRange(long value, long min, long max, DisplayLong... flags) {
		return validateRange(value, min, max, VALUE, flags);
	}

	/**
	 * Validates that integer value is inclusively between minimum and maximum.
	 */
	public static long validateRange(long value, long min, long max, String name,
		DisplayLong... flags) {
		if (value < min || value > max) throw exceptionf("%s is not within [%s, %s]: %s",
			name(name), format(min, flags), format(max, flags), format(value, flags));
		return value;
	}

	/**
	 * Validates that floating point value is inclusively between minimum and maximum.
	 */
	public static double validateRangeFp(double value, double min, double max,
		DisplayDouble... flags) {
		return validateRangeFp(value, min, max, VALUE, flags);
	}

	/**
	 * Validates that floating point value is inclusively between minimum and maximum.
	 */
	public static double validateRangeFp(double value, double min, double max, String name,
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
	public static short validateUbyte(long value, DisplayLong... flags) {
		return validateUbyte(value, VALUE, flags);
	}

	/**
	 * Validates value is within unsigned byte range.
	 */
	public static short validateUbyte(long value, String name, DisplayLong... flags) {
		return ubyte(validateRange(value, 0, BYTE_MASK, name(name), def(flags, hex)));
	}

	/**
	 * Validates value is within unsigned short range.
	 */
	public static int validateUshort(long value, DisplayLong... flags) {
		return validateUshort(value, VALUE, flags);
	}

	/**
	 * Validates value is within unsigned short range.
	 */
	public static int validateUshort(long value, String name, DisplayLong... flags) {
		return ushort(validateRange(value, 0, SHORT_MASK, name(name), def(flags, hex)));
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
		return validateRange(value, 0, INT_MASK, name(name), def(flags, hex));
	}

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
