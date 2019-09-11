package ceri.common.math;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class MathUtil {
	public static final double PIx2 = Math.PI * 2; // common calculation
	public static final double PI_BY_2 = Math.PI / 2; // common calculation
	private static final char ZERO = '0';
	private static final int BASE10 = 10;
	private static final int MAX_ROUND_PLACES = 10;
	private static final double MAX_ROUND = 1000000000.0;

	private MathUtil() {}

	public static int toInt(boolean b) {
		return b ? 1 : 0;
	}

	public static double average(double... values) {
		if (values.length == 0) return Double.NaN;
		if (values.length == 1) return values[0];
		return DoubleStream.of(values).average().orElse(Double.NaN);
	}

	public static int averageInt(int... values) {
		return (int) Math.round(average(values));
	}

	public static double average(int... values) {
		if (values.length == 0) return Double.NaN;
		if (values.length == 1) return values[0];
		return IntStream.of(values).average().orElse(Double.NaN);
	}

	public static long averageLong(long... values) {
		return Math.round(average(values));
	}

	public static double average(long... values) {
		if (values.length == 0) return Double.NaN;
		if (values.length == 1) return values[0];
		return LongStream.of(values).average().orElse(Double.NaN);
	}

	public static boolean approxEqual(double lhs, double rhs, double precision) {
		return lhs == rhs || Math.abs(lhs - rhs) <= precision;
	}

	public static long safeToLong(double value) {
		if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) return (long) value;
		throw new ArithmeticException("long overflow");
	}

	public static int safeToInt(double value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) return (int) value;
		throw new ArithmeticException("int overflow");
	}

	public static short toShortExact(long value) {
		if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) return (short) value;
		throw new ArithmeticException("short overflow");
	}

	public static byte toByteExact(long value) {
		if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) return (byte) value;
		throw new ArithmeticException("byte overflow");
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

	public static int signum(long x, long y) {
		if (x == 0 || y == 0) return 0;
		return (x > 0 == y > 0) ? 1 : -1;
	}

	/**
	 * Return absolute value. Throws ArithmeticException if overflow.
	 */
	public static long absExact(long value) {
		if (value != Long.MIN_VALUE) return Math.abs(value);
		throw new ArithmeticException("long overflow");
	}

	/**
	 * Return absolute value. Throws ArithmeticException if overflow.
	 */
	public static int absExact(int value) {
		if (value != Integer.MIN_VALUE) return Math.abs(value);
		throw new ArithmeticException("int overflow");
	}

	/**
	 * Generates a pseudo-random number from min to max inclusive.
	 */
	public static int randomInt(int min, int max) {
		return (int) random(min, max);
	}

	/**
	 * Generates a pseudo-random number from min to max inclusive.
	 */
	public static long random(long min, long max) {
		if (min == max) return min;
		return Math.round((Math.random() * (max - min)) + min);
	}

	/**
	 * Divides two numbers, rounding any remainder away from 0.
	 */
	public static long divideUp(long x, long y) {
		return Math.addExact(x / y, x % y == 0 ? 0 : signum(x, y));
	}

	/**
	 * Divides two numbers, rounding any remainder away from 0.
	 */
	public static int divideUp(int x, int y) {
		return Math.addExact(x / y, x % y == 0 ? 0 : 1);
	}

	/**
	 * Rounds the division of two long values.
	 */
	public static long roundDiv(long x, long y) {
		return Math.round(((double) x) / y);
	}

	/**
	 * Rounds a double value to an int. Throws an ArithmeticException if the value overflows.
	 */
	public static int intRoundExact(double value) {
		return Math.toIntExact(Math.round(value));
	}

	/**
	 * Rounds an array of values to the given number of decimal places. Too inaccurate for very
	 * large or small values.
	 */
	public static double[] simpleRound(int places, double... values) {
		return DoubleStream.of(values).map(d -> simpleRound(d, places)).toArray();
	}

	/**
	 * Rounds a value to the given number of decimal places. Too inaccurate for very large or small
	 * values.
	 */
	public static double simpleRound(double value, int places) {
		if (Double.isNaN(value)) return Double.NaN;
		if (places > MAX_ROUND_PLACES) throw new IllegalArgumentException(
			"places must be <= " + MAX_ROUND_PLACES + ": " + places);
		if (value > MAX_ROUND || value < -MAX_ROUND)
			throw new IllegalArgumentException("value magnitude must be <= " + MAX_ROUND);
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Convert a short value to an array of 2 bytes.
	 */
	public static byte[] shortToBytes(int i) {
		short s = (short) i;
		return new byte[] { (byte) (s >> 8), (byte) s };
	}

	/**
	 * Convert an int value to an array of 4 bytes.
	 */
	public static byte[] intToBytes(int i) {
		return new byte[] { (byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i };
	}

	/**
	 * Mean value from an array of doubles.
	 */
	public static double mean(double... values) {
		if (values.length == 0) throw new IllegalArgumentException("No values specified");
		if (values.length == 1) return values[0];
		double d = 0;
		int i = 0;
		while (i < values.length)
			d += values[i++];
		return d / i;
	}

	/**
	 * Median value from an array of doubles.
	 */
	public static double median(double... values) {
		if (values.length == 0) throw new IllegalArgumentException("No values specified");
		if (values.length == 1) return values[0];
		double[] sortedValues = Arrays.copyOf(values, values.length);
		Arrays.sort(sortedValues);
		int midIndex = values.length / 2;
		if (values.length % 2 == 1) return sortedValues[midIndex];
		return (sortedValues[midIndex - 1] + sortedValues[midIndex]) / 2.0;
	}

	/**
	 * Converts a long value into an array of digits in base 10.
	 */
	public static byte[] digits(long value) {
		return digits(value, BASE10);
	}

	/**
	 * Converts a long value into an array of digits in given radix. If radix is outside of
	 * Character.MIN_RADIX and Character.MAX_RADIX then 10 is used.
	 */
	public static byte[] digits(long value, int radix) {
		String valueStr = Long.toString(value, radix);
		byte[] digits = new byte[valueStr.length()];
		for (int i = 0; i < digits.length; i++) {
			char ch = valueStr.charAt(i);
			digits[i] = (byte) (ch >= 'a' ? ch - 'a' + 10 : ch - ZERO);
		}
		return digits;
	}

	/**
	 * Comparison of two integers, 1 (lhs > rhs), 0 (lhs = rhs), -1 (lhs < rhs)
	 */
	public static int compare(int lhs, int rhs) {
		if (lhs == rhs) return 0;
		return (lhs > rhs ? 1 : -1);
	}

	/**
	 * Returns the percentage for a value 0..1. Returns NaN for a zero range.
	 */
	public static double percentage(double value) {
		return percentage(value, 1.0);
	}

	/**
	 * Returns the percentage for a value in a range. Returns NaN for a zero range.
	 */
	public static double percentage(double value, double range) {
		if (range == 0) return Double.NaN;
		// Optimization for large range values
		if (range > 100) return (value / range) * 100;
		return (value * 100) / range;
	}

	/**
	 * Returns the value for a percentage of a 0..1.
	 */
	public static double valueFromPercentage(double percentage) {
		return valueFromPercentage(percentage, 1.0);
	}

	/**
	 * Returns the value for a percentage of a range.
	 */
	public static double valueFromPercentage(double percentage, double range) {
		// Optimization for large range values
		if (range > 100) return percentage * (range / 100);
		// Optimization for large percentage values
		if (percentage > 100) return (percentage / 100) * range;
		return (percentage * range) / 100;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static byte[] increment(byte[] array, byte amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static short[] increment(short[] array, short amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static int[] increment(int[] array, int amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static long[] increment(long[] array, long amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static float[] increment(float[] array, float amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Increments all values in the primitive array. If the amount is negative the values will be
	 * decremented.
	 */
	public static double[] increment(double[] array, double amount) {
		if (amount != 0) for (int i = 0; i < array.length; i++)
			array[i] += amount;
		return array;
	}

	/**
	 * Limits the value to be within the min and max.
	 */
	public static float limit(float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max.
	 */
	public static double limit(double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max.
	 */
	public static int limit(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max.
	 */
	public static long limit(long value, long min, long max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive).
	 */
	public static float periodicLimit(float value, float period) {
		while (value > period)
			value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive).
	 */
	public static double periodicLimit(double value, double period) {
		while (value > period)
			value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period.
	 */
	public static long periodicLimit(long value, long period) {
		while (value > period)
			value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (exclusive).
	 */
	public static int periodicLimitEx(int value, int period) {
		while (value >= period)
			value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (exclusive).
	 */
	public static long periodicLimitEx(long value, long period) {
		while (value >= period)
			value -= period;
		while (value < 0)
			value += period;
		return value;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static byte min(byte... array) {
		if (array == null || array.length == 0) return 0;
		byte min = Byte.MAX_VALUE;
		for (byte val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static short min(short... array) {
		if (array == null || array.length == 0) return 0;
		short min = Short.MAX_VALUE;
		for (short val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static int min(int... array) {
		if (array == null || array.length == 0) return 0;
		int min = Integer.MAX_VALUE;
		for (int val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static long min(long... array) {
		if (array == null || array.length == 0) return 0;
		long min = Long.MAX_VALUE;
		for (long val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static float min(float... array) {
		if (array == null || array.length == 0) return 0;
		float min = Float.MAX_VALUE;
		for (float val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the minimum value in the primitive array, or 0 if the array has no values.
	 */
	public static double min(double... array) {
		if (array == null || array.length == 0) return 0;
		double min = Double.MAX_VALUE;
		for (double val : array)
			if (min > val) min = val;
		return min;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static byte max(byte... array) {
		if (array == null || array.length == 0) return 0;
		byte max = Byte.MIN_VALUE;
		for (byte val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static short max(short... array) {
		if (array == null || array.length == 0) return 0;
		short max = Short.MIN_VALUE;
		for (short val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static int max(int... array) {
		if (array == null || array.length == 0) return 0;
		int max = Integer.MIN_VALUE;
		for (int val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static long max(long... array) {
		if (array == null || array.length == 0) return 0;
		long max = Long.MIN_VALUE;
		for (long val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static float max(float... array) {
		if (array == null || array.length == 0) return 0;
		float max = Float.MIN_VALUE;
		for (float val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the maximum value in the primitive array, or 0 if the array has no values.
	 */
	public static double max(double... array) {
		if (array == null || array.length == 0) return 0;
		double max = -Double.MAX_VALUE;
		for (double val : array)
			if (max < val) max = val;
		return max;
	}

	/**
	 * Returns the short value of an unsigned byte.
	 */
	public static short unsignedByte(byte b) {
		return (short) ((0x100 + b) & 0xff);
	}

	/**
	 * Returns the int value of an unsigned short.
	 */
	public static int unsignedShort(short b) {
		return (0x10000 + b) & 0xffff;
	}

	/**
	 * Returns the long value of an unsigned int.
	 */
	public static long unsignedInt(int i) {
		return (0x100000000L + i) & 0xffffffffL;
	}

}
