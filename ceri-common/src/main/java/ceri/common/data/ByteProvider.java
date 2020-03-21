package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import static java.lang.Math.min;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.math.MathUtil;

/**
 * Interface that provides positional access to bytes. For bulk efficiency, consider overriding the
 * following methods that process one byte at a time, make a sub-array copy, or defer to
 * ByteProvider/ByteReceiver instead of byte arrays.
 * 
 * <pre>
 * long getEndian(int index, int size, boolean msb); [copy]
 * String getString(int index, int length, Charset charset); [copy]
 * ByteProvider slice(int offset, int length); [copy]
 * int copyTo(int srcOffset, byte[] dest, int destOffset, int length); [1-byte]
 * int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length); [1-byte]
 * int writeTo(int offset, OutputStream out, int length) throws IOException; [1-byte]
 * boolean matches(int srcOffset, byte[] array, int offset, int length); [1-byte]
 * boolean matches(int srcOffset, ByteProvider provider, int offset, int length); [1-byte]
 * int indexOf(int srcOffset, byte[] array, int offset, int length); [1-byte]
 * int indexOf(int srcOffset, ByteProvider provider, int offset, int length); [1-byte]
 * </pre>
 * 
 * @see ceri.common.collection.ImmutableByteArray
 * @see ceri.common.concurrent.AtomicByteArray
 */
public interface ByteProvider {

	/**
	 * Length of the array.
	 */
	int length();

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
	 * Returns the value from endian bytes at given index.
	 */
	default long getEndian(int index, int size, boolean msb) {
		byte[] copy = copy(index, size);
		return msb ? ByteUtil.fromMsb(copy) : ByteUtil.fromLsb(copy);
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
	 * Returns the string from ISO-Latin-1 bytes.
	 */
	default String getAscii() {
		return getAscii(0);
	}

	/**
	 * Returns the string from ISO-Latin-1 bytes.
	 */
	default String getAscii(int index) {
		return getAscii(index, length() - index);
	}

	/**
	 * Returns the string from ISO-Latin-1 bytes.
	 */
	default String getAscii(int index, int length) {
		return getString(index, length, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Returns the string from UTF-8 bytes.
	 */
	default String getUtf8() {
		return getUtf8(0);
	}

	/**
	 * Returns the string from UTF-8 bytes.
	 */
	default String getUtf8(int index) {
		return getUtf8(index, length() - index);
	}

	/**
	 * Returns the string from UTF-8 bytes.
	 */
	default String getUtf8(int index, int length) {
		return getString(index, length, StandardCharsets.UTF_8);
	}

	/**
	 * Returns the string from default character-set encoded bytes.
	 */
	default String getString() {
		return getString(0);
	}

	/**
	 * Returns the string from default character-set encoded bytes.
	 */
	default String getString(int index) {
		return getString(index, length() - index);
	}

	/**
	 * Returns the string from default character-set encoded bytes.
	 */
	default String getString(int index, int length) {
		return getString(index, length, Charset.defaultCharset());
	}

	/**
	 * Returns the string from character-set encoded bytes.
	 */
	default String getString(Charset charset) {
		return getString(0, charset);
	}

	/**
	 * Returns the string from character-set encoded bytes.
	 */
	default String getString(int index, Charset charset) {
		return getString(index, length() - index, charset);
	}

	/**
	 * Returns the string from character-set encoded bytes.
	 */
	default String getString(int index, int length, Charset charset) {
		byte[] copy = copy(index, length);
		return new String(copy, charset);
	}

	/**
	 * Returns a copy of the array.
	 */
	default byte[] copy() {
		return copy(0);
	}

	/**
	 * Returns a copy of the array slice.
	 */
	default byte[] copy(int offset) {
		return copy(offset, length() - offset);
	}

	/**
	 * Returns a copy of the array slice.
	 */
	default byte[] copy(int offset, int length) {
		ArrayUtil.validateSlice(length(), offset, length);
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		byte[] copy = new byte[length];
		copyTo(offset, copy, 0, length);
		return copy;
	}

	/**
	 * Create a new view of the array.
	 */
	default ByteProvider slice(int offset) {
		return slice(offset, length() - offset);
	}

	/**
	 * Create a new view of the array. Use a negative length to right-justify the view.
	 */
	default ByteProvider slice(int offset, int length) {
		if (length == 0) return ImmutableByteArray.EMPTY;
		if (length < 0) return slice(offset + length, -length);
		ArrayUtil.validateSlice(length(), offset, length);
		if (offset == 0 && length == length()) return this;
		return ImmutableByteArray.wrap(copy(offset, length));
	}

	/**
	 * Copies this array from offset 0 to the given array. Length copied is the minimum of available
	 * source and destination lengths. Returns the length copied.
	 */
	default int copyTo(byte[] dest) {
		return copyTo(dest, 0);
	}

	/**
	 * Copies this array from offset 0 to the given array slice. Length copied is the minimum of
	 * available source and destination lengths. Returns the destination position after copying,
	 * destOffset + length.
	 */
	default int copyTo(byte[] dest, int destOffset) {
		return copyTo(0, dest, destOffset);
	}

	/**
	 * Copies this array from offset 0 to the given array slice. Returns the destination position
	 * after copying, destOffset + length.
	 */
	default int copyTo(byte[] dest, int destOffset, int length) {
		return copyTo(0, dest, destOffset, length);
	}

	/**
	 * Copies this array from offset to the given array. Length copied is the minimum of available
	 * source and destination lengths. Returns the length copied.
	 */
	default int copyTo(int srcOffset, byte[] dest) {
		return copyTo(srcOffset, dest, 0);
	}

	/**
	 * Copies this array from offset to the given array slice. Length copied is the minimum of
	 * available source and destination lengths. Returns the destination position after copying,
	 * destOffset + length.
	 */
	default int copyTo(int srcOffset, byte[] dest, int destOffset) {
		return copyTo(srcOffset, dest, destOffset,
			min(length() - srcOffset, dest.length - destOffset));
	}

	/**
	 * Copies this array from offset to the given array slice. Returns the destination position
	 * after copying, destOffset + length.
	 */
	default int copyTo(int srcOffset, byte[] dest, int destOffset, int length) {
		ArrayUtil.validateSlice(length(), srcOffset, length);
		ArrayUtil.validateSlice(dest.length, destOffset, length);
		for (int i = 0; i < length; i++)
			dest[destOffset + i] = getByte(srcOffset + i);
		return destOffset + length;
	}

	/**
	 * Writes this array from offset 0 to the given receiver. Length written is the minimum of
	 * available source and destination lengths. Returns the length written.
	 */
	default int copyTo(ByteReceiver dest) {
		return copyTo(dest, 0);
	}

	/**
	 * Writes this array from offset 0 to the given receiver offset. Length written is the minimum
	 * of available source and destination lengths. Returns the destination position after write,
	 * destOffset + length.
	 */
	default int copyTo(ByteReceiver dest, int destOffset) {
		return copyTo(0, dest, destOffset);
	}

	/**
	 * Writes this array from offset 0 to the given receiver offset. Returns the destination
	 * position after write, destOffset + length.
	 */
	default int copyTo(ByteReceiver dest, int destOffset, int length) {
		return copyTo(0, dest, destOffset, length);
	}

	/**
	 * Writes this array from offset to the given receiver. Length written is the minimum of
	 * available source and destination lengths. Returns the length written.
	 */
	default int copyTo(int srcOffset, ByteReceiver dest) {
		return copyTo(srcOffset, dest, 0);
	}

	/**
	 * Writes this array from offset to the given receiver offset. Length written is the minimum of
	 * available source and destination lengths. Returns the destination position after write,
	 * destOffset + length.
	 */
	default int copyTo(int srcOffset, ByteReceiver dest, int destOffset) {
		return copyTo(srcOffset, dest, destOffset,
			min(length() - srcOffset, dest.length() - destOffset));
	}

	/**
	 * Writes this array from offset to the given receiver offset. Returns the destination position
	 * after write, destOffset + length. Writes one byte at a time; in some cases efficiency may be
	 * improved by overriding this method.
	 */
	default int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
		ArrayUtil.validateSlice(length(), srcOffset, length);
		ArrayUtil.validateSlice(dest.length(), destOffset, length);
		for (int i = 0; i < length; i++)
			dest.set(destOffset + i, getByte(srcOffset + i));
		return destOffset + length;
	}

	/**
	 * Writes this array from offset 0 to the output stream. Returns the length written.
	 */
	default int writeTo(OutputStream out) throws IOException {
		return writeTo(out, length());
	}

	/**
	 * Writes this array from offset 0 to the output stream. Returns the length written.
	 */
	default int writeTo(OutputStream out, int length) throws IOException {
		return writeTo(0, out, length);
	}

	/**
	 * Writes this array from offset to the output stream. Returns the position after write,
	 * length().
	 */
	default int writeTo(int offset, OutputStream out) throws IOException {
		return writeTo(offset, out, length() - offset);
	}

	/**
	 * Writes this array from offset to the output stream, and returns the offset after writing.
	 * Default implementation writes one byte at a time; in some cases efficiency may be improved by
	 * overriding, or by using:
	 * 
	 * <pre>
	 * out.write(copy(offset, length))
	 * </pre>
	 */
	default int writeTo(int offset, OutputStream out, int length) throws IOException {
		ArrayUtil.validateSlice(length(), offset, length);
		for (int i = 0; i < length; i++)
			out.write(getByte(offset + i));
		return offset + length;
	}

	/**
	 * Provides unsigned bytes as a stream.
	 */
	default IntStream ustream() {
		return ustream(0);
	}

	/**
	 * Provides unsigned bytes as a stream, starting at offset.
	 */
	default IntStream ustream(int offset) {
		return ustream(offset, length() - offset);
	}

	/**
	 * Provides unsigned bytes as a stream, starting at offset, for given length.
	 */
	default IntStream ustream(int offset, int length) {
		ArrayUtil.validateSlice(length(), offset, length);
		return IntStream.range(offset, offset + length).map(i -> getUbyte(i));
	}

	/**
	 * Determines whether bytes equal the byte array.
	 */
	default boolean matches(int... array) {
		return matches(ArrayUtil.bytes(array));
	}

	/**
	 * Determines whether bytes equal the byte array.
	 */
	default boolean matches(byte... array) {
		return matches(array, 0);
	}

	/**
	 * Determines whether bytes equal the byte array slice.
	 */
	default boolean matches(byte[] array, int offset) {
		return matches(0, array, offset);
	}

	/**
	 * Determines whether bytes equal the byte array slice.
	 */
	default boolean matches(byte[] array, int offset, int length) {
		return matches(0, array, offset, length);
	}

	/**
	 * Determines whether bytes from offset equal the byte array.
	 */
	default boolean matches(int srcOffset, byte[] array) {
		return matches(srcOffset, array, 0);
	}

	/**
	 * Determines whether bytes from offset equal the byte array slice.
	 */
	default boolean matches(int srcOffset, byte[] array, int offset) {
		return matches(srcOffset, array, offset, array.length - offset);
	}

	/**
	 * Determines whether bytes from offset equal the byte array slice.
	 */
	default boolean matches(int srcOffset, byte[] array, int offset, int length) {
		return matches(srcOffset, ImmutableByteArray.wrap(array), offset, length);
	}

	/**
	 * Determines whether bytes equal the byte provider array.
	 */
	default boolean matches(ByteProvider provider) {
		return matches(provider, 0);
	}

	/**
	 * Determines whether bytes equal the byte provider array slice.
	 */
	default boolean matches(ByteProvider provider, int offset) {
		return matches(0, provider, offset);
	}

	/**
	 * Determines whether bytes equal the byte provider array slice.
	 */
	default boolean matches(ByteProvider provider, int offset, int length) {
		return matches(0, provider, offset, length);
	}

	/**
	 * Determines whether bytes from offset equal the byte provider array.
	 */
	default boolean matches(int srcOffset, ByteProvider provider) {
		return matches(srcOffset, provider, 0);
	}

	/**
	 * Determines whether bytes from offset equal the byte provider array slice.
	 */
	default boolean matches(int srcOffset, ByteProvider provider, int offset) {
		return matches(srcOffset, provider, offset, provider.length() - offset);
	}

	/**
	 * Determines whether bytes from offset equal the byte provider array slice.
	 */
	default boolean matches(int srcOffset, ByteProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(length(), srcOffset, length)) return false;
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return false;
		for (int i = 0; i < length; i++)
			if (getByte(srcOffset + i) != provider.getByte(offset + i)) return false;
		return true;
	}

	/**
	 * Returns the first index that matches byte array. Returns -1 if not match.
	 */
	default int indexOf(int... array) {
		return indexOf(ArrayUtil.bytes(array));
	}

	/**
	 * Returns the first index that matches byte array. Returns -1 if not match.
	 */
	default int indexOf(byte... array) {
		return indexOf(array, 0);
	}

	/**
	 * Returns the first index that matches byte array slice. Returns -1 if not match.
	 */
	default int indexOf(byte[] array, int offset) {
		return indexOf(0, array, offset);
	}

	/**
	 * Returns the first index that matches byte array slice. Returns -1 if not match.
	 */
	default int indexOf(byte[] array, int offset, int length) {
		return indexOf(0, array, offset, length);
	}

	/**
	 * Returns the first index from offset that matches byte array. Returns -1 if not match.
	 */
	default int indexOf(int srcOffset, byte[] array) {
		return indexOf(srcOffset, array, 0);
	}

	/**
	 * Returns the first index from offset that matches byte array slice. Returns -1 if not match.
	 */
	default int indexOf(int srcOffset, byte[] array, int offset) {
		return indexOf(srcOffset, array, offset, array.length - offset);
	}

	/**
	 * Returns the first index from offset that matches byte array slice. Returns -1 if not match.
	 */
	default int indexOf(int srcOffset, byte[] array, int offset, int length) {
		return indexOf(srcOffset, ImmutableByteArray.wrap(array), offset, length);
	}

	/**
	 * Returns the first index that matches byte provider array. Returns -1 if not match.
	 */
	default int indexOf(ByteProvider provider) {
		return indexOf(provider, 0);
	}

	/**
	 * Returns the first index that matches byte provider array slice. Returns -1 if not match.
	 */
	default int indexOf(ByteProvider provider, int offset) {
		return indexOf(0, provider, offset);
	}

	/**
	 * Returns the first index that matches byte provider array slice. Returns -1 if not match.
	 */
	default int indexOf(ByteProvider provider, int offset, int length) {
		return indexOf(0, provider, offset, length);
	}

	/**
	 * Returns the first index from offset that matches byte provider array. Returns -1 if not
	 * match.
	 */
	default int indexOf(int srcOffset, ByteProvider provider) {
		return indexOf(srcOffset, provider, 0);
	}

	/**
	 * Returns the first index from offset that matches byte provider array slice. Returns -1 if not
	 * match.
	 */
	default int indexOf(int srcOffset, ByteProvider provider, int offset) {
		return indexOf(srcOffset, provider, offset, provider.length() - offset);
	}

	/**
	 * Returns the first index from offset that matches byte provider array slice. Returns -1 if not
	 * match.
	 */
	default int indexOf(int srcOffset, ByteProvider provider, int offset, int length) {
		if (!ArrayUtil.isValidSlice(provider.length(), offset, length)) return -1;
		if (!ArrayUtil.isValidSlice(length(), srcOffset, length)) return -1;
		for (int i = srcOffset; i <= length() - length; i++)
			if (matches(i, provider, offset, length)) return i;
		return -1;
	}

}
