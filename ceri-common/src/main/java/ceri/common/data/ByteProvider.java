package ceri.common.data;

import java.io.IOException;
import java.io.OutputStream;
import ceri.common.collection.ArrayUtil;

/**
 * Interface that provides access to bytes in an array.
 * 
 * For bulk efficiency, consider overriding these methods:
 * 
 * <pre>
 * void writeTo(int srcOffset, ByteReceiver dest, int destOffset, int length)
 * void writeTo(OutputStream out, int offset, int length) throws IOException
 * </pre>
 */
public interface ByteProvider {

	static ByteProvider of(byte[] array) {
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
			public void writeTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
				ArrayUtil.validateSlice(dest.length(), destOffset, length);
				ArrayUtil.validateSlice(length(), srcOffset, length);
				dest.set(destOffset, array, srcOffset, length);
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
		copyTo(copy, offset, length);
		return copy;
	}

	/**
	 * Copies this array from offset 0 to the given array.
	 */
	default int copyTo(byte[] dest) {
		return copyTo(dest, 0);
	}

	/**
	 * Copies this array from offset 0 to the given array slice.
	 */
	default int copyTo(byte[] dest, int destOffset) {
		return copyTo(dest, destOffset, dest.length - destOffset);
	}

	/**
	 * Copies this array from offset 0 to the given array slice.
	 */
	default int copyTo(byte[] dest, int destOffset, int length) {
		return copyTo(0, dest, destOffset, length);
	}

	/**
	 * Copies this array from offset to the given array slice.
	 */
	default int copyTo(int srcOffset, byte[] dest, int destOffset) {
		return copyTo(srcOffset, dest, destOffset, dest.length - destOffset);
	}

	/**
	 * Copies this array from offset to the given array slice.
	 */
	default int copyTo(int srcOffset, byte[] dest, int destOffset, int length) {
		writeTo(destOffset, ByteReceiver.of(dest), srcOffset, length);
		return destOffset + length;
	}

	/**
	 * Writes this array from offset 0 to the given receiver.
	 */
	default void writeTo(ByteReceiver dest) {
		writeTo(dest, 0);
	}

	/**
	 * Writes this array from offset 0 to the given receiver offset.
	 */
	default void writeTo(ByteReceiver dest, int destOffset) {
		writeTo(dest, destOffset, dest.length() - destOffset);
	}

	/**
	 * Writes this array from offset 0 to the given receiver offset.
	 */
	default void writeTo(ByteReceiver dest, int destOffset, int length) {
		writeTo(0, dest, destOffset, length);
	}

	/**
	 * Writes this array from offset to the given receiver.
	 */
	default void writeTo(int srcOffset, ByteReceiver dest) {
		writeTo(srcOffset, dest, 0);
	}

	/**
	 * Writes this array from offset to the given receiver offset.
	 */
	default void writeTo(int srcOffset, ByteReceiver dest, int destOffset) {
		writeTo(srcOffset, dest, destOffset, dest.length() - destOffset);
	}

	/**
	 * Writes this array from offset to the given receiver offset. Writes one byte at a time; in
	 * some cases efficiency may be improved by overriding this method.
	 */
	default void writeTo(int srcOffset, ByteReceiver dest, int destOffset, int length) {
		ArrayUtil.validateSlice(length(), srcOffset, length);
		ArrayUtil.validateSlice(dest.length(), destOffset, length);
		for (int i = 0; i < length; i++)
			dest.set(destOffset + i, get(srcOffset + i));
	}

	/**
	 * Writes this array from offset 0 to the output stream.
	 */
	default void writeTo(OutputStream out) throws IOException {
		writeTo(out, 0);
	}

	/**
	 * Writes this array from offset to the output stream.
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

}
