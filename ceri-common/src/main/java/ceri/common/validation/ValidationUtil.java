package ceri.common.validation;

import static java.lang.Long.compareUnsigned;
import java.util.function.Predicate;
import ceri.common.util.ExceptionUtil;

public class ValidationUtil {
	private static final String VALUE = "Value";

	private ValidationUtil() {}

	public static <T> void validate(Predicate<T> predicate, T value) {
		validate(predicate.test(value), "Unexpected value: %s", value);
	}

	public static <T> void validate(Predicate<T> predicate, T value, String name) {
		validate(predicate.test(value), "Unexpected %s value: %s", name, value);
	}

	public static void validate(boolean expr) {
		if (!expr) throw new IllegalArgumentException();
	}

	public static void validate(boolean expr, String name) {
		if (!expr) throw new IllegalArgumentException(name + " failed");
	}

	public static void validate(boolean expr, String format, Object... args) {
		if (!expr) throw ExceptionUtil.exceptionf(format, args);
	}

	public static void validateNotNull(Object value) {
		if (value == null) throw new NullPointerException();
	}

	public static void validateNotNull(Object value, String name) {
		if (value == null) throw new NullPointerException(name);
	}

	public static void validateNull(Object value) {
		validateNull(value, VALUE);
	}

	public static void validateNull(Object value, String name) {
		if (value != null) throw ExceptionUtil.exceptionf("%s must be null: %s", name, value);
	}

	public static void validateEqual(Object value, Object expected) {
		validateEqual(value, expected, VALUE);
	}

	public static void validateEqual(Object value, Object expected, String name) {
		if (value == expected) return;
		if (value != null && value.equals(expected)) return;
		throw ExceptionUtil.exceptionf("%s must be %s: %s", name, expected, value);
	}

	public static void validateEqual(long value, long expected) {
		if (value != expected) throw InvalidLongRangeException.equal(value, expected);
	}

	public static void validateEqual(long value, long expected, String name) {
		if (value != expected) throw InvalidLongRangeException.equal(value, expected, name);
	}

	public static void validateEqualUnsigned(long value, long expected) {
		if (value != expected) throw InvalidUnsignedRangeException.equal(value, expected);
	}

	public static void validateEqualUnsigned(long value, long expected, String name) {
		if (value != expected) throw InvalidUnsignedRangeException.equal(value, expected, name);
	}

	public static void validateMin(long value, long min) {
		if (value < min) throw InvalidLongRangeException.min(value, min);
	}

	public static void validateMin(long value, long min, String name) {
		if (value < min) throw InvalidLongRangeException.min(value, min, name);
	}

	public static void validateMax(long value, long max) {
		if (value > max) throw InvalidLongRangeException.max(value, max);
	}

	public static void validateMax(long value, long max, String name) {
		if (value > max) throw InvalidLongRangeException.max(value, max, name);
	}

	public static void validateRange(long value, long min, long max) {
		if (value < min || value > max) throw InvalidLongRangeException.range(value, min, max);
	}

	public static void validateRange(long value, long min, long max, String name) {
		if (value < min || value > max)
			throw InvalidLongRangeException.range(value, min, max, name);
	}

	public static void validateMinUnsigned(long value, long min) {
		if (compareUnsigned(value, min) < 0) throw InvalidUnsignedRangeException.min(value, min);
	}

	public static void validateMinUnsigned(long value, long min, String name) {
		if (compareUnsigned(value, min) < 0)
			throw InvalidUnsignedRangeException.min(value, min, name);
	}

	public static void validateMaxUnsigned(long value, long max) {
		if (compareUnsigned(value, max) > 0) throw InvalidUnsignedRangeException.max(value, max);
	}

	public static void validateMaxUnsigned(long value, long max, String name) {
		if (compareUnsigned(value, max) > 0)
			throw InvalidUnsignedRangeException.max(value, max, name);
	}

	public static void validateRangeUnsigned(long value, long min, long max) {
		if (compareUnsigned(value, min) < 0 || compareUnsigned(value, max) > 0)
			throw InvalidUnsignedRangeException.range(value, min, max);
	}

	public static void validateRangeUnsigned(long value, long min, long max, String name) {
		if (compareUnsigned(value, min) < 0 || compareUnsigned(value, max) > 0)
			throw InvalidUnsignedRangeException.range(value, min, max, name);
	}

	public static void validateEqual(double value, double expected) {
		if (value != expected) throw InvalidDoubleRangeException.equal(value, expected);
	}

	public static void validateEqual(double value, double expected, String name) {
		if (value != expected) throw InvalidDoubleRangeException.equal(value, expected, name);
	}

	public static void validateMin(double value, double min) {
		if (value < min) throw InvalidDoubleRangeException.min(value, min);
	}

	public static void validateMin(double value, double min, String name) {
		if (value < min) throw InvalidDoubleRangeException.min(value, min, name);
	}

	public static void validateMax(double value, double max) {
		if (value > max) throw InvalidDoubleRangeException.max(value, max);
	}

	public static void validateMax(double value, double max, String name) {
		if (value > max) throw InvalidDoubleRangeException.max(value, max, name);
	}

	public static void validateRange(double value, double min, double max) {
		if (value < min || value > max) throw InvalidDoubleRangeException.range(value, min, max);
	}

	public static void validateRange(double value, double min, double max, String name) {
		if (value < min || value > max)
			throw InvalidDoubleRangeException.range(value, min, max, name);
	}

}
