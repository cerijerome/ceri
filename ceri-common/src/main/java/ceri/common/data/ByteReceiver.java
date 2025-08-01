package ceri.common.data;

import static ceri.common.data.ByteUtil.IS_BIG_ENDIAN;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.common.array.ArrayUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Interface for receiving bytes into an array. For bulk efficiency, consider overriding the
 * following methods that process one byte at a time, or copy arrays.
 *
 * <pre>
 * int setEndian(int index, int size, long value, boolean msb); [copy]
 * int fill(int index, int length, int value); [1-byte]
 * int copyFrom(int index, byte[] array, int offset, int length); [1-byte]
 * int copyFrom(int index, ByteProvider provider, int offset, int length); [1-byte]
 * int readFrom(int index, InputStream in, int length) throws IOException; [1-byte]
 * </pre>
 *
 * @see ceri.common.data.ByteArray.Mutable
 * @see ceri.common.concurrent.VolatileByteArray
 */
public interface ByteReceiver {

	static ByteReceiver empty() {
		return ByteArray.Mutable.EMPTY;
	}

	/**
	 * {@link Navigator} and {@link ByteWriter} wrapper for a {@link ByteReceiver}. This provides
	 * sequential writing of bytes, and relative/absolute positioning for the next write. The type T
	 * allows typed access to the ByteReceiver.
	 * <p/>
	 * ByteWriter interface is complemented with methods that use remaining bytes instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Writer<T extends Writer<T>> extends Navigator<T> implements ByteWriter<T> {
		private final ByteReceiver receiver;
		private final int start;

		protected Writer(ByteReceiver receiver, int offset, int length) {
			super(length);
			this.receiver = receiver;
			this.start = offset;
		}

		/* ByteWriter overrides and additions */

		@Override
		public T writeByte(int value) {
			return position(receiver.setByte(position(), value));
		}

		@Override
		public T writeEndian(long value, int size, boolean msb) {
			return position(receiver.setEndian(position(), size, value, msb));
		}

		@Override
		public T writeString(String s, Charset charset) {
			return position(receiver.setString(position(), s, charset));
		}

		/**
		 * Fill remaining bytes with same value.
		 */
		public T fill(int value) {
			return fill(remaining(), value);
		}

		@Override
		public T fill(int length, int value) {
			return position(receiver.fill(position(), length, value));
		}

		@Override
		public T writeFrom(byte[] array, int offset, int length) {
			return position(receiver.copyFrom(position(), array, offset, length));
		}

		@Override
		public T writeFrom(ByteProvider provider, int offset, int length) {
			return position(receiver.copyFrom(position(), provider, offset, length));
		}

		/**
		 * Writes bytes from the input stream to remaining space, and returns the number of bytes
		 * transferred.
		 */
		public int transferFrom(InputStream in) throws IOException {
			return transferFrom(in, remaining());
		}

		@Override
		public int transferFrom(InputStream in, int length) throws IOException {
			int current = position();
			position(receiver.readFrom(current, in, length));
			return position() - current;
		}

		/* Other methods */

		public ByteReceiver receiver() {
			return receiver(remaining());
		}

		public ByteReceiver receiver(int length) {
			ByteReceiver receiver = this.receiver.slice(position(), length);
			position(position() + length);
			return receiver;
		}

		/**
		 * Creates a new reader for remaining bytes without incrementing the offset.
		 */
		public Writer<T> slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent bytes without incrementing the offset.
		 */
		public Writer<T> slice(int length) {
			ValidationUtil.validateSlice(length(), offset(), length);
			return new Writer<>(receiver, position(), length);
		}

		/**
		 * The actual position within the byte receiver.
		 */
		protected int position() {
			return start + offset();
		}

		/**
		 * Set the offset from receiver actual position.
		 */
		protected T position(int position) {
			return offset(position - start);
		}
	}

	/**
	 * Length of the space to receive bytes.
	 */
	int length();

	/**
	 * Determines if the length is 0.
	 */
	default boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Sets the byte value at given index, returns index + 1.
	 */
	int setByte(int index, int value);

	/**
	 * Sets bytes at given index. Returns the index after the written bytes.
	 */
	default int setBytes(int index, int... array) {
		return copyFrom(index, ArrayUtil.bytes.of(array));
	}

	/**
	 * Sets byte value 1 or 0 at the index. Returns the index after the written bytes.
	 */
	default int setBool(int index, boolean value) {
		return setByte(index, value ? 1 : 0);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setShort(int index, int value) {
		return setEndian(index, Short.BYTES, value, IS_BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setShortMsb(int index, int value) {
		return setEndian(index, Short.BYTES, value, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setShortLsb(int index, int value) {
		return setEndian(index, Short.BYTES, value, false);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setInt(int index, int value) {
		return setEndian(index, Integer.BYTES, value, IS_BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setIntMsb(int index, int value) {
		return setEndian(index, Integer.BYTES, value, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setIntLsb(int index, int value) {
		return setEndian(index, Integer.BYTES, value, false);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setLong(int index, long value) {
		return setEndian(index, Long.BYTES, value, IS_BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setLongMsb(int index, long value) {
		return setEndian(index, Long.BYTES, value, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setLongLsb(int index, long value) {
		return setEndian(index, Long.BYTES, value, false);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setFloat(int index, float value) {
		return setInt(index, Float.floatToIntBits(value));
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setFloatMsb(int index, float value) {
		return setIntMsb(index, Float.floatToIntBits(value));
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setFloatLsb(int index, float value) {
		return setIntLsb(index, Float.floatToIntBits(value));
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setDouble(int index, double value) {
		return setLong(index, Double.doubleToLongBits(value));
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setDoubleMsb(int index, double value) {
		return setLongMsb(index, Double.doubleToLongBits(value));
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setDoubleLsb(int index, double value) {
		return setLongLsb(index, Double.doubleToLongBits(value));
	}

	/**
	 * Sets the value in endian bytes at the index. Returns the index after the written bytes.
	 * Default implementation makes a copy of the bytes; efficiency may be improved by overriding
	 * this method.
	 */
	default int setEndian(int index, int size, long value, boolean msb) {
		byte[] bytes = msb ? ByteUtil.toMsb(value, size) : ByteUtil.toLsb(value, size);
		return copyFrom(index, bytes);
	}

	/**
	 * Encodes string as ISO-Latin-1 bytes from index. Returns the index after the written bytes.
	 */
	default int setAscii(int index, String s) {
		return setString(index, s, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Encodes string as UTF-8 bytes from index. Returns the index after the written bytes.
	 */
	default int setUtf8(int index, String s) {
		return setString(index, s, StandardCharsets.UTF_8);
	}

	/**
	 * Encodes string as bytes from index using the default character set. Returns the index after
	 * the written bytes.
	 */
	default int setString(int index, String s) {
		return setString(index, s, Charset.defaultCharset());
	}

	/**
	 * Encodes string as bytes from index using the character set. Returns the index after the
	 * written bytes.
	 */
	default int setString(int index, String s, Charset charset) {
		byte[] bytes = s.getBytes(charset);
		return copyFrom(index, bytes);
	}

	/**
	 * Creates a byte receiver view from index.
	 */
	default ByteReceiver slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates a byte receiver sub-view. A negative length will right-justify the view. Returns the
	 * current receiver for zero index and same length.
	 */
	default ByteReceiver slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Fills bytes from the index with the same value. Returns the index after the written bytes.
	 */
	default int fill(int index, int value) {
		return fill(index, length() - index, value);
	}

	/**
	 * Fills bytes from the index with the same value. Returns the index after the written bytes.
	 * Default implementation copies one byte at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int fill(int index, int length, int value) {
		ValidationUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setByte(index++, value);
		return index;
	}

	/**
	 * Copies bytes from the array to the index. Returns the index after the written bytes.
	 */
	default int copyFrom(int index, byte[] array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies bytes from the array to the index. Returns the index after the written bytes.
	 */
	default int copyFrom(int index, byte[] array, int offset) {
		return copyFrom(index, array, offset, array.length - offset);
	}

	/**
	 * Copies bytes from the array to the index. Returns the index after the written bytes. Default
	 * implementation copies one byte at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyFrom(int index, byte[] array, int offset, int length) {
		ValidationUtil.validateSlice(array.length, offset, length);
		ValidationUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setByte(index++, array[offset++]);
		return index;
	}

	/**
	 * Copies bytes from the provider to the index. Returns the index after the written bytes.
	 */
	default int copyFrom(int index, ByteProvider array) {
		return copyFrom(index, array, 0);
	}

	/**
	 * Copies bytes from the provider to the index. Returns the index after the written bytes.
	 */
	default int copyFrom(int index, ByteProvider array, int offset) {
		return copyFrom(index, array, offset, array.length() - offset);
	}

	/**
	 * Copies bytes from the provider to the index. Returns the index after the written bytes.
	 * Default implementation copies one byte at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int copyFrom(int index, ByteProvider provider, int offset, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		ValidationUtil.validateSlice(provider.length(), offset, length);
		while (length-- > 0)
			setByte(index++, provider.getByte(offset++));
		return index;
	}

	/**
	 * Reads bytes from the input stream and writes to the receiver at the index. The number of
	 * bytes read may be less than requested if EOF occurs. Returns the index after the written
	 * bytes.
	 */
	default int readFrom(int index, InputStream in) throws IOException {
		return readFrom(index, in, length() - index);
	}

	/**
	 * Reads bytes from the input stream and writes to the receiver at the index. The number of
	 * bytes read may be less than requested if EOF occurs. Returns the index after the written
	 * bytes. Default implementation reads one byte at a time; efficiency may be improved by
	 * overriding, or calling:
	 *
	 * <pre>
	 * return readBufferFrom(this, index, in, length);
	 * </pre>
	 */
	default int readFrom(int index, InputStream in, int length) throws IOException {
		ValidationUtil.validateSlice(length(), index, length);
		while (length-- > 0) {
			int value = in.read();
			if (value < 0) break; // EOF
			setByte(index++, value);
		}
		return index;
	}

	/**
	 * Reads bytes from the input stream and writes to the receiver at the index. The number of
	 * bytes read may be less than requested if EOF occurs. Returns the index after the written
	 * bytes. Implementing classes can call this in readFrom() if buffering is more efficient.
	 */
	static int readBufferFrom(ByteReceiver receiver, int index, InputStream in, int length)
		throws IOException {
		ValidationUtil.validateSlice(receiver.length(), index, length);
		byte[] buffer = in.readNBytes(length); // < length if EOF
		return receiver.copyFrom(index, buffer);
	}

	/**
	 * Provides sequential byte access.
	 */
	default Writer<?> writer(int index) {
		return writer(index, length() - index);
	}

	/**
	 * Provides sequential byte access.
	 */
	default Writer<?> writer(int index, int length) {
		ValidationUtil.validateSlice(length(), index, length);
		return new Writer<>(this, index, length);
	}

}
