package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
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
 * boolean matches(int index, byte[] array, int offset, int length); [1-byte]
 * boolean matches(int index, ByteProvider provider, int offset, int length); [1-byte]
 * </pre>
 * 
 * @see ceri.common.collection.ImmutableByteArray
 * @see ceri.common.concurrent.VolatileByteArray
 */
public interface ByteProvider {
	static final ByteProvider EMPTY = ByteArray.Immutable.EMPTY;

	/**
	 * Length of the array.
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
	 * Provides bytes as a hex string, using a delimiter.
	 */
	default String getHex(int index, String delimiter) {
		return toHex(index, length() - index, delimiter);
	}

	/**
	 * Provides bytes as a hex string, using a delimiter.
	 */
	default String toHex(int index, int length, String delimiter) {
		return ByteUtil.toHex(ustream(index, length), delimiter);
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
		if (length == 0) return ByteArray.Immutable.EMPTY;
		if (length < 0) return slice(index + length, -length);
		ArrayUtil.validateSlice(length(), index, length);
		if (index == 0 && length == length()) return this;
		return ByteArray.Immutable.wrap(copy(index, length));
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

}
