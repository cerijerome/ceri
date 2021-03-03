package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.Iterators;
import ceri.common.function.Fluent;
import ceri.common.math.MathUtil;

/**
 * Interface that provides positional access to bytes. For bulk efficiency, consider overriding the
 * following methods that process one byte at a time, or copy arrays.
 *
 * <pre>
 * long getEndian(int index, int size, boolean msb); [copy]
 * String getString(int index, int length, Charset charset); [copy]
 * ByteProvider slice(int offset, int length); [copy]
 * int copyTo(int index, byte[] dest, int destOffset, int length); [1-byte]
 * int copyTo(int index, ByteReceiver dest, int destOffset, int length); [1-byte]
 * int writeTo(int offset, OutputStream out, int length) throws IOException; [1-byte]
 * boolean isEqualTo(int index, byte[] array, int offset, int length); [1-byte]
 * boolean isEqualTo(int index, ByteProvider provider, int offset, int length); [1-byte]
 * </pre>
 *
 * @see ceri.common.data.ByteArray.Immutable
 * @see ceri.common.concurrent.VolatileByteArray
 */
public interface ByteProvider extends Iterable<Integer> {

	static ByteProvider empty() {
		return ByteArray.Immutable.EMPTY;
	}

	static ByteProvider of(int... bytes) {
		return ByteArray.Immutable.wrap(bytes);
	}
	
	static ByteProvider of(byte... bytes) {
		return ByteArray.Immutable.wrap(bytes);
	}

	/**
	 * {@link Navigator} and {@link ByteReader} wrapper for a {@link ByteProvider}. This provides
	 * sequential access to bytes, and relative/absolute positioning for the next read.
	 * <p/>
	 * ByteReader interface is complemented with methods that use remaining bytes instead of given
	 * length. Except for {@link #offset(int)}, methods do not include an offset position. Clients
	 * must first call {@link #offset(int)} if an absolute position is required.
	 */
	static class Reader extends Navigator<Reader> implements ByteReader, Fluent<Reader> {
		private final ByteProvider provider;
		private final int start;

		private Reader(ByteProvider provider, int offset, int length) {
			super(length);
			this.provider = provider;
			this.start = offset;
		}

		/* ByteReader overrides and additions */

		@Override
		public byte readByte() {
			return provider.getByte(inc(1));
		}

		@Override
		public long readEndian(int size, boolean msb) {
			return provider.getEndian(inc(size), size, msb);
		}

		/**
		 * Returns the string from ISO-Latin-1 bytes.
		 */
		public String readAscii() {
			return readAscii(remaining());
		}

		/**
		 * Returns the string from UTF-8 bytes.
		 */
		public String readUtf8() {
			return readUtf8(remaining());
		}

		/**
		 * Returns the string from default character-set bytes.
		 */
		public String readString() {
			return readString(remaining());
		}

		/**
		 * Returns the string from character-set encoded bytes.
		 */
		public String readString(Charset charset) {
			return readString(remaining(), charset);
		}

		@Override
		public String readString(int length, Charset charset) {
			return provider.getString(inc(length), length, charset);
		}

		/**
		 * Reads an array of the remaining bytes.
		 */
		public byte[] readBytes() {
			return readBytes(remaining());
		}

		@Override
		public byte[] readBytes(int length) {
			return provider.copy(inc(length), length);
		}

		@Override
		public int readInto(byte[] dest, int offset, int length) {
			return provider.copyTo(inc(length), dest, offset, length);
		}

		@Override
		public int readInto(ByteReceiver receiver, int offset, int length) {
			return provider.copyTo(inc(length), receiver, offset, length);
		}

		/**
		 * Writes bytes to the output stream, and returns the number of bytes transferred.
		 */
		public int transferTo(OutputStream out) throws IOException {
			return transferTo(out, remaining());
		}

		@Override
		public int transferTo(OutputStream out, int length) throws IOException {
			int offset = inc(length);
			return provider.writeTo(offset, out, length) - offset;
		}

		/**
		 * Provides unsigned bytes as a stream.
		 */
		public IntStream ustream() {
			return ustream(remaining());
		}

		@Override
		public IntStream ustream(int length) {
			return provider.ustream(inc(length), length);
		}

		/* Other methods */

		@Override
		public Reader skip(int length) {
			return super.skip(length);
		}

		/**
		 * Returns a view of the ByteProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public ByteProvider provider() {
			return provider(remaining());
		}

		/**
		 * Returns a view of the ByteProvider, incrementing the offset. Only supported if slice() is
		 * implemented.
		 */
		public ByteProvider provider(int length) {
			return provider.slice(inc(length), length);
		}

		/**
		 * Creates a new reader for remaining bytes without incrementing the offset.
		 */
		public Reader slice() {
			return slice(remaining());
		}

		/**
		 * Creates a new reader for subsequent bytes without incrementing the offset.
		 */
		public Reader slice(int length) {
			ArrayUtil.validateSlice(length(), offset(), length);
			return new Reader(provider, start + offset(), length);
		}

		/**
		 * Returns the current position and increments the offset by length.
		 */
		private int inc(int length) {
			int position = start + offset();
			skip(length);
			return position;
		}
	}

	/**
	 * Iterates over unsigned bytes.
	 */
	@Override
	default Iterator<Integer> iterator() {
		return Iterators.indexed(length(), i -> (int) getUbyte(i));
	}

	/**
	 * The number of provided bytes.
	 */
	int length();

	/**
	 * Determines if the length is 0.
	 */
	default boolean isEmpty() {
		return length() == 0;
	}

	/**
	 * Returns the byte at given index.
	 */
	byte getByte(int index);

	/**
	 * Returns the unsigned byte value at given index.
	 */
	default short getUbyte(int index) {
		return MathUtil.ubyte(getByte(index));
	}

	/**
	 * Returns true if byte is non-zero at given index.
	 */
	default boolean getBool(int index) {
		return getByte(index) != 0;
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	default short getShort(int index) {
		return (short) getEndian(index, Short.BYTES, BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	default short getShortMsb(int index) {
		return (short) getEndian(index, Short.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	default short getShortLsb(int index) {
		return (short) getEndian(index, Short.BYTES, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes at given index.
	 */
	default int getUshort(int index) {
		return MathUtil.ushort(getShort(index));
	}

	/**
	 * Returns the unsigned value from big-endian bytes at given index.
	 */
	default int getUshortMsb(int index) {
		return MathUtil.ushort(getShortMsb(index));
	}

	/**
	 * Returns the unsigned value from little-endian bytes at given index.
	 */
	default int getUshortLsb(int index) {
		return MathUtil.ushort(getShortLsb(index));
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	default int getInt(int index) {
		return (int) getEndian(index, Integer.BYTES, BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	default int getIntMsb(int index) {
		return (int) getEndian(index, Integer.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	default int getIntLsb(int index) {
		return (int) getEndian(index, Integer.BYTES, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes at given index.
	 */
	default long getUint(int index) {
		return MathUtil.uint(getInt(index));
	}

	/**
	 * Returns the unsigned value from big-endian bytes at given index.
	 */
	default long getUintMsb(int index) {
		return MathUtil.uint(getIntMsb(index));
	}

	/**
	 * Returns the unsigned value from little-endian bytes at given index.
	 */
	default long getUintLsb(int index) {
		return MathUtil.uint(getIntLsb(index));
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	default long getLong(int index) {
		return getEndian(index, Long.BYTES, BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	default long getLongMsb(int index) {
		return getEndian(index, Long.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	default long getLongLsb(int index) {
		return getEndian(index, Long.BYTES, false);
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	default float getFloat(int index) {
		return Float.intBitsToFloat(getInt(index));
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	default float getFloatMsb(int index) {
		return Float.intBitsToFloat(getIntMsb(index));
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	default float getFloatLsb(int index) {
		return Float.intBitsToFloat(getIntLsb(index));
	}

	/**
	 * Returns the value from native-order bytes at given index.
	 */
	default double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}

	/**
	 * Returns the value from big-endian bytes at given index.
	 */
	default double getDoubleMsb(int index) {
		return Double.longBitsToDouble(getLongMsb(index));
	}

	/**
	 * Returns the value from little-endian bytes at given index.
	 */
	default double getDoubleLsb(int index) {
		return Double.longBitsToDouble(getLongLsb(index));
	}

	/**
	 * Returns the value from endian bytes at given index. Default implementation makes a copy of
	 * bytes; efficiency may be improved by overriding this method.
	 */
	default long getEndian(int index, int size, boolean msb) {
		byte[] copy = copy(index, size);
		return msb ? ByteUtil.fromMsb(copy) : ByteUtil.fromLsb(copy);
	}

	/**
	 * Decodes ISO-Latin-1 bytes from index into a string.
	 */
	default String getAscii(int index) {
		return getAscii(index, length() - index);
	}

	/**
	 * Decodes ISO-Latin-1 bytes from index into a string.
	 */
	default String getAscii(int index, int length) {
		return getString(index, length, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Decodes UTF-8 bytes from index into a string.
	 */
	default String getUtf8(int index) {
		return getUtf8(index, length() - index);
	}

	/**
	 * Decodes UTF-8 bytes from index into a string.
	 */
	default String getUtf8(int index, int length) {
		return getString(index, length, StandardCharsets.UTF_8);
	}

	/**
	 * Decodes bytes from index into a string using the default character set.
	 */
	default String getString(int index) {
		return getString(index, length() - index);
	}

	/**
	 * Decodes bytes from index into a string using the default character set.
	 */
	default String getString(int index, int length) {
		return getString(index, length, Charset.defaultCharset());
	}

	/**
	 * Decodes bytes from index into a string using the character set.
	 */
	default String getString(int index, Charset charset) {
		return getString(index, length() - index, charset);
	}

	/**
	 * Decodes bytes from index into a string using the character set. Default implementation makes
	 * a copy of bytes; efficiency may be improved by overriding this method.
	 */
	default String getString(int index, int length, Charset charset) {
		byte[] copy = copy(index, length);
		return new String(copy, charset);
	}

	/**
	 * Creates a byte provider view from index.
	 */
	default ByteProvider slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Creates a byte provider sub-view. A negative length will right-justify the view. Returns the
	 * current provider for zero index and same length. Default implementation makes a copy of
	 * bytes; efficiency may be improved by overriding this method.
	 */
	default ByteProvider slice(int index, int length) {
		if (length == 0) return empty();
		if (index == 0 && length == length()) return this;
		throw new UnsupportedOperationException(
			String.format("slice(%d, %d) is not supported", index, length));
	}

	/**
	 * Returns a copy of provided bytes from index.
	 */
	default byte[] copy(int index) {
		return copy(index, length() - index);
	}

	/**
	 * Returns a copy of provided bytes from index.
	 */
	default byte[] copy(int index, int length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		ArrayUtil.validateSlice(length(), index, length);
		byte[] copy = new byte[length];
		copyTo(index, copy, 0, length);
		return copy;
	}

	/**
	 * Copies bytes from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, byte[] array) {
		return copyTo(index, array, 0);
	}

	/**
	 * Copies bytes from index to the array. Returns the index after copying.
	 */
	default int copyTo(int index, byte[] array, int offset) {
		return copyTo(index, array, offset, array.length - offset);
	}

	/**
	 * Copies bytes from index to the array. Returns the index after copying. Default implementation
	 * writes one byte at a time; efficiency may be improved by overriding this method.
	 */
	default int copyTo(int index, byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = getByte(index++);
		return index;
	}

	/**
	 * Copies bytes from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, ByteReceiver receiver) {
		return copyTo(index, receiver, 0);
	}

	/**
	 * Copies bytes from index to the receiver. Returns the index after copying.
	 */
	default int copyTo(int index, ByteReceiver receiver, int offset) {
		return copyTo(index, receiver, offset, receiver.length() - offset);
	}

	/**
	 * Copies bytes from index to the receiver. Returns the index after copying. Default
	 * implementation writes one byte at a time; efficiency may be improved by overriding this
	 * method.
	 */
	default int copyTo(int index, ByteReceiver receiver, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setByte(offset++, getByte(index++));
		return index;
	}

	/**
	 * Writes bytes from index to the output stream. Returns the index after writing.
	 */
	default int writeTo(int index, OutputStream out) throws IOException {
		return writeTo(index, out, length() - index);
	}

	/**
	 * Writes bytes from index to the output stream. Returns the index after writing. Default
	 * implementation writes one byte at a time; efficiency may be improved by overriding, or by
	 * calling:
	 *
	 * <pre>
	 * return writeBufferTo(this, index, out, length);
	 * </pre>
	 */
	default int writeTo(int index, OutputStream out, int length) throws IOException {
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			out.write(getByte(index++));
		return index;
	}

	/**
	 * Writes bytes to the output stream from the receiver at the index. Returns the index after the
	 * written bytes. Implementing classes can call this in writeTo() if buffering is more
	 * efficient.
	 */
	static int writeBufferTo(ByteProvider provider, int index, OutputStream out, int length)
		throws IOException {
		byte[] buffer = provider.copy(index, length);
		out.write(buffer);
		return index + buffer.length;
	}

	/**
	 * Provides unsigned bytes from index as a stream.
	 */
	default IntStream ustream(int index) {
		return ustream(index, length() - index);
	}

	/**
	 * Provides unsigned bytes from index as a stream.
	 */
	default IntStream ustream(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return IntStream.range(index, index + length).map(i -> getUbyte(i));
	}

	/**
	 * Returns true if bytes are equal to array bytes.
	 */
	default boolean isEqualTo(int index, int... array) {
		return isEqualTo(index, ArrayUtil.bytes(array));
	}

	/**
	 * Returns true if bytes from index are equal to array bytes.
	 */
	default boolean isEqualTo(int index, byte[] array) {
		return isEqualTo(index, array, 0);
	}

	/**
	 * Returns true if bytes from index are equal to array bytes.
	 */
	default boolean isEqualTo(int index, byte[] array, int offset) {
		return isEqualTo(index, array, offset, array.length - offset);
	}

	/**
	 * Returns true if bytes from index are equal to array bytes.
	 */
	default boolean isEqualTo(int index, byte[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return false;
		while (length-- > 0)
			if (getByte(index++) != array[offset++]) return false;
		return true;
	}

	/**
	 * Returns true if bytes from index are equal to provider bytes.
	 */
	default boolean isEqualTo(int index, ByteProvider provider) {
		return isEqualTo(index, provider, 0);
	}

	/**
	 * Returns true if bytes from index are equal to provider bytes.
	 */
	default boolean isEqualTo(int index, ByteProvider provider, int offset) {
		return isEqualTo(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns true if bytes from index are equal to provider bytes.
	 */
	default boolean isEqualTo(int index, ByteProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return false;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return false;
		while (length-- > 0)
			if (getByte(index++) != provider.getByte(offset++)) return false;
		return true;
	}

	/**
	 * Returns the first index that matches array bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, int... array) {
		return indexOf(index, ArrayUtil.bytes(array));
	}

	/**
	 * Returns the first index that matches array bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, byte[] array) {
		return indexOf(index, array, 0);
	}

	/**
	 * Returns the first index that matches array bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, byte[] array, int offset) {
		return indexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the first index that matches array bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, byte[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, array, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the first index that matches provider bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, ByteProvider provider) {
		return indexOf(index, provider, 0);
	}

	/**
	 * Returns the first index that matches provider bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, ByteProvider provider, int offset) {
		return indexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the first index that matches provider bytes. Returns -1 if no match.
	 */
	default int indexOf(int index, ByteProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (; index <= length() - length; index++)
			if (isEqualTo(index, provider, offset, length)) return index;
		return -1;
	}

	/**
	 * Returns the last index that matches array bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, int... array) {
		return lastIndexOf(index, ArrayUtil.bytes(array));
	}

	/**
	 * Returns the last index that matches array bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, byte[] array) {
		return lastIndexOf(index, array, 0);
	}

	/**
	 * Returns the last index that matches array bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, byte[] array, int offset) {
		return lastIndexOf(index, array, offset, array.length - offset);
	}

	/**
	 * Returns the last index that matches array bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, byte[] array, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(array.length, offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, array, offset, length)) return i;
		return -1;
	}

	/**
	 * Returns the last index that matches provider bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, ByteProvider provider) {
		return lastIndexOf(index, provider, 0);
	}

	/**
	 * Returns the last index that matches provider bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, ByteProvider provider, int offset) {
		return lastIndexOf(index, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the last index that matches provider bytes. Returns -1 if no match.
	 */
	default int lastIndexOf(int index, ByteProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), index, length)) return -1;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		for (int i = length() - length; i >= index; i--)
			if (isEqualTo(i, provider, offset, length)) return i;
		return -1;
	}

	/**
	 * Provides sequential byte access.
	 */
	default Reader reader(int index) {
		return reader(index, length() - index);
	}

	/**
	 * Provides sequential byte access.
	 */
	default Reader reader(int index, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		return new Reader(this, index, length);
	}

	/**
	 * Provides a hex string representation.
	 */
	static String toHex(ByteProvider provider) {
		return toHex(provider, Integer.MAX_VALUE);
	}

	/**
	 * Provides a limited hex string representation.
	 */
	static String toHex(ByteProvider provider, int max) {
		return toString(provider, max, array -> ArrayUtil.toHex(array, 0, array.length));
	}

	/**
	 * Provides a string representation.
	 */
	static String toString(ByteProvider provider) {
		return toString(provider, Integer.MAX_VALUE);
	}

	/**
	 * Provides a limited string representation.
	 */
	static String toString(ByteProvider provider, int max) {
		return toString(provider, max, array -> ArrayUtil.toString(array, 0, array.length));
	}

	/**
	 * Provides a limited string representation.
	 */
	private static String toString(ByteProvider provider, int max, Function<byte[], String> fn) {
		int length = provider.length();
		var array = provider.copy(0, length <= max ? length : max - 1);
		String s = fn.apply(array);
		if (length > max) s = s.substring(0, s.length() - 1) + ", ...]";
		return s + "(" + length + ")";
	}

}
