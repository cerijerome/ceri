package ceri.common.concurrent;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;

/**
 * Fixed-size byte array with volatile values.
 */
public class VolatileByteArray implements ByteProvider, ByteReceiver {
	public static final VolatileByteArray EMPTY = VolatileByteArray.wrap(EMPTY_BYTE);
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
		return wrap(ArrayUtil.bytes(array));
	}

	public static VolatileByteArray wrap(byte[] array) {
		return new VolatileByteArray(array, 0, array.length);
	}

	private VolatileByteArray(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	/* ByteProvider overrides */

	@Override
	public int length() {
		return length;
	}

	@Override
	public boolean isEmpty() {
		return ByteProvider.super.isEmpty();
	}

	@Override
	public byte getByte(int index) {
		return (byte) handle.getVolatile(array, offset + index);
	}

	@Override
	public VolatileByteArray slice(int offset) {
		return slice(offset, length() - offset);
	}

	@Override
	public VolatileByteArray slice(int offset, int length) {
		ArrayUtil.validateSlice(length(), offset, length);
		return new VolatileByteArray(array, this.offset + offset, length);
	}

	/* ByteReceiver overrides */

	@Override
	public int setByte(int index, int value) {
		handle.setVolatile(array, offset + index++, (byte) value);
		return index;
	}

}