package ceri.common.concurrent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteAccessor;
import ceri.common.data.ByteProvider;
import ceri.common.util.Validate;

/**
 * Fixed-size byte array with volatile values.
 */
public class VolatileByteArray implements ByteAccessor {
	public static final VolatileByteArray EMPTY = VolatileByteArray.wrap(ArrayUtil.bytes.empty);
	private static final VarHandle handle = MethodHandles.arrayElementVarHandle(byte[].class);
	private final byte[] array;
	private final int offset;
	private final int length;

	public static VolatileByteArray copyOf(byte[] array) {
		return copyOf(array, 0);
	}

	public static VolatileByteArray copyOf(byte[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static VolatileByteArray copyOf(byte[] array, int offset, int length) {
		return wrap(Arrays.copyOfRange(array, offset, offset + length));
	}

	public static VolatileByteArray of(int size) {
		return wrap(new byte[size]);
	}

	public static VolatileByteArray wrap(int... array) {
		return wrap(ArrayUtil.bytes.of(array));
	}

	public static VolatileByteArray wrap(byte[] array) {
		return new VolatileByteArray(array, 0, array.length);
	}

	private VolatileByteArray(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public byte getByte(int index) {
		return (byte) handle.getVolatile(array, offset + index);
	}

	@Override
	public int setByte(int index, int value) {
		handle.setVolatile(array, offset + index++, (byte) value);
		return index;
	}

	@Override
	public VolatileByteArray slice(int offset) {
		return slice(offset, length() - offset);
	}

	@Override
	public VolatileByteArray slice(int offset, int length) {
		Validate.validateSlice(length(), offset, length);
		return new VolatileByteArray(array, this.offset + offset, length);
	}

	@Override
	public String toString() {
		return "V" + ByteProvider.toHex(this);
	}
}
