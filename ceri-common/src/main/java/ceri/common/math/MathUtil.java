package ceri.common.math;

import static ceri.common.math.Bound.Type.exclusive;
import static ceri.common.validation.ValidationUtil.validateMin;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import ceri.common.collection.ArrayUtil;

public class MathUtil {
	public static final double PIx2 = Math.PI * 2; // common calculation
	public static final double PI_BY_2 = Math.PI / 2; // common calculation
	private static final int MAX_ROUND_PLACES = 10;
	private static final double MAX_ROUND = 1000000000.0;
	private static final int MAX_UBYTE = 0xff;
	private static final int MAX_USHORT = 0xffff;
	private static final long MAX_UINT = 0xffffffffL;

	private MathUtil() {}

	/**
	 * Determines int value from boolean.
	 */
	public static int toInt(boolean b) {
		return b ? 1 : 0;
	}

	/**
	 * Casts a double to an int. Throws an exception if outside the limits of int.
	 */
	public static int safeToInt(double value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) return (int) value;
		throw new ArithmeticException("int overflow");
	}

	/**
	 * Casts a double to a long. Throws an exception if outside the limits of long.
	 */
	public static long safeToLong(double value) {
		if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) return (long) value;
		throw new ArithmeticException("long overflow");
	}

	/**
	 * Casts a long to a byte. Throws an exception if outside the limits of byte.
	 */
	public static byte byteExact(long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) return (byte) value;
		throw new ArithmeticException("byte overflow");
	}

	/**
	 * Casts a long to a byte. Throws an exception if outside the limits of byte.
	 */
	public static short shortExact(long value) {
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) return (short) value;
		throw new ArithmeticException("short overflow");
	}

	/**
	 * Casts a long to unsigned byte. Throws an exception if outside the limits of unsigned byte.
	 */
	public static byte ubyteExact(long value) {
		if (value >= 0 && value <= MAX_UBYTE) return (byte) value;
		throw new ArithmeticException("byte overflow");
	}

	/**
	 * Casts a long to unsigned short. Throws an exception if outside the limits of unsigned short.
	 */
	public static short ushortExact(long value) {
		if (value >= 0 && value <= MAX_USHORT) return (short) value;
		throw new ArithmeticException("short overflow");
	}

	/**
	 * Casts a long to unsigned int. Throws an exception if outside the limits of unsigned int.
	 */
	public static int uintExact(long value) {
		if (value >= 0 && value <= MAX_UINT) return (int) value;
		throw new ArithmeticException("int overflow");
	}

	/**
	 * Returns the short value of an unsigned byte.
	 */
	public static short ubyte(long i) {
		return (short) (i & MAX_UBYTE);
	}

	/**
	 * Returns the int value of an unsigned short.
	 */
	public static int ushort(long i) {
		return (int) (i & MAX_USHORT);
	}

	/**
	 * Returns the long value of an unsigned int.
	 */
	public static long uint(long i) {
		return i & MAX_UINT;
	}

	/**
	 * Return absolute value. Throws ArithmeticException if overflow.
	 */
	public static int absExact(int value) {
		if (value != Integer.MIN_VALUE) return Math.abs(value);
		throw new ArithmeticException("int overflow");
	}

	/**
	 * Return absolute value. Throws ArithmeticException if overflow.
	 */
	public static long absExact(long value) {
		if (value != Long.MIN_VALUE) return Math.abs(value);
		throw new ArithmeticException("long overflow");
	}

	/**
	 * Rounds a double value to an int. Throws an ArithmeticException if the value overflows.
	 */
	public static int intRoundExact(double value) {
		return Math.toIntExact(Math.round(value));
	}

	/**
	 * Rounds up the division of two values.
	 */
	public static int ceilDiv(int x, int y) {
		int r = x / y;
		if ((x ^ y) >= 0 && (r * y != x)) r++; // round up if same signs and modulo not zero
		return r;
	}

	/**
	 * Rounds up the division of two values.
	 */
	public static long ceilDiv(long x, long y) {
		long r = x / y;
		if ((x ^ y) >= 0 && (r * y != x)) r++; // round up if same signs and modulo not zero
		return r;
	}

	/**
	 * Rounds the division of two values.
	 */
	public static int roundDiv(int x, int y) {
		if (y == 0) throw new ArithmeticException("/ by zero");
		return (int) Math.round((double) x / y);
	}

	/**
	 * Rounds the division of two values.
	 */
	public static long roundDiv(long x, long y) {
		if (y == 0) throw new ArithmeticException("/ by zero");
		return Math.round((double) x / y);
	}

	/**
	 * Rounds a value to the given number of decimal places. Infinity and NaN values are returned
	 * without change.
	 */
	public static double round(int places, double value) {
		if (places < 0) throw new IllegalArgumentException("Places must be >= 0: " + places);
		if (Double.isInfinite(value) || Double.isNaN(value)) return value;
		// BigDecimal double constructor is unpredictable (see javadoc)
		return new BigDecimal(String.valueOf(value)).setScale(places, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * Rounds a value to the given number of decimal places. Supports up to 10 places, and does not
	 * round very large or small values. However, this method is more efficient than round(double,
	 * int).
	 */
	public static double simpleRound(int places, double value) {
		if (Double.isNaN(value)) return Double.NaN;
		if (places < 0 || places > MAX_ROUND_PLACES) throw new IllegalArgumentException(
			"places must be 0.." + MAX_ROUND_PLACES + "): " + places);
		if (value > MAX_ROUND || value < -MAX_ROUND) return value;
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Determines are equal within given precision.
	 */
	public static boolean approxEqual(double lhs, double rhs, double precision) {
		return lhs == rhs || Math.abs(lhs - rhs) <= precision;
	}

	/**
	 * Returns true if the sum of the two values has overflowed.
	 */
	public static boolean overflow(int sum, int l, int r) {
		return ((l ^ sum) & (r ^ sum)) < 0;
	}

	/**
	 * Returns true if the sum of the two values has overflowed.
	 */
	public static boolean overflow(long sum, long l, long r) {
		return ((l ^ sum) & (r ^ sum)) < 0;
	}

	/**
	 * Generates a pseudo-random number from min to max inclusive.
	 */
	public static int random(int min, int max) {
		if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE)
			return ThreadLocalRandom.current().nextInt();
		if (max == Integer.MAX_VALUE) return ThreadLocalRandom.current().nextInt(min - 1, max) + 1;
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	/**
	 * Generates a pseudo-random number from min to max inclusive.
	 */
	public static long random(long min, long max) {
		if (min == Long.MIN_VALUE && max == Long.MAX_VALUE)
			return ThreadLocalRandom.current().nextLong();
		if (max == Long.MAX_VALUE) return ThreadLocalRandom.current().nextLong(min - 1, max) + 1;
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}

	/**
	 * Generates a pseudo-random number from 0 (inclusive) to 1 (exclusive).
	 */
	public static double random() {
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * Generates a pseudo-random number from min (inclusive) to max (exclusive).
	 */
	public static double random(double min, double maxExclusive) {
		return ThreadLocalRandom.current().nextDouble(min, maxExclusive);
	}

	/**
	 * Returns the percentage for a value 0..1. Returns NaN for a zero range.
	 */
	public static double toPercent(double value) {
		return toPercent(value, 1.0);
	}

	/**
	 * Returns the percentage for a value in a range. Returns NaN for a zero range.
	 */
	public static double toPercent(double value, double range) {
		if (range == 0) return Double.NaN;
		return (value / range) * 100;
	}

	/**
	 * Returns the value for a percentage of a 0..1.
	 */
	public static double fromPercent(double percentage) {
		return fromPercent(percentage, 1.0);
	}

	/**
	 * Returns the value for a percentage of a range.
	 */
	public static double fromPercent(double percentage, double range) {
		return (percentage / 100) * range;
	}

	/**
	 * Calculates the greatest common divisor of two numbers.
	 */
	public static int gcd(int lhs, int rhs) {
		return Math.toIntExact(gcd((long) lhs, (long) rhs));
	}

	/**
	 * Calculates the greatest common divisor of two numbers.
	 */
	public static long gcd(long lhs, long rhs) {
		if (lhs == rhs) return absExact(lhs);
		if (lhs == 0) return absExact(rhs);
		if (rhs == 0) return absExact(lhs);
		if (lhs == 1 || lhs == -1 || rhs == 1 || rhs == -1) return 1;
		if (lhs % rhs == 0) return absExact(rhs);
		if (rhs % lhs == 0) return absExact(lhs);
		return gcd(rhs, lhs % rhs);
	}

	/**
	 * Calculates the lowest common multiple of two numbers.
	 */
	public static int lcm(int lhs, int rhs) {
		return Math.toIntExact(lcm((long) lhs, (long) rhs));
	}

	/**
	 * Calculates the lowest common multiple of two numbers.
	 */
	public static long lcm(long lhs, long rhs) {
		if (lhs == rhs) return absExact(lhs);
		if (lhs == 0 || rhs == 0) return 0; // debatable
		if (lhs == 1 || lhs == -1) return absExact(rhs);
		if (rhs == 1 || rhs == -1) return absExact(lhs);
		if (lhs % rhs == 0) return absExact(lhs);
		if (rhs % lhs == 0) return absExact(rhs);
		return absExact(Math.multiplyExact(lhs / gcd(lhs, rhs), rhs));
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(int... array) {
		return mean(array, 0);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(int[] array, int offset) {
		return mean(array, offset, array.length - offset);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(int[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		long sum = 0;
		for (int i = 0; i < length; i++, offset++)
			sum += array[offset];
		return (double) sum / length;
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(long... array) {
		return mean(array, 0);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(long[] array, int offset) {
		return mean(array, offset, array.length - offset);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(long[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		long div = 0;
		long rem = 0;
		for (int i = 0; i < length; i++, offset++) {
			div += array[offset] / length;
			rem += array[offset] % length; // cannot overflow long
		}
		return div + (double) rem / length;
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static float mean(float... array) {
		return mean(array, 0);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static float mean(float[] array, int offset) {
		return mean(array, offset, array.length - offset);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static float mean(float[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		float sum = 0;
		for (int i = 0; i < length; i++, offset++)
			sum += array[offset];
		return sum / length;
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(double... array) {
		return mean(array, 0);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(double[] array, int offset) {
		return mean(array, offset, array.length - offset);
	}

	/**
	 * Mean value from a primitive array.
	 */
	public static double mean(double[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		double sum = 0;
		for (int i = 0; i < length; i++, offset++)
			sum += array[offset];
		return sum / length;
	}

	/**
	 * Calculates the median value from an array of primitives. The array will be sorted; if this is
	 * not desired, pass in a copy of the array instead.
	 */
	public static double median(int... values) {
		return median(values, 0);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(int[] values, int offset) {
		return median(values, offset, values.length - offset);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(int[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		Arrays.sort(array, offset, offset + length);
		int i = offset + (length >>> 1);
		return length % 2 == 1 ? array[i] : mean(array[i - 1], array[i]);
	}

	/**
	 * Calculates the median value from an array of primitives. The array will be sorted; if this is
	 * not desired, pass in a copy of the array instead.
	 */
	public static double median(long... values) {
		return median(values, 0);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(long[] values, int offset) {
		return median(values, offset, values.length - offset);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(long[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		Arrays.sort(array, offset, offset + length);
		int i = offset + (length >>> 1);
		return length % 2 == 1 ? array[i] : mean(array[i - 1], array[i]);
	}

	/**
	 * Calculates the median value from an array of primitives. The array will be sorted; if this is
	 * not desired, pass in a copy of the array instead.
	 */
	public static float median(float... values) {
		return median(values, 0);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static float median(float[] values, int offset) {
		return median(values, offset, values.length - offset);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static float median(float[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		Arrays.sort(array, offset, offset + length);
		int i = offset + (length >>> 1);
		return length % 2 == 1 ? array[i] : mean(array[i - 1], array[i]);
	}

	/**
	 * Calculates the median value from an array of primitives. The array will be sorted; if this is
	 * not desired, pass in a copy of the array instead.
	 */
	public static double median(double... values) {
		return median(values, 0);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(double[] values, int offset) {
		return median(values, offset, values.length - offset);
	}

	/**
	 * Calculates the median value from an array of primitives. The array slice will be sorted; if
	 * this is not desired, pass in a copy of the array instead.
	 */
	public static double median(double[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		if (length == 1) return array[offset];
		Arrays.sort(array, offset, offset + length);
		int i = offset + (length >>> 1);
		return length % 2 == 1 ? array[i] : mean(array[i - 1], array[i]);
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static int limit(int value, int min, int max) {
		validateMin(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static long limit(long value, long min, long max) {
		validateMin(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static float limit(float value, float min, float max) {
		validateMin(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static double limit(double value, double min, double max) {
		validateMin(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static int periodicLimit(int value, int period, Bound.Type type) {
		Objects.requireNonNull(type);
		validateMin(period, 1);
		while (value > period)
			value -= period;
		if (type == exclusive && value == period) value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static long periodicLimit(long value, long period, Bound.Type type) {
		Objects.requireNonNull(type);
		validateMin(period, 1);
		while (value > period)
			value -= period;
		if (type == exclusive && value == period) value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static float periodicLimit(float value, float period, Bound.Type type) {
		Objects.requireNonNull(type);
		validateMin(period, 0.0, exclusive);
		while (value > period)
			value -= period;
		if (type == exclusive && value == period) value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive). Period must be >
	 * 0.
	 */
	public static double periodicLimit(double value, double period, Bound.Type type) {
		Objects.requireNonNull(type);
		validateMin(period, 0.0, exclusive);
		while (value > period)
			value -= period;
		if (type == exclusive && value == period) value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static byte min(byte... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static byte min(byte[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static byte min(byte[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		byte min = Byte.MAX_VALUE;
		for (; length > 0; length--, offset++)
			min = min <= array[offset] ? min : array[offset];
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static short min(short... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static short min(short[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static short min(short[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		short min = Short.MAX_VALUE;
		for (; length > 0; length--, offset++)
			min = min <= array[offset] ? min : array[offset];
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static int min(int... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static int min(int[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static int min(int[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		int min = Integer.MAX_VALUE;
		for (; length > 0; length--, offset++)
			min = Integer.min(min, array[offset]);
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static long min(long... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static long min(long[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static long min(long[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		long min = Long.MAX_VALUE;
		for (; length > 0; length--, offset++)
			min = Long.min(min, array[offset]);
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static float min(float... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static float min(float[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static float min(float[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		float min = Float.POSITIVE_INFINITY;
		for (; length > 0; length--, offset++)
			min = Float.min(min, array[offset]);
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static double min(double... array) {
		return min(array, 0);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static double min(double[] array, int offset) {
		return min(array, offset, array.length - offset);
	}

	/**
	 * Returns the minimum value in the primitive array; throws exception if no values.
	 */
	public static double min(double[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		double min = Double.POSITIVE_INFINITY;
		for (; length > 0; length--, offset++)
			min = Double.min(min, array[offset]);
		return min;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static byte max(byte[] array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static byte max(byte[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static byte max(byte[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		byte max = Byte.MIN_VALUE;
		for (; length > 0; length--, offset++)
			max = max >= array[offset] ? max : array[offset];
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static short max(short[] array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static short max(short[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static short max(short[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		short max = Short.MIN_VALUE;
		for (; length > 0; length--, offset++)
			max = max >= array[offset] ? max : array[offset];
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static int max(int... array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static int max(int[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static int max(int[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		int max = Integer.MIN_VALUE;
		for (; length > 0; length--, offset++)
			max = Integer.max(max, array[offset]);
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static long max(long... array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static long max(long[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static long max(long[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		long max = Long.MIN_VALUE;
		for (; length > 0; length--, offset++)
			max = Long.max(max, array[offset]);
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static float max(float... array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static float max(float[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static float max(float[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		float max = Float.NEGATIVE_INFINITY;
		for (; length > 0; length--, offset++)
			max = Float.max(max, array[offset]);
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static double max(double... array) {
		return max(array, 0);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static double max(double[] array, int offset) {
		return max(array, offset, array.length - offset);
	}

	/**
	 * Returns the maximum value in the primitive array; throws exception if no values.
	 */
	public static double max(double[] array, int offset, int length) {
		Objects.requireNonNull(array);
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMin(length, 1);
		double max = Double.NEGATIVE_INFINITY;
		for (; length > 0; length--, offset++)
			max = Double.max(max, array[offset]);
		return max;
	}

}
