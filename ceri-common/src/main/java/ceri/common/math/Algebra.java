package ceri.common.math;

import ceri.common.array.Array;

public class Algebra {

	private Algebra() {}

	/**
	 * Factorial value (value!). Double is used as values may be larger than long can handle.
	 */
	public static double factorial(long value) {
		if (value < 0) throw new IllegalArgumentException(value + "! not defined.");
		if (value == 0) return 1;
		return value * factorial(value - 1);
	}

	/**
	 * Factorial value (value!). Checks and throws exception if factorial value is too big for long
	 * type. Values 0-20 are long-compatible.
	 */
	public static long longFactorial(long value) {
		double factorial = factorial(value);
		if (factorial > Long.MAX_VALUE) throw new IllegalArgumentException(
			value + "! is larger than long type allows: " + factorial);
		return (long) factorial;
	}

	/**
	 * Returns the value at given index on given level of Pascal's triangle. Defined as: c!/r!(c-r)!
	 * where level = c, index = r.
	 */
	public static long pascal(long level, long index) {
		if (index < 0 || index > level) return 0;
		return (long) (factorial(level, index) / factorial(index));
	}

	/**
	 * Finds real roots of ax^2 + bx + c = 0
	 */
	public static double[] quadraticRealRoots(double a, double b, double c) {
		if (a == 0.0 && b == 0.0) return Array.DOUBLE.empty;
		if (a == 0.0) return new double[] { -c / b };
		double d = (b * b) - (4 * a * c);
		if (d < 0) return Array.DOUBLE.empty;
		double sqrtd = Math.sqrt(d);
		double x1 = (-b + sqrtd) / (2 * a);
		double x2 = (-b - sqrtd) / (2 * a);
		return new double[] { fixNegativeZero(x1), fixNegativeZero(x2) };
	}

	/**
	 * Finds real roots of x for x^3 + a2.x^2 + a1.x + a0 = 0. Uses formulae from
	 * http://mathworld.wolfram.com/CubicFormula.html
	 */
	public static double[] cubicRealRoots(double a2, double a1, double a0) {
		double Q = ((3 * a1) - (a2 * a2)) / 9;
		double R = ((9 * a2 * a1) - (27 * a0) - (2 * a2 * a2 * a2)) / 54;
		double D = (Q * Q * Q) + (R * R);
		if (D < 0.0) return cubicUniqueRealRoots(a2, Q, R);
		if (D > 0.0) return cubicSingleRealRoot(a2, R, D);
		return cubicNonUniqueRealRoots(a2, R);
	}

	private static double[] cubicNonUniqueRealRoots(double a2, double R) {
		double cbrtR = Math.cbrt(R);
		double x1 = (2 * cbrtR) - (a2 / 3);
		double x2 = -cbrtR - (a2 / 3);
		return new double[] { fixNegativeZero(x1), fixNegativeZero(x2), fixNegativeZero(x2) };
	}

	private static double[] cubicSingleRealRoot(double a2, double R, double D) {
		double sqrtD = Math.sqrt(D);
		double x1 = Math.cbrt(R + sqrtD) + Math.cbrt(R - sqrtD) - (a2 / 3);
		return new double[] { fixNegativeZero(x1) };
	}

	private static double[] cubicUniqueRealRoots(double a2, double Q, double R) {
		double sqrtQ = Math.sqrt(-Q);
		double theta = Math.acos(R / (sqrtQ * sqrtQ * sqrtQ));
		double x1 = (2 * sqrtQ * Math.cos(theta / 3)) - (a2 / 3);
		double x2 = (2 * sqrtQ * Math.cos((theta + (2 * Math.PI)) / 3)) - (a2 / 3);
		double x3 = (2 * sqrtQ * Math.cos((theta + (4 * Math.PI)) / 3)) - (a2 / 3);
		return new double[] { fixNegativeZero(x1), fixNegativeZero(x2), fixNegativeZero(x3) };
	}

	private static double fixNegativeZero(double d) {
		if (d == -0.0) return 0.0;
		return d;
	}

	/**
	 * c!/(c-r)! where value = c, count = r.
	 */
	private static double factorial(long value, long count) {
		if (value == 0 || count == 0) return 1;
		return value * factorial(value - 1, count - 1);
	}

}
