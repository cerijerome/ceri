package ceri.common.data;

import ceri.common.math.Maths;
import ceri.common.util.Validate;

public class IntUtil {
	public static final int LONG_INTS = Long.BYTES / Integer.BYTES;

	private IntUtil() {}

	public static int low(long value) {
		return (int) value;
	}

	public static int high(long value) {
		return (int) (value >>> Integer.SIZE);
	}

	public static long longFromMsb(int... array) {
		return longFromMsb(array, 0);
	}

	public static long longFromMsb(int[] array, int offset) {
		Validate.validateSlice(array.length, offset, LONG_INTS);
		return (Maths.uint(array[offset]) << Integer.SIZE) | Maths.uint(array[offset + 1]);
	}

	public static long longFromLsb(int... array) {
		return longFromLsb(array, 0);
	}

	public static long longFromLsb(int[] array, int offset) {
		Validate.validateSlice(array.length, offset, LONG_INTS);
		return (Maths.uint(array[offset + 1]) << Integer.SIZE) | Maths.uint(array[offset]);
	}

	public static int[] longToMsb(long value) {
		int[] array = new int[LONG_INTS];
		writeLongMsb(value, array);
		return array;
	}

	public static int[] longToLsb(long value) {
		int[] array = new int[LONG_INTS];
		writeLongLsb(value, array);
		return array;
	}

	public static int writeLongMsb(long value, int... array) {
		return writeLongMsb(value, array, 0);
	}

	public static int writeLongMsb(long value, int[] array, int offset) {
		Validate.validateSlice(array.length, offset, LONG_INTS);
		array[offset++] = high(value);
		array[offset++] = low(value);
		return offset;
	}

	public static int writeLongLsb(long value, int... array) {
		return writeLongLsb(value, array, 0);
	}

	public static int writeLongLsb(long value, int[] array, int offset) {
		Validate.validateSlice(array.length, offset, LONG_INTS);
		array[offset++] = low(value);
		array[offset++] = high(value);
		return offset;
	}

}
