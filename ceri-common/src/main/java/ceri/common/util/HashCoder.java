package ceri.common.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import ceri.common.collection.ArrayUtil;

/**
 * Class for construction of strong hash codes.
 */
public class HashCoder {
	private static final int INITIAL_VALUE = 17; // convenient prime
	private static final int MULTIPLIER = 31; // convenient prime
	private static final int INIT = 1;
	private static final int TRUE_HASH = 1231;
	private static final int FALSE_HASH = 1237;
	private int result = INITIAL_VALUE;

	private HashCoder() {}

	public static void main(String[] args) {
		String[] ss = { "hello", null, "there" };
		System.out.println(objHash(ss, 0, 3));
		System.out.println(Arrays.hashCode(ss));
		//long[] a = { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE };
		boolean[] a = { true, true, false, true, false };
		System.out.println(Arrays.hashCode(a));
		//System.out.println(longHash(a, 0, 5));
		System.out.println(boolHash(a, 0, 5));
	}
	
	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int boolHash(boolean[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + (a[offset++] ? TRUE_HASH : FALSE_HASH);
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int byteHash(byte[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + a[offset++];
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int charHash(char[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + a[offset++];
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int shortHash(short[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + a[offset++];
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int intHash(int[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + a[offset++];
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int longHash(long[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0) {
			long l = a[offset++];
			result = MULTIPLIER * result + (int) (l ^ (l >>> Integer.SIZE));
		}
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int floatHash(float[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + Float.floatToIntBits(a[offset++]);
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int doubleHash(double[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0) {
			long l = Double.doubleToLongBits(a[offset++]);
			result = MULTIPLIER * result + (int) (l ^ (l >>> Integer.SIZE));
		}
		return result;
	}

	/**
	 * Provides Arrays.hashCode() for a sub-array.
	 */
	public static int objHash(Object[] a, int offset, int length) {
		if (a == null || !ArrayUtil.isValidSlice(a.length, offset, length)) return 0;
		int result = INIT;
		while (length-- > 0)
            result = MULTIPLIER * result + Objects.hashCode(a[offset++]);
		return result;
	}

	/**
	 * Constructor - using this with the add methods is more efficient for adding primitives.
	 */
	public static HashCoder create() {
		return new HashCoder();
	}

	/**
	 * Convenience method, best used with non-primitives.
	 */
	public static int hash(Object... values) {
		return new HashCoder().add(values).hashCode();
	}

	/**
	 * The resulting hash code.
	 */
	@Override
	public int hashCode() {
		return result;
	}

	/**
	 * Adds a value to the hash.
	 */
	public HashCoder add(boolean value) {
		return addValue(value ? 1 : 0);
	}

	/**
	 * Adds a value to the hash.
	 */
	public HashCoder add(int value) {
		return addValue(value);
	}

	/**
	 * Adds a value to the hash.
	 */
	public HashCoder add(long value) {
		return addValue((int) (value ^ (value >>> 32)));
	}

	/**
	 * Adds a value to the hash.
	 */
	public HashCoder add(float value) {
		return addValue(Float.floatToIntBits(value));
	}

	/**
	 * Adds a value to the hash.
	 */
	public HashCoder add(double value) {
		return add(Double.doubleToLongBits(value));
	}

	/**
	 * Adds multiple values to the hash.
	 */
	public HashCoder add(Object... values) {
		for (Object value : values)
			addObject(value);
		return this;
	}

	/**
	 * Includes hashcode of given object to result, based on its type. Arrays will be deeply hashed.
	 * To avoid deeply hashed arrays, instead call add(Arrays.hashCode(array));
	 */
	private HashCoder addObject(Object value) {
		if (value == null) return addValue(0);
		if (value instanceof Boolean) return add((Boolean) value);
		if (value instanceof Byte) return add((Byte) value);
		if (value instanceof Character) return add((Character) value);
		if (value instanceof Short) return add((Short) value);
		if (value instanceof Integer) return add((Integer) value);
		if (value instanceof Long) return add((Long) value);
		if (value instanceof Float) return add((Float) value);
		if (value instanceof Double) return add((Double) value);
		if (value.getClass().isArray()) return addArray(value);
		return addValue(value.hashCode());
	}

	private HashCoder addArray(Object array) {
		for (int i = 0; i < Array.getLength(array); i++)
			add(Array.get(array, i));
		return this;
	}

	private HashCoder addValue(int value) {
		result = MULTIPLIER * result + value;
		return this;
	}

}
