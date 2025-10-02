package ceri.common.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import ceri.common.collect.Iterators;
import ceri.common.function.Functions;
import ceri.common.util.Validate;

public class Maths {
	private static Map<Class<?>, Functions.LongOperator> UNSIGNED_OPS =
		Map.of(Byte.class, Maths::ubyte, Short.class, Maths::ushort, Integer.class, Maths::uint);
	private static final long ZERO10 = 10000000000L;
	// PI approximations: 22/7, 355/113
	public static final double PI_BY_2 = Math.PI / 2; // common calculation
	public static final double PI_3_BY_2 = Math.PI * 3 / 2; // common calculation
	private static final int MAX_ROUND_PLACES = 10;
	private static final double MAX_ROUND = 1000000000.0;
	public static final int MAX_UBYTE = 0xff;
	public static final int MAX_USHORT = 0xffff;
	public static final long MAX_UINT = 0xffffffffL;
	public static final int TURN_DEG = 360;
	public static final int HALF_TURN_DEG = 180;
	public static final int QUARTER_TURN_DEG = 90;
	private static final long LONG_SIN_MULTIPLIER = 100;
	private static final long LONG_SIN_FULL = TURN_DEG * LONG_SIN_MULTIPLIER;
	private static final long LONG_SIN_HALF = LONG_SIN_FULL >> 1;
	private static final long LONG_SIN_QUARTER = LONG_SIN_HALF >> 1;
	private static final long LONG_SIN_MAGIC = 40500L * LONG_SIN_MULTIPLIER * LONG_SIN_MULTIPLIER;

	private Maths() {}

	/**
	 * Returns the number of decimal digits, not including sign.
	 */
	public static int decimalDigits(long n) {
		if (n == Long.MIN_VALUE) return decimalDigits(Long.MAX_VALUE);
		if (n < 0) return decimalDigits(-n);
		if (n >= ZERO10) return 10 + decimalDigits(n / ZERO10);
		return (int) Math.round(Math.log10(n) + 0.5); // not accurate for high n
	}

	/**
	 * Returns the approximate sine value of pi * n/d radians multiplied by the given max.
	 */
	public static int intSinFromRatio(int n, int d, int max) {
		Validate.min(d, 1);
		return (int) longSin(roundDiv(n * LONG_SIN_HALF, d), max);
	}

	/**
	 * Returns the approximate cosine value of pi * n/d radians multiplied by the given max.
	 */
	public static int intCosFromRatio(int n, int d, int max) {
		Validate.min(d, 1);
		return (int) longSin(roundDiv(n * LONG_SIN_HALF, d) + LONG_SIN_QUARTER, max);
	}

	/**
	 * Returns the approximate sine value of x degrees, multiplied by the given max.
	 */
	public static int intSin(int degrees, int max) {
		return (int) longSin(degrees * LONG_SIN_MULTIPLIER, max);
	}

	/**
	 * Returns the approximate sine value of d in partial degrees, multiplied by the given max.
	 * Based on sine approximation formula, which has a relative error of 1.8%.
	 */
	private static long longSin(long d, int max) {
		d %= LONG_SIN_FULL;
		if (d < 0) d += LONG_SIN_FULL;
		if (d >= LONG_SIN_HALF) return -longSin(d - LONG_SIN_HALF, max);
		return roundDiv(max * (d << 2) * (LONG_SIN_HALF - d),
			LONG_SIN_MAGIC - (d * (LONG_SIN_HALF - d)));
	}

	/**
	 * Returns the approximate cosine value of x degrees, multiplied by the given max.
	 */
	public static int intCos(int degrees, int max) {
		return intSin(degrees + QUARTER_TURN_DEG, max);
	}

	/**
	 * Calculates the polynomial sum for coefficients of powers of x in ascending order from 0.
	 */
	public static double polynomial(double x, double... coefficients) {
		if (coefficients.length == 0) return 0;
		double sum = coefficients[0];
		double m = 1;
		for (int i = 1; i < coefficients.length; i++) {
			m *= x;
			sum += coefficients[i] * m;
		}
		return sum;
	}

	/**
	 * Calculates the polynomial sum for coefficients of powers of x in ascending order from 0.
	 */
	public static long polynomial(long x, long... coefficients) {
		if (coefficients.length == 0) return 0;
		long sum = coefficients[0];
		long m = 1;
		for (int i = 1; i < coefficients.length; i++) {
			m = Math.multiplyExact(m, x);
			sum = Math.addExact(sum, Math.multiplyExact(coefficients[i], m));
		}
		return sum;
	}

	/**
	 * Base-2 log of an unsigned integer. Returns -1 for 0.
	 */
	public static int ulog2(int n) {
		return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(n);
	}

	/**
	 * Determines int value from boolean.
	 */
	public static int toInt(boolean b) {
		return b ? 1 : 0;
	}

	/**
	 * Returns the absolute value, or max if the value overflows.
	 */
	public static int absLimit(int a) {
		return a == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(a);
	}

	/**
	 * Returns the absolute value, or max if the value overflows.
	 */
	public static long absLimit(long a) {
		return a == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(a);
	}

	/**
	 * Add values, or return the max/min if operation overflows.
	 */
	public static int addLimit(int x, int y) {
		int r = x + y;
		if (((x ^ r) & (y ^ r)) >= 0) return r; // no overflow
		return x < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	/**
	 * Add values, or return the max/min if operation overflows.
	 */
	public static long addLimit(long x, long y) {
		long r = x + y;
		if (((x ^ r) & (y ^ r)) >= 0) return r; // no overflow
		return x < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
	}

	/**
	 * Subtract values, or return the max/min if operation overflows.
	 */
	public static int subtractLimit(int x, int y) {
		int r = x - y;
		if (((x ^ y) & (x ^ r)) >= 0) return r; // no overflow
		return x < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	/**
	 * Subtract values, or return the max/min if operation overflows.
	 */
	public static long subtractLimit(long x, long y) {
		long r = x - y;
		if (((x ^ y) & (x ^ r)) >= 0) return r; // no overflow
		return x < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
	}

	/**
	 * Multiply values, or return the max/min if operation overflows.
	 */
	public static int multiplyLimit(int x, int y) {
		long r = (long) x * (long) y;
		if ((int) r == r) return (int) r;
		return r < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	/**
	 * Multiply values, or return the max/min if operation overflows.
	 */
	public static long multiplyLimit(long x, int y) {
		return multiplyLimit(x, (long) y);
	}

	/**
	 * Multiply values, or return the max/min if operation overflows.
	 */
	public static long multiplyLimit(long x, long y) {
		long r = x * y;
		long ax = Math.abs(x);
		long ay = Math.abs(y);
		if (((ax | ay) >>> 31 == 0) || ((y == 0 || r / y == x) && (x != Long.MIN_VALUE || y != -1)))
			return r;
		return Long.signum(x) != Long.signum(y) ? Long.MIN_VALUE : Long.MAX_VALUE;
	}

	/**
	 * Decrement value by 1 unless an overflow would occur.
	 */
	public static int decrementLimit(int a) {
		return a == Integer.MIN_VALUE ? a : a - 1;
	}

	/**
	 * Decrement value by 1 unless an overflow would occur.
	 */
	public static long decrementLimit(long a) {
		return a == Long.MIN_VALUE ? a : a - 1L;
	}

	/**
	 * Increment value by 1 unless an overflow would occur.
	 */
	public static int incrementLimit(int a) {
		return a == Integer.MAX_VALUE ? a : a + 1;
	}

	/**
	 * Increment value by 1 unless an overflow would occur.
	 */
	public static long incrementLimit(long a) {
		return a == Long.MAX_VALUE ? a : a + 1L;
	}

	/**
	 * Negate value, or return the max if min value would overflow.
	 */
	public static int negateLimit(int a) {
		return a == Integer.MIN_VALUE ? Integer.MAX_VALUE : -a;
	}

	/**
	 * Negate value, or return the max if min value would overflow.
	 */
	public static long negateLimit(long a) {
		return a == Long.MIN_VALUE ? Long.MAX_VALUE : -a;
	}

	/**
	 * Cast the value, or return the max/min if outside the range.
	 */
	public static int toIntLimit(long value) {
		if ((int) value == value) return (int) value;
		return value < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	/**
	 * Cast the value, or return the max/min if outside the range.
	 */
	public static int toIntLimit(double value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) return (int) value;
		return value < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	/**
	 * Cast the value, or return the max/min if outside the range.
	 */
	public static long toLongLimit(double value) {
		if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) return (long) value;
		return value < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
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
	 * Return the unsigned value based on type; long values are unchanged.
	 */
	public static Long unsigned(Number n) {
		if (n == null) return null;
		var unsignedOp = UNSIGNED_OPS.get(n.getClass());
		if (unsignedOp == null) return n.longValue();
		return unsignedOp.applyAsLong(n.longValue());
	}

	/**
	 * Rounds a double value to an int.
	 */
	public static int intRound(double value) {
		return (int) limit(Math.round(value), Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Rounds a double value to an int. Throws an ArithmeticException if the value overflows.
	 */
	public static int intRoundExact(double value) {
		return Math.toIntExact(Math.round(value));
	}

	/**
	 * Rounds the value to the nearest long. Throws an ArithmeticException if the value overflows.
	 */
	public static long safeRound(double value) {
		if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) return Math.round(value);
		throw new ArithmeticException("long overflow");
	}

	/**
	 * Rounds the value to the nearest int. Throws an ArithmeticException if the value overflows.
	 */
	public static int safeRoundInt(double value) {
		return Math.toIntExact(safeRound(value));
	}

	/**
	 * Rounds the division of two values.
	 */
	public static int roundDiv(int x, int y) {
		if (y == 0) throw new ArithmeticException("/ by zero");
		int div = x / y;
		int r = x % y;
		if (r == 0) return div;
		if (y > 0) {
			if (x > 0) {
				if (r > ((y - 1) >> 1)) div++;
			} else {
				if (r < -(y >> 1)) div--;
			}
		} else {
			if (x > 0) {
				if (r > -((y + 1) >> 1)) div--;
			} else {
				if (r < ((y >> 1) + 1)) div++;
			}
		}
		return div;
	}

	/**
	 * Rounds the division of two values.
	 */
	public static long roundDiv(long x, long y) {
		if (y == 0) throw new ArithmeticException("/ by zero");
		long div = x / y;
		long r = x % y;
		if (r == 0) return div;
		if (y > 0) {
			if (x > 0) {
				if (r > ((y - 1) >> 1)) div++;
			} else {
				if (r < -(y >> 1)) div--;
			}
		} else {
			if (x > 0) {
				if (r > -((y + 1) >> 1)) div--;
			} else {
				if (r < ((y >> 1) + 1)) div++;
			}
		}
		return div;
	}

	/**
	 * Rounds a value to the given number of decimal places. Infinity and NaN values are returned
	 * without change.
	 */
	public static double round(int places, double value) {
		if (Double.isInfinite(value) || Double.isNaN(value)) return value;
		Validate.min(places, 0, "places");
		// BigDecimal double constructor is unpredictable (see javadoc)
		return new BigDecimal(String.valueOf(value)).setScale(places, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * Rounds a value to the given number of decimal places. Supports up to 10 places, and does not
	 * round very large or small values. However, this method is more efficient than round().
	 */
	public static double simpleRound(int places, double value) {
		if (Double.isNaN(value)) return Double.NaN;
		Validate.range(places, 0, MAX_ROUND_PLACES);
		if (value > MAX_ROUND || value < -MAX_ROUND) return value;
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Determines if float values are equal, or if both are NaN.
	 */
	public static boolean equals(float lhs, float rhs) {
		return Float.floatToIntBits(lhs) == Float.floatToIntBits(rhs);
	}

	/**
	 * Determines if double values are equal, or if both are NaN.
	 */
	public static boolean equals(double lhs, double rhs) {
		return Double.doubleToLongBits(lhs) == Double.doubleToLongBits(rhs);
	}

	/**
	 * Determines values are equal within given precision.
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
	 * Generates a pseudo-random number from 0 to max inclusive.
	 */
	public static int random(int max) {
		return random(0, max);
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
	 * Generates a pseudo-random number from 0 to max inclusive.
	 */
	public static long random(long max) {
		return random(0L, max);
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
	 * Generates a pseudo-random number from 0 (inclusive) to max (exclusive).
	 */
	public static double random(double maxExclusive) {
		return random(0.0, maxExclusive);
	}

	/**
	 * Generates a pseudo-random number from min (inclusive) to max (exclusive).
	 */
	public static double random(double min, double maxExclusive) {
		return ThreadLocalRandom.current().nextDouble(min, maxExclusive);
	}

	/**
	 * Selects a pseudo-random object.
	 */
	@SafeVarargs
	public static <T> T random(T... ts) {
		return random(Arrays.asList(ts));
	}

	/**
	 * Selects a pseudo-random object.
	 */
	public static <T> T random(List<T> ts) {
		if (ts.isEmpty()) return null;
		return ts.get(random(0, ts.size() - 1));
	}

	/**
	 * Selects a pseudo-random object.
	 */
	public static <T> T random(Set<T> ts) {
		if (ts.isEmpty()) return null;
		return Iterators.nth(ts.iterator(), random(0, ts.size() - 1));
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
	public static int gcd(int... nums) {
		Validate.min(nums.length, 1);
		int gcd = nums[0];
		for (int i = 1; i < nums.length; i++)
			gcd = Math.toIntExact(calculateGcd(gcd, nums[i]));
		return gcd;
	}

	/**
	 * Calculates the greatest common divisor of given numbers.
	 */
	public static long gcd(long... nums) {
		Validate.min(nums.length, 1);
		long gcd = nums[0];
		for (int i = 1; i < nums.length; i++)
			gcd = calculateGcd(gcd, nums[i]);
		return gcd;
	}

	/**
	 * Calculates the lowest common multiple of given numbers.
	 */
	public static int lcm(int... nums) {
		Validate.min(nums.length, 1);
		int lcm = nums[0];
		for (int i = 1; i < nums.length; i++)
			lcm = Math.toIntExact(calculateLcm(lcm, nums[i]));
		return lcm;
	}

	/**
	 * Calculates the lowest common multiple of given numbers.
	 */
	public static long lcm(long... nums) {
		Validate.min(nums.length, 1);
		long lcm = nums[0];
		for (int i = 1; i < nums.length; i++)
			lcm = calculateLcm(lcm, nums[i]);
		return lcm;
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
		if (length == 1) return array[offset];
		Arrays.sort(array, offset, offset + length);
		int i = offset + (length >>> 1);
		return length % 2 == 1 ? array[i] : mean(array[i - 1], array[i]);
	}

	/**
	 * Returns true if the value is within the inclusive min and max.
	 */
	public static boolean within(int value, int min, int max) {
		return value >= min && value <= max;
	}

	/**
	 * Returns true if the value is within the inclusive min and max.
	 */
	public static boolean within(long value, long min, long max) {
		return value >= min && value <= max;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static int limit(int value, int min, int max) {
		Validate.min(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static long limit(long value, long min, long max) {
		Validate.min(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static float limit(float value, float min, float max) {
		Validate.min(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Limits the value to be within the min and max inclusive.
	 */
	public static double limit(double value, double min, double max) {
		Validate.min(max, min);
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static int periodicLimit(int value, int period, Bound.Type type) {
		Objects.requireNonNull(type);
		Validate.min(period, 1);
		int rem = value % period;
		if (rem < 0) return rem + period;
		if (type == Bound.Type.inc && rem == 0 && value >= period) return period;
		return rem;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static long periodicLimit(long value, long period, Bound.Type type) {
		Objects.requireNonNull(type);
		Validate.min(period, 1);
		long rem = value % period;
		if (rem < 0) return rem + period;
		if (type == Bound.Type.inc && rem == 0 && value >= period) return period;
		return rem;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static float periodicLimit(float value, float period, Bound.Type type) {
		Objects.requireNonNull(type);
		Validate.min(Validate.notEqual(period, 0.0), 0.0);
		if (value == period && type == Bound.Type.inc) return period;
		float rem = value % period;
		if (rem < 0.0f) return rem + period;
		if (type == Bound.Type.inc && rem == 0.0 && value >= period) return period;
		return rem + 0.0f;
	}

	/**
	 * Adjust a periodic value to be within 0 and period (inclusive or exclusive).
	 */
	public static double periodicLimit(double value, double period, Bound.Type type) {
		Objects.requireNonNull(type);
		Validate.min(Validate.notEqual(period, 0.0), 0.0);
		if (value == period && type == Bound.Type.inc) return period;
		double rem = value % period;
		if (rem < 0.0) return rem + period;
		if (type == Bound.Type.inc && rem == 0.0 && value >= period) return period;
		return rem + 0.0;
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
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
		Validate.slice(array.length, offset, length);
		Validate.min(length, 1);
		double max = Double.NEGATIVE_INFINITY;
		for (; length > 0; length--, offset++)
			max = Double.max(max, array[offset]);
		return max;
	}

	// support

	/**
	 * Calculates the greatest common divisor of two numbers.
	 */
	private static long calculateGcd(long lhs, long rhs) {
		if (lhs == rhs) return Math.absExact(lhs);
		if (lhs == 0) return Math.absExact(rhs);
		if (rhs == 0) return Math.absExact(lhs);
		if (lhs == 1 || lhs == -1 || rhs == 1 || rhs == -1) return 1;
		if (lhs % rhs == 0) return Math.absExact(rhs);
		if (rhs % lhs == 0) return Math.absExact(lhs);
		return gcd(rhs, lhs % rhs);
	}

	/**
	 * Calculates the lowest common multiple of two numbers.
	 */
	private static long calculateLcm(long lhs, long rhs) {
		if (lhs == rhs) return Math.absExact(lhs);
		if (lhs == 0 || rhs == 0) return 0; // debatable
		if (lhs == 1 || lhs == -1) return Math.absExact(rhs);
		if (rhs == 1 || rhs == -1) return Math.absExact(lhs);
		if (lhs % rhs == 0) return Math.absExact(lhs);
		if (rhs % lhs == 0) return Math.absExact(rhs);
		return Math.absExact(Math.multiplyExact(lhs / gcd(lhs, rhs), rhs));
	}
}
