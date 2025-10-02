package ceri.common.concurrent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import ceri.common.array.ArrayUtil;
import ceri.common.data.IntAccessor;
import ceri.common.data.IntProvider;
import ceri.common.util.Validate;

/**
 * Fixed-size int array with volatile values.
 */
public class VolatileIntArray implements IntAccessor {
	public static final VolatileIntArray EMPTY = VolatileIntArray.wrap(ArrayUtil.ints.empty);
	private static final VarHandle handle = MethodHandles.arrayElementVarHandle(int[].class);
	private final int[] array;
	private final int offset;
	private final int length;

	public static VolatileIntArray copyOf(int[] array) {
		return copyOf(array, 0);
	}

	public static VolatileIntArray copyOf(int[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static VolatileIntArray copyOf(int[] array, int offset, int length) {
		return wrap(Arrays.copyOfRange(array, offset, offset + length));
	}

	public static VolatileIntArray of(int size) {
		return wrap(new int[size]);
	}

	public static VolatileIntArray wrap(int... array) {
		return new VolatileIntArray(array, 0, array.length);
	}

	private VolatileIntArray(int[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public int getInt(int index) {
		return (int) handle.getVolatile(array, offset + index);
	}

	@Override
	public int setInt(int index, int value) {
		handle.setVolatile(array, offset + index++, value);
		return index;
	}

	@Override
	public VolatileIntArray slice(int offset) {
		return slice(offset, length() - offset);
	}

	@Override
	public VolatileIntArray slice(int offset, int length) {
		Validate.slice(length(), offset, length);
		return new VolatileIntArray(array, this.offset + offset, length);
	}

	@Override
	public String toString() {
		return "V" + IntProvider.toString(this);
	}
}
