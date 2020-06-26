package ceri.common.data;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Fluent;
import ceri.common.math.MathUtil;
import ceri.common.util.HashCoder;

/**
 * Base wrapper for a byte array, implementing the ByteProvider interface. The Immutable sub-class
 * provides a concrete type. The Mutable sub-class also implements ByteReceiver, which provides
 * mutable access to the array.
 * <p/>
 * Internally, offset and length mark the permitted access window of the array. Bytes outside this
 * window must not be accessed or copied.
 */
public abstract class ByteArray implements ByteProvider {
	final byte[] array;
	private final int offset;
	private final int length;

	/**
	 * Wrapper for a byte array that does not allow modification. It allows slicing of views to
	 * promote re-use rather than copying of bytes. Note that wrap() does not copy the original byte
	 * array, and modifications of the original array will modify the wrapped array. If slicing
	 * large arrays, copying may be better for long-term objects, as the reference to the original
	 * array is no longer held.
	 */
	public static class Immutable extends ByteArray implements Fluent<Immutable> {
		public static final Immutable EMPTY = new Immutable(EMPTY_BYTE, 0, 0);

		public static Immutable copyOf(byte[] array) {
			return copyOf(array, 0);
		}

		public static Immutable copyOf(byte[] array, int offset) {
			return copyOf(array, offset, array.length - offset);
		}

		public static Immutable copyOf(byte[] array, int offset, int length) {
			byte[] newArray = Arrays.copyOfRange(array, offset, offset + length);
			return wrap(newArray);
		}

		public static Immutable wrap(int... array) {
			return wrap(ArrayUtil.bytes(array));
		}

		public static Immutable wrap(byte[] array) {
			return wrap(array, 0);
		}

		public static Immutable wrap(byte[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Immutable wrap(byte[] array, int offset, int length) {
			ArrayUtil.validateSlice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Immutable(array, offset, length);
		}

		private Immutable(byte[] array, int offset, int length) {
			super(array, offset, length);
		}

		/* ByteProvider overrides */

		@Override
		public Immutable slice(int index) {
			return slice(index, length() - index);
		}

		@Override
		public Immutable slice(int index, int length) {
			if (length == 0) return EMPTY;
			if (length < 0) return slice(index + length, -length);
			validateSlice(index, length);
			if (index == 0 && length == length()) return this;
			return wrap(array, offset(index), length);
		}

		/* Object overrides */

		@Override
		public int hashCode() {
			return hash();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Immutable)) return false;
			return isEqual((Immutable) obj);
		}
	}

	/**
	 * Wrapper for a byte array that allows modification. It allows slicing of views to promote
	 * re-use rather than copying of bytes. Note that wrap() does not copy the original byte array,
	 * and modifications of the original array will modify the wrapped array.
	 */
	public static class Mutable extends ByteArray implements ByteReceiver, Fluent<Mutable> {
		public static final Mutable EMPTY = new Mutable(EMPTY_BYTE, 0, 0);

		public static Mutable of(int length) {
			return wrap(new byte[length]);
		}

		public static Mutable wrap(int... array) {
			return wrap(ArrayUtil.bytes(array));
		}

		public static Mutable wrap(byte[] array) {
			return wrap(array, 0);
		}

		public static Mutable wrap(byte[] array, int offset) {
			return wrap(array, offset, array.length - offset);
		}

		public static Mutable wrap(byte[] array, int offset, int length) {
			ArrayUtil.validateSlice(array.length, offset, length);
			if (length == 0) return EMPTY;
			return new Mutable(array, offset, length);
		}

		private Mutable(byte[] array, int offset, int length) {
			super(array, offset, length);
		}

		/**
		 * Returns an immutable view. Any changes to this class will be seen in new view.
		 */
		public Immutable asImmutable() {
			return Immutable.wrap(array, offset(0), length());
		}

		/* ByteProvider overrides */

		@Override
		public boolean isEmpty() {
			return super.isEmpty();
		}

		@Override
		public Mutable slice(int index) {
			return slice(index, length() - index);
		}

		@Override
		public Mutable slice(int index, int length) {
			if (length == 0) return Mutable.EMPTY;
			if (length < 0) return slice(index + length, -length);
			validateSlice(index, length);
			if (index == 0 && length == length()) return this;
			return Mutable.wrap(array, offset(index), length);
		}

		/* ByteReceiver overrides */

		@Override
		public int setByte(int index, int b) {
			array[offset(index++)] = (byte) b;
			return index;
		}

		@Override
		public int setEndian(int index, int size, long value, boolean msb) {
			validateSlice(index, size);
			return msb ? ByteUtil.writeMsb(value, array, offset(index), size) :
				ByteUtil.writeLsb(value, array, offset(index), size);
		}

		@Override
		public int fill(int index, int length, int value) {
			validateSlice(index, length);
			Arrays.fill(array, offset(index), offset(index + length), (byte) value);
			return index + length;
		}

		@Override
		public int copyFrom(int index, byte[] array, int offset, int length) {
			validateSlice(index, length);
			ArrayUtil.validateSlice(array.length, offset, length);
			System.arraycopy(array, offset, this.array, offset(index), length);
			return index + length;
		}

		@Override
		public int copyFrom(int index, ByteProvider provider, int offset, int length) {
			validateSlice(index, length);
			provider.copyTo(offset, array, offset(index), length);
			return index + length;
		}

		@Override
		public int readFrom(int index, InputStream in, int length) throws IOException {
			validateSlice(index, length);
			int n = in.readNBytes(array, offset(index), length);
			return index + n;
		}

		/* Object overrides */

		@Override
		public int hashCode() {
			return hash();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Mutable)) return false;
			return isEqual((Mutable) obj);
		}
	}

	/**
	 * Wrapper for an expandable byte array, implementing ByteWriter and ByteReader. Reader methods
	 * will not increase the size of the array, and can throw IndexOutOfBoundsException if out of
	 * range. Writer methods will increase the size of the array as needed, up to MAX_SIZE.
	 * <p/>
	 * Internally, length is the current size, either the initial minimum size, or advanced by
	 * writing bytes. The length determines the size of the array for bytes(), mutable(),
	 * immutable() calls.
	 */
	public static class Encoder extends Navigator<Encoder>
		implements ByteWriter<Encoder>, ByteReader {
		public static final int MAX_DEF = Integer.MAX_VALUE - 8; // from ByteArrayOutputStream
		private static final int SIZE_DEF = 32;
		private final int max;
		private Mutable mutable;
		private byte[] array;

		/**
		 * Create an encoder with zero length, and default buffer size.
		 */
		public static Encoder of() {
			return new Encoder(new byte[SIZE_DEF], 0, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the byte array if the length is known ahead of time.
		 */
		public static Encoder of(int min) {
			return of(min, MAX_DEF);
		}

		/**
		 * Create an encoder with minimum length, and matching buffer size. Useful to optimize
		 * delivery of the byte array if the length is known ahead of time. The max determines the
		 * maximum size the byte array can grow to.
		 */
		public static Encoder of(int min, int max) {
			return new Encoder(new byte[min], min, max);
		}

		private Encoder(byte[] array, int limit, int max) {
			super(limit);
			this.max = max;
			this.array = array;
			mutable = Mutable.wrap(array);
		}

		public byte[] bytes() {
			if (length() == array.length) return array;
			return Arrays.copyOf(array, length());
		}

		public Mutable mutable() {
			return Mutable.wrap(array, 0, length());
		}

		public Immutable immutable() {
			return Immutable.wrap(array, 0, length());
		}

		/* ByteReader */

		@Override
		public byte readByte() {
			return array[readInc(1)];
		}

		@Override
		public long readEndian(int size, boolean msb) {
			return mutable.getEndian(readInc(size), size, msb);
		}

		@Override
		public String readString(int length, Charset charset) {
			return mutable.getString(readInc(length), length, charset);
		}

		@Override
		public byte[] readBytes(int length) {
			return mutable.copy(readInc(length), length);
		}

		@Override
		public int readInto(byte[] dest, int offset, int length) {
			return mutable.copyTo(readInc(length), dest, offset, length);
		}

		@Override
		public int readInto(ByteReceiver receiver, int offset, int length) {
			return mutable.copyTo(readInc(length), receiver, offset, length);
		}

		@Override
		public int transferTo(OutputStream out, int length) throws IOException {
			int current = offset();
			ArrayUtil.validateSlice(length(), current, length);
			offset(mutable.writeTo(current, out, length));
			return offset() - current;
		}

		@Override
		public IntStream ustream(int length) {
			return mutable.ustream(readInc(length), length);
		}

		/* ByteWriter */

		@Override
		public Encoder skip(int length) {
			writeInc(length);
			return this;
		}

		@Override
		public Encoder writeByte(int value) {
			int current = writeInc(1); // may modify mutable reference, don't combine
			mutable.setByte(current, value);
			return this;
		}

		@Override
		public Encoder writeEndian(long value, int size, boolean msb) {
			int current = writeInc(size); // may modify mutable reference, don't combine
			mutable.setEndian(current, size, value, msb);
			return this;
		}

		@Override
		public Encoder writeString(String s, Charset charset) {
			byte[] bytes = s.getBytes(charset);
			int current = writeInc(bytes.length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, bytes);
			return this;
		}

		@Override
		public Encoder fill(int length, int value) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.fill(current, length, value);
			return this;
		}

		@Override
		public Encoder writeFrom(byte[] array, int offset, int length) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, array, offset, length);
			return this;
		}

		@Override
		public Encoder writeFrom(ByteProvider provider, int offset, int length) {
			int current = writeInc(length); // may modify mutable reference, don't combine
			mutable.copyFrom(current, provider, offset, length);
			return this;
		}

		@Override
		public int transferFrom(InputStream in, int length) throws IOException {
			validateMin(length, 0);
			int current = offset();
			grow(current + length);
			writeInc(mutable.readFrom(current, in, length) - current);
			return offset() - current;
		}

		private int readInc(int length) {
			return inc(length, false);
		}

		private int writeInc(int length) {
			return inc(length, true);
		}

		/**
		 * Returns the current position and increments the offset by given length. If this exceeds
		 * the limit, and growth is permitted, the byte array is expanded to contain the new length.
		 * If growth is required but not permitted, an exception is thrown.
		 */
		private int inc(int length, boolean grow) {
			int current = offset();
			int offset = current + length;
			if (!grow && (offset < 0 || offset > length())) // includes overflow
				throw new IndexOutOfBoundsException(offset);
			if (grow) grow(offset);
			length(Math.max(length(), offset));
			offset(offset);
			return current;
		}

		private void grow(int length) {
			verifyGrowLength(length);
			if (length <= array.length) return;
			int newLen = array.length << 1;
			if (MathUtil.uint(newLen) > max) newLen = max;
			if (newLen < SIZE_DEF) newLen = Math.min(SIZE_DEF, max);
			if (newLen < length) newLen = length;
			array = Arrays.copyOf(array, newLen);
			mutable = Mutable.wrap(array);
		}

		private void verifyGrowLength(int length) {
			if (MathUtil.uint(length) > max) throw new IllegalArgumentException(String
				.format("Max allocation size %1$d (0x%1$x) bytes: %2$d (0x%2$x)", max, length));
		}
	}

	private ByteArray(byte[] array, int offset, int length) {
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
	public byte getByte(int index) {
		return array[offset(index)];
	}

	@Override
	public long getEndian(int index, int size, boolean msb) {
		validateSlice(index, size);
		return msb ? ByteUtil.fromMsb(array, offset(index), size) :
			ByteUtil.fromLsb(array, offset(index), size);
	}

	@Override
	public String getString(int index, int length, Charset charset) {
		validateSlice(index, length);
		return new String(array, offset(index), length, charset);
	}

	@Override
	public byte[] copy(int index, int length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		validateSlice(index, length);
		return Arrays.copyOfRange(array, offset(index), offset(index + length));
	}

	@Override
	public int copyTo(int index, byte[] dest, int offset, int length) {
		validateSlice(index, length);
		ArrayUtil.validateSlice(dest.length, offset, length);
		System.arraycopy(array, offset(index), dest, offset, length);
		return offset + length;
	}

	@Override
	public int copyTo(int index, ByteReceiver receiver, int offset, int length) {
		validateSlice(index, length);
		receiver.copyFrom(offset, array, offset(index), length);
		return index + length;
	}

	@Override
	public int writeTo(int index, OutputStream out, int length) throws IOException {
		validateSlice(index, length);
		if (length > 0) out.write(array, offset(index), length);
		return index + length;
	}

	@Override
	public boolean isEqualTo(int index, byte[] array, int offset, int length) {
		if (!isValidSlice(index, length)) return false;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
		return Arrays.equals(this.array, offset(index), offset(index + length), array, offset,
			offset + length);
	}

	@Override
	public boolean isEqualTo(int index, ByteProvider provider, int offset, int length) {
		if (!isValidSlice(index, length)) return false;
		return provider.isEqualTo(offset, array, offset(index), length);
	}

	/* Object overrides */

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + length + "]";
	}

	/* Support methods */

	boolean isEqual(ByteArray other) {
		if (length != other.length) return false;
		return Arrays.equals(array, offset(0), offset(length), other.array, other.offset(0),
			other.offset(length));
	}

	int hash() {
		HashCoder hashCoder = HashCoder.create();
		for (int i = 0; i < length; i++)
			hashCoder.add(array[offset(i)]);
		return hashCoder.hashCode();
	}

	boolean isValidSlice(int index, int length) {
		return ArrayUtil.isValidSlice(this.length, index, length);
	}

	void validateSlice(int index, int length) {
		ArrayUtil.validateSlice(this.length, index, length);
	}

	int offset(int index) {
		return this.offset + index;
	}
}
