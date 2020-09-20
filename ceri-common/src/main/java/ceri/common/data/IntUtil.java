package ceri.common.data;

import ceri.common.collection.ArrayUtil;
import ceri.common.math.MathUtil;

public class IntUtil {
	public static final int LONG_INTS = Long.BYTES / Integer.BYTES;

	private IntUtil() {}

	public static long longFromMsb(int[] array) {
		return longFromMsb(array, 0);
	}

	public static long longFromMsb(int[] array, int offset) {
		ArrayUtil.validateSlice(array.length, offset, LONG_INTS);
		return (MathUtil.uint(array[offset]) << Integer.SIZE) | MathUtil.uint(array[offset + 1]);
	}

	public static long longFromLsb(int[] array) {
		return longFromLsb(array, 0);
	}

	public static long longFromLsb(int[] array, int offset) {
		ArrayUtil.validateSlice(array.length, offset, LONG_INTS);
		return (MathUtil.uint(array[offset + 1]) << Integer.SIZE) | MathUtil.uint(array[offset]);
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

	public static int writeLongMsb(long value, int[] array) {
		return writeLongMsb(value, array, 0);
	}

	public static int writeLongMsb(long value, int[] array, int offset) {
		ArrayUtil.validateSlice(array.length, offset, LONG_INTS);
		array[offset++] = (int) (value >>> Integer.SIZE);
		array[offset++] = (int) value;
		return offset;
	}

	public static int writeLongLsb(long value, int[] array) {
		return writeLongLsb(value, array, 0);
	}

	public static int writeLongLsb(long value, int[] array, int offset) {
		ArrayUtil.validateSlice(array.length, offset, LONG_INTS);
		array[offset++] = (int) value;
		array[offset++] = (int) (value >>> Integer.SIZE);
		return offset;
	}

}
