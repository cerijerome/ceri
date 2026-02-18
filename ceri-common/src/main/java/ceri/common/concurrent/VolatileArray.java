package ceri.common.concurrent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import ceri.common.array.Array;
import ceri.common.array.PrimitiveArray;
import ceri.common.array.TypedArray;
import ceri.common.data.ByteAccessor;
import ceri.common.data.ByteProvider;
import ceri.common.data.IntAccessor;
import ceri.common.data.IntProvider;
import ceri.common.data.LongAccessor;
import ceri.common.data.LongProvider;
import ceri.common.util.Validate;

/**
 * Fixed-size arrays with volatile access to values.
 */
public class VolatileArray<A, T> {
	public static final Factory<byte[], Bytes> BYTES = new Factory<>(Array.BYTE, Bytes::new);
	public static final Factory<int[], Ints> INTS = new Factory<>(Array.INT, Ints::new);
	public static final Factory<long[], Longs> LONGS = new Factory<>(Array.LONG, Longs::new);
	private final Factory<A, T> factory;
	private final A array;
	private final int offset;
	private final int length;

	private interface Wrapper<A, T> {
		T wrap(A array, int offset, int length);
	}

	public static class Factory<A, T> {
		public final T empty;
		private final TypedArray<A> primitive;
		private final VarHandle handle;
		private final Wrapper<A, T> wrapper;

		private Factory(PrimitiveArray<A, ?> primitive, Wrapper<A, T> wrapper) {
			this.primitive = primitive;
			this.wrapper = wrapper;
			handle = MethodHandles.arrayElementVarHandle(primitive.type());
			empty = of(0);
		}

		public T copyOf(A array) {
			return copyOf(array, 0);
		}

		public T copyOf(A array, int offset) {
			return copyOf(array, offset, primitive.length(array) - offset);
		}

		public T copyOf(A array, int offset, int length) {
			return wrap(primitive.copyOf(array, offset, length));
		}

		public T of(int size) {
			return wrap(primitive.array(size));
		}

		public T wrap(A array) {
			return wrapper.wrap(array, 0, primitive.length(array));
		}
	}

	/**
	 * Byte array with volatile access.
	 */
	public static class Bytes extends VolatileArray<byte[], Bytes> implements ByteAccessor {
		private Bytes(byte[] array, int offset, int length) {
			super(BYTES, array, offset, length);
		}

		@Override
		public byte getByte(int index) {
			return (byte) super.get(index);
		}

		@Override
		public int setByte(int index, int value) {
			return super.set(index, (byte) value);
		}

		@Override
		public String toString() {
			return "V" + ByteProvider.toHex(this);
		}
	}

	/**
	 * Int array with volatile access.
	 */
	public static class Ints extends VolatileArray<int[], Ints> implements IntAccessor {
		private Ints(int[] array, int offset, int length) {
			super(INTS, array, offset, length);
		}

		@Override
		public int getInt(int index) {
			return (int) super.get(index);
		}

		@Override
		public int setInt(int index, int value) {
			return super.set(index, value);
		}

		@Override
		public String toString() {
			return "V" + IntProvider.toString(this);
		}
	}

	/**
	 * Long array with volatile access.
	 */
	public static class Longs extends VolatileArray<long[], Longs> implements LongAccessor {
		private Longs(long[] array, int offset, int length) {
			super(LONGS, array, offset, length);
		}

		@Override
		public long getLong(int index) {
			return (long) super.get(index);
		}

		@Override
		public int setLong(int index, long value) {
			return super.set(index, value);
		}

		@Override
		public String toString() {
			return "V" + LongProvider.toString(this);
		}
	}

	private VolatileArray(Factory<A, T> factory, A array, int offset, int length) {
		this.factory = factory;
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public int length() {
		return length;
	}

	public T slice(int offset) {
		return slice(offset, length() - offset);
	}

	public T slice(int offset, int length) {
		Validate.slice(length(), offset, length);
		return factory.wrapper.wrap(array, this.offset + offset, length);
	}

	private Object get(int index) {
		return factory.handle.getVolatile(array, offset + index);
	}

	private int set(int index, Object value) {
		factory.handle.setVolatile(array, offset + index++, value);
		return index;
	}
}
