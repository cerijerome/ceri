package ceri.common.data;

import static java.lang.Math.min;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil;

/**
 * Interface that provides access to bytes in an array.
 * 
 * For bulk efficiency, consider overriding these methods that process one byte at a time:
 * 
 * <pre>
 * int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length)
 * int writeTo(OutputStream out, int offset, int length) throws IOException
 * </pre>
 */
public interface ByteProvider {
	ByteProvider EMPTY = wrap();
	
	static ByteProvider wrap(int... array) {
		return wrap(ArrayUtil.bytes(array));
	}

	static ByteProvider wrap(byte... array) {
		return new ByteProvider() {
			@Override
			public int length() {
				return array.length;
			}

			@Override
			public byte get(int index) {
				return array[index];
			}

			@Override
			public int copyTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
				ArrayUtil.validateSlice(length(), srcOffset, length);
				ArrayUtil.validateSlice(dest.length(), destOffset, length);
				dest.copyFrom(destOffset, array, srcOffset, length);
				return destOffset + length;
			}

			@Override
			public void writeTo(OutputStream out, int offset, int length) throws IOException {
				ArrayUtil.validateSlice(length(), offset, length);
				out.write(array, offset, length);
			}
		};
	}

	/**
	 * Length of the array.
	 */
	int length();

	/**
	 * Returns the byte at given index.
	 */
	byte get(int index);

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
		copyTo(srcOffset, ByteReceiver.wrap(dest), destOffset, length);
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
			dest.set(destOffset + i, get(srcOffset + i));
		return destOffset + length;
	}

	/**
	 * Writes this array from offset 0 to the output stream. Returns the length written.
	 */
	default void writeTo(OutputStream out) throws IOException {
		writeTo(out, 0);
	}

	/**
	 * Writes this array from offset to the output stream. Returns the position after write,
	 * length().
	 */
	default void writeTo(OutputStream out, int offset) throws IOException {
		writeTo(out, offset, length() - offset);
	}

	/**
	 * Writes this array from offset to the output stream. Writes one byte at a time; in some cases
	 * efficiency may be improved by overriding, or by using:
	 * 
	 * <pre>
	 * out.write(copy(offset, length))
	 * </pre>
	 */
	default void writeTo(OutputStream out, int offset, int length) throws IOException {
		ArrayUtil.validateSlice(length(), offset, length);
		for (int i = 0; i < length; i++)
			out.write(get(offset + i));
	}

	/**
	 * Provides the bytes as a stream.
	 */
	default IntStream stream() {
		return stream(0);
	}

	/**
	 * Provides the bytes as a stream, starting at offset.
	 */
	default IntStream stream(int offset) {
		return stream(offset, length() - offset);
	}

	/**
	 * Provides the bytes as a stream, starting at offset, for given length.
	 */
	default IntStream stream(int offset, int length) {
		ArrayUtil.validateSlice(length(), offset, length);
		return IntStream.range(offset, offset + length).map(i -> get(i) & 0xff);
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
		return matches(srcOffset, wrap(array), offset, length);
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
			if (get(srcOffset + i) != provider.get(offset + i)) return false;
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
		return indexOf(srcOffset, wrap(array), offset, length);
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
