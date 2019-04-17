package ceri.common.concurrent;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.common.data.ByteUtil.bytes;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;

/**
 * Fixed-size byte array with volatile values.
 */
public class AtomicByteArray implements ByteProvider, ByteReceiver {
	public static final AtomicByteArray EMPTY = AtomicByteArray.wrap(EMPTY_BYTE);
	private static final VarHandle handle = MethodHandles.arrayElementVarHandle(byte[].class);
	private final byte[] array;
	private final int offset;
	private final int length;

	public static AtomicByteArray copyOf(byte[] array) {
		return copyOf(array, 0);
	}

	public static AtomicByteArray copyOf(byte[] array, int offset) {
		return copyOf(array, offset, array.length - offset);
	}

	public static AtomicByteArray copyOf(byte[] array, int offset, int length) {
		return wrap(Arrays.copyOfRange(array, offset, offset + length));
	}

	public static AtomicByteArray from(ImmutableByteArray array) {
		return wrap(array.copy());
	}

	public static AtomicByteArray of(int size) {
		return wrap(new byte[size]);
	}

	public static AtomicByteArray wrap(int... array) {
		return wrap(bytes(array));
	}

	public static AtomicByteArray wrap(byte... array) {
		return new AtomicByteArray(array, 0, array.length);
	}

	private AtomicByteArray(byte[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public AtomicByteArray slice(int offset) {
		return slice(offset, length() - offset);
	}

	public AtomicByteArray slice(int offset, int length) {
		ArrayUtil.validateSlice(length(), offset, length);
		return new AtomicByteArray(array, this.offset + offset, length);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public byte get(int index) {
		return (byte) handle.getVolatile(array, offset + index);
	}

	@Override
	public void set(int index, int value) {
		handle.setVolatile(array, offset + index, (byte) value);
	}

}
