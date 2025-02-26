package ceri.common.concurrent;

import static ceri.common.collection.ArrayUtil.EMPTY_LONG;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import ceri.common.data.LongAccessor;
import ceri.common.data.LongProvider;
import ceri.common.validation.ValidationUtil;

/**
 * Fixed-size long array with volatile values.
 */
public class VolatileLongArray implements LongAccessor {
	public static final VolatileLongArray EMPTY = VolatileLongArray.wrap(EMPTY_LONG);
	private static final VarHandle handle = MethodHandles.arrayElementVarHandle(long[].class);
	private final long[] array;
	private final int offset;
	private final int length;

	public static VolatileLongArray copyOf(long[] array) {
		return copyOf(array, 0);
	}

	public static VolatileLongArray copyOf(long[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static VolatileLongArray copyOf(long[] array, int offset, int length) {
		return wrap(Arrays.copyOfRange(array, offset, offset + length));
	}

	public static VolatileLongArray of(int size) {
		return wrap(new long[size]);
	}

	public static VolatileLongArray wrap(long... array) {
		return new VolatileLongArray(array, 0, array.length);
	}

	private VolatileLongArray(long[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public long getLong(int index) {
		return (long) handle.getVolatile(array, offset + index);
	}

	@Override
	public int setLong(int index, long value) {
		handle.setVolatile(array, offset + index++, value);
		return index;
	}

	@Override
	public VolatileLongArray slice(int offset) {
		return slice(offset, length() - offset);
	}

	@Override
	public VolatileLongArray slice(int offset, int length) {
		ValidationUtil.validateSlice(length(), offset, length);
		return new VolatileLongArray(array, this.offset + offset, length);
	}

	@Override
	public String toString() {
		return "V" + LongProvider.toString(this);
	}
}
