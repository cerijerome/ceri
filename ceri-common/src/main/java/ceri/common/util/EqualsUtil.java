package ceri.common.util;

public class EqualsUtil {

	private EqualsUtil() {}

	/**
	 * Checks if float values are equal.
	 */
	public static boolean equals(float o1, float o2) {
		return Float.floatToIntBits(o1) == Float.floatToIntBits(o2);
	}

	/**
	 * Checks if double values are equal.
	 */
	public static boolean equals(double o1, double o2) {
		return Double.doubleToLongBits(o1) == Double.doubleToLongBits(o2);
	}

	/**
	 * Checks if objects are equal.
	 */
	public static boolean equals(Object o1, Object o2) {
		if (o1 == o2) return true;
		if (o1 == null || o2 == null) return false;
		if (o1 instanceof Float && o2 instanceof Float) return equals(((Float) o1).floatValue(),
			((Float) o2).floatValue());
		if (o1 instanceof Double && o2 instanceof Double) return equals(
			((Double) o1).doubleValue(), ((Double) o2).doubleValue());
		return o1.equals(o2);
	}

	/**
	 * Checks if string representations of objects are equal. Returns false if one is null. Does not
	 * trim strings before checking equality.
	 */
	public static boolean stringEquals(Object lhs, Object rhs) {
		if (lhs == rhs) return true;
		if (lhs == null || rhs == null) return false;
		return String.valueOf(lhs).equals(String.valueOf(rhs));
	}

}
