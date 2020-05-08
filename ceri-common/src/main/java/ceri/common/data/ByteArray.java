package ceri.common.data;

import static ceri.common.collection.ArrayUtil.EMPTY_BYTE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Fluent;
import ceri.common.util.HashCoder;

/**
 * Base wrapper for a byte array, implementing the ByteProvider interface. The Immutable sub-class
 * provides a concrete type. The Mutable sub-class implements ByteReceiver, which provides
 * additional mutable access to the array.
 * <p/>
 * Internally, offset and length mark the permitted access window of the array. Bytes outside this
 * window must not be accessed or copied.
 */
public abstract class ByteArray implements ByteProvider {
	public static final Mutable EMPTY = new Mutable(ArrayUtil.EMPTY_BYTE, 0, 0);
	final byte[] array;
	private final int offset;
	private final int length;

	/**
	 * A fixed-size byte array encoder.
	 */
	public static ByteReceiver.Encoder encoder(byte[] data) {
		return ByteReceiver.Encoder.wrap(data);
	}
	
	/**
	 * A fixed-size byte array encoder.
	 */
	public static ByteReceiver.Encoder encoder(int size) {
		return ByteReceiver.Encoder.of(size);
	}
	
	/**
	 * A variable-length byte array encoder.
	 */
	public static ByteStream.Encoder encoder() {
		return ByteStream.Encoder.of();
	}
	
//	/**
//	 * Encode a fixed-size byte array, using a ByteWriter.
//	 */
//	public static Mutable encode(int size, Consumer<? super ByteReceiver.Writer> consumer) {
//		Mutable bytes = Mutable.of(size);
//		consumer.accept(bytes.writer(0));
//		return bytes;
//	}
//	
//	/**
//	 * Encode a variable-sized byte array, using a ByteWriter.
//	 */
//	public static Mutable encode(Consumer<? super ByteStream.Writer> consumer) {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		consumer.accept(ByteStream.writer(out));
//		return Mutable.wrap(out.toByteArray());
//	}
	
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

		public static Immutable wrap(IntStream stream) {
			return wrap(ByteUtil.bytes(stream));
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
		public static final Mutable EMPTY = new Mutable(ArrayUtil.EMPTY_BYTE, 0, 0);

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

		/* ByteProvider overrides */

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
	public boolean isEmpty() {
		return ByteProvider.super.isEmpty();
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
