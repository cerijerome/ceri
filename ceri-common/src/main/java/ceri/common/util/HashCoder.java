package ceri.common.util;

import java.lang.reflect.Array;

/**
 * Class for construction of strong hash codes.
 */
public class HashCoder {
	private static final int INITIAL_VALUE = 17; // convenient prime
	private static final int MULTIPLIER = 31; // convenient prime
	private int result = INITIAL_VALUE;

	private HashCoder() {}

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
		if (value instanceof Boolean) return add(((Boolean) value).booleanValue());
		if (value instanceof Byte) return add(((Byte) value).byteValue());
		if (value instanceof Character) return add(((Character) value).charValue());
		if (value instanceof Short) return add(((Short) value).shortValue());
		if (value instanceof Integer) return add(((Integer) value).intValue());
		if (value instanceof Long) return add(((Long) value).longValue());
		if (value instanceof Float) return add(((Float) value).floatValue());
		if (value instanceof Double) return add(((Double) value).doubleValue());
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
