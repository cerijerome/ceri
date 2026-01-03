package ceri.common.io;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import ceri.common.array.Array;
import ceri.common.array.PrimitiveArray;
import ceri.common.array.RawArray;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;

/**
 * Support for common buffer functionality.
 */
public class Buffers<B extends Buffer, A, T> {
	public static final OfChar CHAR = new OfChar();
	public static final OfByte BYTE = new OfByte();
	public static final OfShort SHORT = new OfShort();
	public static final OfInt INT = new OfInt();
	public static final OfLong LONG = new OfLong();
	public static final OfFloat FLOAT = new OfFloat();
	public static final OfDouble DOUBLE = new OfDouble();
	private final PrimitiveArray<A, T> array;
	private final int typeSize;
	private final Wrapper<B, A> wrap;
	private final Getter<B, A> get;
	private final Functions.BiOperator<B> put;
	private final Functions.Operator<B> readOnly;
	private final Functions.ToIntBiFunction<B, B> mismatch;
	private final Functions.Function<ByteBuffer, B> adapt;

	private interface Wrapper<B extends Buffer, A> {
		B apply(A array, int index, int length);
	}

	private interface Getter<B extends Buffer, A> {
		B apply(B buffer, A array, int index, int length);
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfChar extends Buffers<CharBuffer, char[], Character> {
		private OfChar() {
			super(Array.chars, Character.BYTES, CharBuffer::wrap, CharBuffer::get, CharBuffer::put,
				CharBuffer::asReadOnlyBuffer, CharBuffer::mismatch, ByteBuffer::asCharBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public CharBuffer of(char... array) {
			return of(array, 0);
		}

		/**
		 * Creates a read-only buffer view of the char sequence.
		 */
		public CharBuffer of(CharSequence s) {
			return s == null ? null : CharBuffer.wrap(s);
		}

		/**
		 * Gets values from the buffer at current position as a new string, and updates the buffer
		 * position.
		 */
		public String getString(CharBuffer buffer) {
			return getString(buffer, Integer.MAX_VALUE);
		}

		/**
		 * Gets values from the buffer at current position as a new string, and updates the buffer
		 * position.
		 */
		public String getString(CharBuffer buffer, int length) {
			if (buffer == null) return null;
			length = Maths.limit(length, 0, buffer.remaining());
			int lim = buffer.limit();
			var s = buffer.limit(buffer.position() + length).toString();
			buffer.position(buffer.limit()).limit(lim);
			return s;
		}

		/**
		 * Gets values from the buffer at given position as a new string, and updates the buffer
		 * position.
		 */
		public String getStringAt(CharBuffer buffer, int position) {
			return getStringAt(buffer, position, Integer.MAX_VALUE);
		}

		/**
		 * Gets values from the buffer at given position as a new string, and updates the buffer
		 * position.
		 */
		public String getStringAt(CharBuffer buffer, int position, int length) {
			position(buffer, position);
			return getString(buffer, length);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(CharBuffer to, char... from) {
			return copy(from, to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(CharBuffer to, int position, char... from) {
			return copyAt(from, to, position);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int copy(CharSequence s, CharBuffer to) {
			return super.copy(of(s), to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int copyAt(CharSequence s, CharBuffer to, int position) {
			return copyAt(of(s), 0, to, position);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfByte extends Buffers<ByteBuffer, byte[], Byte> {
		private OfByte() {
			super(Array.bytes, Byte.BYTES, ByteBuffer::wrap, ByteBuffer::get, ByteBuffer::put,
				ByteBuffer::asReadOnlyBuffer, ByteBuffer::mismatch, b -> b);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public ByteBuffer of(byte... array) {
			return of(array, 0);
		}

		/**
		 * Creates a buffer view of a converted copy of the array.
		 */
		public ByteBuffer of(int... array) {
			return of(Array.bytes.of(array));
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(ByteBuffer to, int... from) {
			return copy(Array.bytes.of(from), to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(ByteBuffer to, int position, int... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfShort extends Buffers<ShortBuffer, short[], Short> {
		private OfShort() {
			super(Array.shorts, Short.BYTES, ShortBuffer::wrap, ShortBuffer::get, ShortBuffer::put,
				ShortBuffer::asReadOnlyBuffer, ShortBuffer::mismatch, ByteBuffer::asShortBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public ShortBuffer of(short... array) {
			return of(array, 0);
		}

		/**
		 * Creates a buffer view of a converted copy of the array.
		 */
		public ShortBuffer of(int... array) {
			return of(Array.shorts.of(array));
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(ShortBuffer to, int... from) {
			return copy(Array.shorts.of(from), to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(ShortBuffer to, int position, int... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfInt extends Buffers<IntBuffer, int[], Integer> {
		private OfInt() {
			super(Array.ints, Integer.BYTES, IntBuffer::wrap, IntBuffer::get, IntBuffer::put,
				IntBuffer::asReadOnlyBuffer, IntBuffer::mismatch, ByteBuffer::asIntBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public IntBuffer of(int... array) {
			return of(array, 0);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(IntBuffer to, int... from) {
			return copy(from, to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(IntBuffer to, int position, int... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfLong extends Buffers<LongBuffer, long[], Long> {
		private OfLong() {
			super(Array.longs, Long.BYTES, LongBuffer::wrap, LongBuffer::get, LongBuffer::put,
				LongBuffer::asReadOnlyBuffer, LongBuffer::mismatch, ByteBuffer::asLongBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public LongBuffer of(long... array) {
			return of(array, 0);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(LongBuffer to, long... from) {
			return copy(from, to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(LongBuffer to, int position, long... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfFloat extends Buffers<FloatBuffer, float[], Float> {
		private OfFloat() {
			super(Array.floats, Float.BYTES, FloatBuffer::wrap, FloatBuffer::get, FloatBuffer::put,
				FloatBuffer::asReadOnlyBuffer, FloatBuffer::mismatch, ByteBuffer::asFloatBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public FloatBuffer of(float... array) {
			return of(array, 0);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(FloatBuffer to, float... from) {
			return copy(from, to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(FloatBuffer to, int position, float... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Extends type-specific functionality.
	 */
	public static class OfDouble extends Buffers<DoubleBuffer, double[], Double> {
		private OfDouble() {
			super(Array.doubles, Double.BYTES, DoubleBuffer::wrap, DoubleBuffer::get,
				DoubleBuffer::put, DoubleBuffer::asReadOnlyBuffer, DoubleBuffer::mismatch,
				ByteBuffer::asDoubleBuffer);
		}

		/**
		 * Creates a buffer view of the array.
		 */
		public DoubleBuffer of(double... array) {
			return of(array, 0);
		}

		/**
		 * Transfers values to the buffer at current position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int put(DoubleBuffer to, double... from) {
			return copy(from, to);
		}

		/**
		 * Transfers values to the buffer at given position, without overflow. Updates the buffer
		 * position and returns the number of values transferred.
		 */
		public int putAt(DoubleBuffer to, int position, double... from) {
			position(to, position);
			return put(to, from);
		}
	}

	/**
	 * Returns the current position, or 0 if null.
	 */
	public static int position(Buffer buffer) {
		return buffer == null ? 0 : buffer.position();
	}

	/**
	 * Sets the buffer position within bounds, and returns the position.
	 */
	public static int position(Buffer buffer, int position) {
		if (buffer == null) return 0;
		position = Maths.limit(position, 0, buffer.limit());
		buffer.position(position);
		return position;
	}

	/**
	 * Returns the current limit, or 0 if null.
	 */
	public static int limit(Buffer buffer) {
		return buffer == null ? 0 : buffer.limit();
	}

	/**
	 * Sets the buffer limit within bounds, and returns the limit.
	 */
	public static int limit(Buffer buffer, int limit) {
		if (buffer == null) return 0;
		limit = Maths.limit(limit, 0, buffer.capacity());
		buffer.limit(limit);
		return limit;
	}

	/**
	 * Returns the remaining elements; returns 0 if null.
	 */
	public static int remaining(Buffer buffer) {
		return buffer == null ? 0 : buffer.remaining();
	}

	/**
	 * Gets remaining values as a new byte array.
	 */
	public static byte[] bytes(Buffer buffer) {
		if (buffer == null) return null;
		var support = supportFor(buffer);
		return support.getBytes(Reflect.unchecked(buffer));
	}

	private Buffers(PrimitiveArray<A, T> array, int typeSize, Wrapper<B, A> wrap, Getter<B, A> get,
		Functions.BiOperator<B> put, Functions.Operator<B> readOnly,
		Functions.ToIntBiFunction<B, B> mismatch, Functions.Function<ByteBuffer, B> adapt) {
		this.array = array;
		this.typeSize = typeSize;
		this.wrap = wrap;
		this.get = get;
		this.put = put;
		this.readOnly = readOnly;
		this.mismatch = mismatch;
		this.adapt = adapt;
	}

	/**
	 * Returns the number of bytes per buffer element.
	 */
	public int typeSize() {
		return typeSize;
	}

	/**
	 * Returns the array support.
	 */
	public PrimitiveArray<A, T> arrays() {
		return array;
	}

	/**
	 * Returns a buffer view of the array.
	 */
	public B of(A array, int index) {
		return of(array, index, Integer.MAX_VALUE);
	}

	/**
	 * Returns a buffer view of the array.
	 */
	public B of(A array, int index, int length) {
		if (array == null) return null;
		index = Maths.limit(index, 0, RawArray.length(array));
		length = Maths.limit(length, 0, RawArray.length(array) - index);
		return wrap.apply(array, index, length);
	}

	/**
	 * Returns a view of the buffer.
	 */
	public B from(ByteBuffer buffer) {
		if (buffer == null) return null;
		return adapt.apply(buffer);
	}

	/**
	 * Returns a new buffer, copying values from the given buffer.
	 */
	public B copyFrom(Buffer buffer) {
		if (buffer == null) return null;
		var support = supportFor(buffer);
		var byteBuffer = support.copyAsByteBuffer(Reflect.unchecked(buffer));
		return from(byteBuffer);
	}

	/**
	 * Returns a read-only buffer view.
	 */
	public B readOnly(B buffer) {
		if (buffer == null || buffer.isReadOnly()) return buffer;
		return readOnly.apply(buffer);
	}

	/**
	 * Returns the relative offset of any mismatch, or -1 if no mismatch; returns 0 if either buffer
	 * is null.
	 */
	public int mismatch(B buffer, B other) {
		if (buffer == null || other == null) return 0;
		return mismatch.applyAsInt(buffer, other);
	}

	/**
	 * Gets values from the current buffer position as a new array, and updates the buffer position.
	 */
	public A get(B buffer) {
		return get(buffer, Integer.MAX_VALUE);
	}

	/**
	 * Gets values from the current buffer position as a new array, and updates the buffer position.
	 */
	public A get(B buffer, int length) {
		if (buffer == null) return null;
		length = Maths.limit(length, 0, buffer.remaining());
		if (length <= 0) return arrays().empty;
		var array = arrays().array(length);
		get.apply(buffer, array, 0, length);
		return array;
	}

	/**
	 * Gets values from the given buffer position as a new array, and updates the buffer position.
	 */
	public A getAt(B buffer, int position) {
		return getAt(buffer, position, Integer.MAX_VALUE);
	}

	/**
	 * Gets values from the given buffer position as a new array, and updates the buffer position.
	 */
	public A getAt(B buffer, int position, int length) {
		position(buffer, position);
		return get(buffer, length);
	}

	/**
	 * Transfers values from the buffer at current position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(B from, A to) {
		return copy(from, to, 0);
	}

	/**
	 * Transfers values from the buffer at current position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(B from, A to, int index) {
		return copy(from, to, index, Integer.MAX_VALUE);
	}

	/**
	 * Transfers values from the buffer at current position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(B from, A to, int index, int length) {
		return copy(from, of(to, index, length));
	}

	/**
	 * Transfers values from the array to the buffer at current position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(A from, B to) {
		return copy(from, 0, to);
	}

	/**
	 * Transfers values from the array to the buffer at current position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(A from, int index, B to) {
		return copy(from, index, Integer.MAX_VALUE, to);
	}

	/**
	 * Transfers values from the array to the buffer at current position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copy(A from, int index, int length, B to) {
		return copy(of(from, index, length), to);
	}

	/**
	 * Transfers values from source buffer to destination buffer at current positions, without
	 * overflow. Updates the buffer positions and returns the number of values transferred.
	 */
	public int copy(B from, B to) {
		return copy(from, to, Integer.MAX_VALUE);
	}

	/**
	 * Transfers values from source buffer to destination buffer at current positions, up to count,
	 * without overflow. Updates the buffer positions and returns the number of values transferred.
	 */
	public int copy(B from, B to, int count) {
		if (from == null || to == null) return 0;
		count = Maths.min(from.remaining(), to.remaining(), count);
		if (count <= 0) return 0;
		int lim = from.limit();
		from.limit(from.position() + count);
		put.apply(to, from);
		from.limit(lim);
		return count;
	}

	/**
	 * Transfers values from the buffer at given position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(B from, int position, A to) {
		return copyAt(from, position, to, 0);
	}

	/**
	 * Transfers values from the buffer at given position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(B from, int position, A to, int index) {
		return copyAt(from, position, to, index, Integer.MAX_VALUE);
	}

	/**
	 * Transfers values from the buffer at given position to the array, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(B from, int position, A to, int index, int length) {
		position(from, position);
		return copy(from, to, index, length);
	}

	/**
	 * Transfers values from the array to the buffer at given position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(A from, B to, int position) {
		return copyAt(from, 0, to, position);
	}

	/**
	 * Transfers values from the array to the buffer at given position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(A from, int index, B to, int position) {
		return copyAt(from, index, Integer.MAX_VALUE, to, position);
	}

	/**
	 * Transfers values from the array to the buffer at given position, without overflow. Updates
	 * the buffer position and returns the number of values transferred.
	 */
	public int copyAt(A from, int index, int length, B to, int position) {
		position(to, position);
		return copy(of(from, index, length), to);
	}

	/**
	 * Transfers values from source buffer to destination buffer at given positions, without
	 * overflow. Updates the buffer positions and returns the number of values transferred.
	 */
	public int copyAt(B from, int fromPos, B to, int toPos) {
		return copyAt(from, fromPos, to, toPos, Integer.MAX_VALUE);
	}

	/**
	 * Transfers values from source buffer to destination buffer at given positions, up to count,
	 * without overflow. Updates the buffer positions and returns the number of values transferred.
	 */
	public int copyAt(B from, int fromPos, B to, int toPos, int count) {
		position(from, fromPos);
		position(to, toPos);
		return copy(from, to, count);
	}

	// support

	private byte[] getBytes(B buffer) {
		var array = Array.bytes.array(typeSize() * buffer.remaining());
		copy(buffer, from(ByteBuffer.wrap(array)));
		return array;
	}

	private ByteBuffer copyAsByteBuffer(B buffer) {
		var byteBuffer = ByteBuffer.allocate(typeSize() * buffer.remaining());
		copy(buffer, from(byteBuffer));
		return byteBuffer.flip();
	}

	private static Buffers<?, ?, ?> supportFor(Buffer b) {
		return switch (b) {
			case CharBuffer _ -> CHAR;
			case ShortBuffer _ -> SHORT;
			case IntBuffer _ -> INT;
			case LongBuffer _ -> LONG;
			case FloatBuffer _ -> FLOAT;
			case DoubleBuffer _ -> DOUBLE;
			default -> BYTE;
		};
	}
}
