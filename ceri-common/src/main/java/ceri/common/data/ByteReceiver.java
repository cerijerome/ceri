package ceri.common.data;

import java.io.IOException;
import java.io.InputStream;
import ceri.common.collection.ArrayUtil;

/**
 * Interface for receiving bytes into an array.
 * 
 * For bulk efficiency, consider overriding these methods:
 * 
 * <pre>
 * void set(int pos, byte[] data, int offset, int length)
 * void fill(int value, int pos, int length)
 * int readFrom(InputStream in, int offset, int length) throws IOException
 * </pre>
 */
public interface ByteReceiver {

	/**
	 * Wraps a byte array as a byte receiver.
	 */
	static ByteReceiver of(byte[] array) {
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
			public void set(int pos, byte[] data, int offset, int length) {
				System.arraycopy(data, offset, array, pos, length);
			}

			@Override
			public int readFrom(InputStream in, int offset, int length) throws IOException {
				ArrayUtil.validateSlice(length(), offset, length);
				return in.read(array, offset, length);
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
	 * Sets the byte at offset 0.
	 */
	default void set(int b) {
		set(0, b);
	}

	/**
	 * Sets the byte array at offset 0.
	 */
	default void set(byte[] data) {
		set(data, 0);
	}

	/**
	 * Sets the byte array slice at offset 0.
	 */
	default void set(byte[] data, int offset) {
		set(data, offset, data.length - offset);
	}

	/**
	 * Sets the byte array slice at offset 0.
	 */
	default void set(byte[] data, int offset, int length) {
		set(0, data, offset, length);
	}

	/**
	 * Sets the byte array at given position.
	 */
	default void set(int pos, byte[] data) {
		set(pos, data, 0);
	}

	/**
	 * Sets the byte array slice at given position.
	 */
	default void set(int pos, byte[] data, int offset) {
		set(pos, data, offset, data.length - offset);
	}

	/**
	 * Sets the byte array slice at given position. Sets one byte at a time; in some cases
	 * efficiency may be improved by overriding this method.
	 */
	default void set(int pos, byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		ArrayUtil.validateSlice(length(), pos, length);
		for (int i = 0; i < length; i++)
			set(pos + i, data[offset + i]);
	}

	/**
	 * Fills bytes with given value.
	 */
	default void fill(int value) {
		fill(value, 0);
	}
	
	/**
	 * Fills bytes at the given position.
	 */
	default void fill(int value, int pos) {
		fill(value, pos, length() - pos);
	}
	
	/**
	 * Fills bytes at the given position.
	 */
	default void fill(int value, int pos, int length) {
		ArrayUtil.validateSlice(length(), pos, length);
		for (int i = 0; i < length; i++)
			set(pos + i, value);
	}

	/**
	 * Sets bytes at offset 0 by reading from the input stream.
	 */
	default int readFrom(InputStream out) throws IOException {
		return readFrom(out, 0);
	}

	/**
	 * Sets bytes at offset by reading from the input stream.
	 */
	default int readFrom(InputStream in, int offset) throws IOException {
		return readFrom(in, offset, length() - offset);
	}

	/**
	 * Sets bytes at offset by reading from the input stream. Returns the number of bytes read.
	 * Default implementation reads one byte at a time; in some cases efficiency may be improved by
	 * overriding, or by using a buffer:
	 * 
	 * <pre>
	 * byte[] buffer = new byte[length];
	 * int n = in.read(buffer)
	 * set(offset, buffer, 0, n);
	 * </pre>
	 */
	default int readFrom(InputStream in, int offset, int length) throws IOException {
		ArrayUtil.validateSlice(length(), offset, length);
		for (int i = 0; i < length; i++)
			set(offset + i, in.read());
		return length;
	}

}
