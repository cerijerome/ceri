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
 * Common validation methods. Specified names will always be at the start of message, so the caller
 * may wish to capitalize the first letter.
 */
public class ValidationUtil {
	private static final String VALUE = "Value";
	private static final String EXPRESSION = "Expression";

	private ValidationUtil() {}

	/* Expression validation */

	public static <T> void validate(Predicate<T> predicate, T value) {
		validate(predicate, value, VALUE);
	}

	public static <T> void validate(Predicate<T> predicate, T value, String name) {
		if (!predicate.test(value)) throw exceptionf("%s is invalid: %s", name(name), value);
	}

	public static void validate(boolean expr) {
		validate(expr, EXPRESSION);
	}

	public static void validate(boolean expr, String name) {
		if (!expr) throw exceptionf("%s failed", name == null ? EXPRESSION : name);
	}

	public static void validatef(boolean expr, String format, Object... args) {
		if (!expr) throw exceptionf(format, args);
	}

	/* (Not) null validation */

	public static void validateNotNull(Object value) {
		validateNotNull(value, VALUE);
	}

	public static void validateNotNull(Object value, String name) {
		if (value == null) throw exceptionf("%s = null", name);
	}

	public static void validateNull(Object value) {
		validateNull(value, VALUE);
	}

	public static void validateNull(Object value, String name) {
		if (value != null) throw exceptionf("%s != null: %s", name(name), value);
	}

	/* Object (in-)equality validation */

	public static void validateEqual(Object value, Object expected) {
		validateEqual(value, expected, VALUE);
	}

	public static void validateEqual(Object value, Object expected, String name) {
		if (!EqualsUtil.equals(value, expected))
			throw exceptionf("%s != %s: %s", name(name), expected, value);
	}

	public static void validateNotEqual(Object value, Object unexpected) {
		validateNotEqual(value, unexpected, VALUE);
	}

	public static void validateNotEqual(Object value, Object unexpected, String name) {
		if (EqualsUtil.equals(value, unexpected))
			throw exceptionf("%s = %s: %s", name(name), unexpected, value);
	}

	/* Number (in-)equality validation */

	public static void validateEqual(long value, long expected, DisplayLong... flags) {
		validateEqual(value, expected, VALUE, flags);
	}

	public static void validateEqual(long value, long expected, String name, DisplayLong... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(value, flags),
			format(expected, flags));
	}

	public static void validateNotEqual(long value, long expected, DisplayLong... flags) {
		validateNotEqual(value, expected, VALUE, flags);
	}

	public static void validateNotEqual(long value, long expected, String name,
		DisplayLong... flags) {
		if (value == expected) throw exceptionf("%s = %s: %s", name(name), format(value, flags),
			format(expected, flags));
	}

	public static void validateEqual(double value, double expected, DisplayDouble... flags) {
		validateEqual(value, expected, VALUE, flags);
	}

	public static void validateEqual(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value != expected) throw exceptionf("%s != %s: %s", name(name), format(value, flags),
			format(expected, flags));
	}

	public static void validateNotEqual(double value, double expected, DisplayDouble... flags) {
		validateNotEqual(value, expected, VALUE, flags);
	}

	public static void validateNotEqual(double value, double expected, String name,
		DisplayDouble... flags) {
		if (value == expected) throw exceptionf("%s = %s: %s", name(name), format(value, flags),
			format(expected, flags));
	}

	/* Unsigned equality validation */

	public static void validateUbyte(long value, long expected, DisplayLong... flags) {
		validateUbyte(value, expected, VALUE, flags);
	}

	public static void validateUbyte(long value, long expected, String name, DisplayLong... flags) {
		validateEqual(ubyte(value), ubyte(expected), name(name), def(flags, hex2, udec));
	}

	public static void validateUshort(long value, long expected, DisplayLong... flags) {
		validateUshort(value, expected, VALUE, flags);
	}

	public static void validateUshort(long value, long expected, String name,
		DisplayLong... flags) {
		validateEqual(ushort(value), ushort(expected), name(name), def(flags, hex4, udec));
	}

	public static void validateUint(long value, long expected, DisplayLong... flags) {
		validateUint(value, expected, VALUE, flags);
	}

	public static void validateUint(long value, long expected, String name, DisplayLong... flags) {
		validateEqual(uint(value), uint(expected), name(name), def(flags, hex8, udec));
	}

	public static void validateUlong(long value, long expected, DisplayLong... flags) {
		validateUlong(value, expected, VALUE, flags);
	}

	public static void validateUlong(long value, long expected, String name, DisplayLong... flags) {
		validateEqual(value, expected, name(name), def(flags, hex16, udec));
	}

	/* Interval validation */

	public static <T> void validateWithin(T value, Interval<T> interval) {
		validateWithin(value, interval, VALUE);
	}

	public static <T> void validateWithin(T value, Interval<T> interval, String name) {
		if (!interval.contains(value))
			throw exceptionf("%s is not within %s: %s", name(name), interval, value);
	}

	public static <T> void validateWithout(T value, Interval<T> interval) {
		validateWithout(value, interval, VALUE);
	}

	public static <T> void validateWithout(T value, Interval<T> interval, String name) {
		if (interval.contains(value))
			throw exceptionf("%s is within %s: %s", name(name), interval, value);
	}

	public static void validateWithin(long value, Interval<Long> interval, DisplayLong... flags) {
		validateWithin(value, interval, VALUE, flags);
	}

	public static void validateWithin(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
	}

	public static void validateWithout(long value, Interval<Long> interval, DisplayLong... flags) {
		validateWithout(value, interval, VALUE, flags);
	}

	public static void validateWithout(long value, Interval<Long> interval, String name,
		DisplayLong... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
	}

	public static void validateWithin(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		validateWithin(value, interval, VALUE, flags);
	}

	public static void validateWithin(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (!interval.contains(value)) throw exceptionf("%s is not within %s: %s", name(name),
			format(interval, flags), format(value, flags));
	}

	public static void validateWithout(double value, Interval<Double> interval,
		DisplayDouble... flags) {
		validateWithout(value, interval, VALUE, flags);
	}

	public static void validateWithout(double value, Interval<Double> interval, String name,
		DisplayDouble... flags) {
		if (interval.contains(value)) throw exceptionf("%s is within %s: %s", name(name),
			format(interval, flags), format(value, flags));
	}

	/* Number min/max/range validation */

	public static void validateMin(long value, long min, DisplayLong... flags) {
		validateMin(value, min, VALUE, flags);
	}

	public static void validateMin(long value, long min, String name, DisplayLong... flags) {
		if (value < min)
			throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
	}

	public static void validateMin(double value, double min, DisplayDouble... flags) {
		validateMin(value, min, VALUE, flags);
	}

	public static void validateMin(double value, double min, String name, DisplayDouble... flags) {
		if (value < min)
			throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
	}

	public static void validateMax(long value, long max, DisplayLong... flags) {
		validateMax(value, max, VALUE, flags);
	}

	public static void validateMax(long value, long max, String name, DisplayLong... flags) {
		if (value > max)
			throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
	}

	public static void validateMax(double value, double max, DisplayDouble... flags) {
		validateMax(value, max, VALUE, flags);
	}

	public static void validateMax(double value, double max, String name, DisplayDouble... flags) {
		if (value > max)
			throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
	}

	public static void validateRange(long value, long min, long max, DisplayLong... flags) {
		validateRange(value, min, max, VALUE, flags);
	}

	public static void validateRange(long value, long min, long max, String name,
		DisplayLong... flags) {
		if (value < min || value > max) throw exceptionf("%s is not within [%s, %s]: %s",
			name(name), format(min, flags), format(max, flags), format(value, flags));
	}

	public static void validateRange(double value, double min, double max, DisplayDouble... flags) {
		validateRange(value, min, max, VALUE, flags);
	}

	public static void validateRange(double value, double min, double max, String name,
		DisplayDouble... flags) {
		if (value < min || value > max) throw exceptionf("%s is not within [%s, %s]: %s",
			name(name), format(min, flags), format(max, flags), format(value, flags));
	}

	/* Unsigned long min/max/range validation */

	public static void validateUmin(long value, long min, DisplayLong... flags) {
		validateUmin(value, min, VALUE, flags);
	}

	public static void validateUmin(long value, long min, String name, DisplayLong... flags) {
		if (uwithin(value, min, null)) return;
		flags = def(flags, hex);
		throw exceptionf("%s < %s: %s", name(name), format(min, flags), format(value, flags));
	}

	public static void validateUmax(long value, long max, DisplayLong... flags) {
		validateUmax(value, max, VALUE, flags);
	}

	public static void validateUmax(long value, long max, String name, DisplayLong... flags) {
		if (uwithin(value, null, max)) return;
		flags = def(flags, hex);
		throw exceptionf("%s > %s: %s", name(name), format(max, flags), format(value, flags));
	}

	public static void validateUrange(long value, long min, long max, DisplayLong... flags) {
		validateUrange(value, min, max, VALUE, flags);
	}

	public static void validateUrange(long value, long min, long max, String name,
		DisplayLong... flags) {
		if (uwithin(value, min, max)) return;
		flags = def(flags, hex);
		throw exceptionf("%s is not within [%s, %s]: %s", name(name), format(min, flags),
			format(max, flags), format(value, flags));
	}

	/* Unsigned type range validation */

	public static void validateUbyte(long value, DisplayLong... flags) {
		validateUbyte(value, VALUE, flags);
	}

	public static void validateUbyte(long value, String name, DisplayLong... flags) {
		validateRange(value, 0, BYTE_MASK, name(name), def(flags, hex));
	}

	public static void validateUshort(long value, DisplayLong... flags) {
		validateUshort(value, VALUE, flags);
	}

	public static void validateUshort(long value, String name, DisplayLong... flags) {
		validateRange(value, 0, SHORT_MASK, name(name), def(flags, hex));
	}

	public static void validateUint(long value, DisplayLong... flags) {
		validateUint(value, VALUE, flags);
	}

	public static void validateUint(long value, String name, DisplayLong... flags) {
		validateRange(value, 0, INT_MASK, name(name), def(flags, hex));
	}

	/* Byte validations */

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
