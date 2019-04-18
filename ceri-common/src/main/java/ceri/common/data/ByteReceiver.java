package ceri.common.data;

import static java.lang.Math.max;
import static java.lang.Math.min;
import java.io.IOException;
import java.io.InputStream;
import ceri.common.collection.ArrayUtil;

/**
 * Interface for receiving bytes into an array.
 * 
 * For bulk efficiency, consider overriding these methods:
 * 
 * <pre>
 * int copy(int pos, byte[] data, int offset, int length)
 * int fill(int value, int pos, int length)
 * int readFrom(InputStream in, int offset, int length) throws IOException
 * </pre>
 */
public interface ByteReceiver {
	static ByteReceiver EMPTY = wrap();
	
	/**
	 * Wraps a byte array as a byte receiver.
	 */
	static ByteReceiver wrap(byte... array) {
		return new ByteReceiver() {
			@Override
			public int length() {
				return array.length;
			}

			@Override
			public void set(int pos, int b) {
				array[pos] = (byte) b;
			}

			@Override
			public int copyFrom(int pos, byte[] data, int offset, int length) {
				System.arraycopy(data, offset, array, pos, length);
				return pos + length;
			}

			@Override
			public int readFrom(InputStream in, int offset, int length) throws IOException {
				ArrayUtil.validateSlice(length(), offset, length);
				int n = in.read(array, offset, length);
				return offset + max(n, 0);
			}
		};
	}

	/**
	 * Length of the space to receive bytes.
	 */
	int length();

	/**
	 * Sets the byte value at given position.
	 */
	void set(int pos, int b);

	/**
	 * Copies the byte array to position 0. Length copied is the minimum of available source and
	 * destination lengths. Returns the length copied.
	 */
	default int copyFrom(int... array) {
		return copyFrom(ByteUtil.bytes(array));
	}

	/**
	 * Copies the byte array to position 0. Length copied is the minimum of available source and
	 * destination lengths. Returns the length copied.
	 */
	default int copyFrom(byte... array) {
		return copyFrom(array, 0);
	}

	/**
	 * Copies the byte array slice to position 0. Length copied is the minimum of available source
	 * and destination lengths. Returns the length copied.
	 */
	default int copyFrom(byte[] array, int offset) {
		return copyFrom(array, offset, min(length(), array.length - offset));
	}

	/**
	 * Copies the byte array slice to position 0. Returns the length.
	 */
	default int copyFrom(byte[] array, int offset, int length) {
		return copyFrom(0, array, offset, length);
	}

	/**
	 * Copies the byte array to given position. Length copied is the minimum of available source and
	 * destination lengths. Returns the array position after copying, pos + length.
	 */
	default int copyFrom(int pos, byte[] array) {
		return copyFrom(pos, array, 0);
	}

	/**
	 * Copies the byte array slice to given position. Length copied is the minimum of available
	 * source and destination lengths. Returns the array position after copying, pos + length.
	 */
	default int copyFrom(int pos, byte[] array, int offset) {
		return copyFrom(pos, array, offset, min(length() - pos, array.length - offset));
	}

	/**
	 * Copies the byte array slice to given position. Returns the array position after copying, pos
	 * + length. Default implementation copies one byte at a time; in some cases efficiency may be
	 * improved by overriding this method and using system array copy.
	 */
	default int copyFrom(int pos, byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		ArrayUtil.validateSlice(length(), pos, length);
		for (int i = 0; i < length; i++)
			set(pos + i, array[offset + i]);
		return pos + length;
	}

	/**
	 * Fills the array. Returns the number of bytes filled, length().
	 */
	default int fill(int value) {
		return fill(value, 0);
	}

	/**
	 * Fills bytes from the given position. Returns the array position after copying, length().
	 */
	default int fill(int value, int pos) {
		return fill(value, pos, length() - pos);
	}

	/**
	 * Fills bytes at the given position. Returns the array position after filling, pos + length.
	 */
	default int fill(int value, int pos, int length) {
		ArrayUtil.validateSlice(length(), pos, length);
		for (int i = 0; i < length; i++)
			set(pos + i, value);
		return pos + length;
	}

	/**
	 * Sets bytes at offset 0 by reading from the input stream. Attempts to fill the length of the
	 * array. Returns the number of bytes read. Number of bytes read may be
	 * less than requested; EOF will result in fewer or no bytes read rather than returning -1.
	 */
	default int readFrom(InputStream in) throws IOException {
		return readFrom(in, 0);
	}

	/**
	 * Sets bytes at offset by reading from the input stream. Attempts to fill the remaining length
	 * of the array. Returns the array position after reading, offset + length. Number of bytes read
	 * may be less than requested; EOF will result in fewer or no bytes read rather than returning
	 * -1.
	 */
	default int readFrom(InputStream in, int offset) throws IOException {
		return readFrom(in, offset, length() - offset);
	}

	/**
	 * Sets bytes at offset by reading from the input stream. Returns the array position after
	 * reading, offset + length. Number of bytes read may be less than requested; EOF will result in
	 * fewer or no bytes read rather than returning -1. Default implementation reads one byte at a
	 * time; in some cases efficiency may be improved by overriding, or by reading into a buffer and
	 * calling set:
	 * 
	 * <pre>
	 * byte[] buffer = new byte[length];
	 * int n = in.read(buffer);
	 * if (n > 0) set(offset, buffer, 0, n);
	 * </pre>
	 */
	default int readFrom(InputStream in, int offset, int length) throws IOException {
		ArrayUtil.validateSlice(length(), offset, length);
		int i = 0;
		for (; i < length; i++) {
			int val = in.read();
			if (val == -1) break;
			set(offset + i, val);
		}
		return offset + i;
	}

}
