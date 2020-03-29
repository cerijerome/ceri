package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import static java.lang.Math.min;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.common.collection.ArrayUtil;

/**
 * Interface for receiving bytes into an array. For bulk efficiency, consider overriding the
 * following methods that process one byte at a time, or copy arrays.
 * 
 * <pre>
 * int setEndian(int index, long value, int size, boolean msb); [copy]
 * int fill(int index, int value, int length); [1-byte]
 * int copyFrom(int index, byte[] array, int offset, int length); [1-byte]
 * int copyFrom(int index, ByteProvider provider, int offset, int length); [1-byte]
 * int readFrom(int index, InputStream in, int length) throws IOException; [1-byte]
 * </pre>
 * 
 * @see ceri.common.data.MutableByteArray
 * @see ceri.common.concurrent.VolatileByteArray
 * @see ceri.dmx.spi.device.SpiPulseDevice
 * @see ceri.serial.spi.pulse.PulseBuffer
 * @see ceri.serial.spi.pulse.SpiPulseTransmitter
 */
public interface ByteReceiver {
	static final ByteReceiver EMPTY = ByteArray.Mutable.EMPTY;
	
	/**
	 * Length of the space to receive bytes.
	 */
	int length();

	/**
	 * Sets the byte value at given index, returns index + 1.
	 */
	int setByte(int index, int value);

	/**
	 * Creates a byte provider sub-view. A negative length will right-justify the view. Returns the
	 * current provider for zero index and same length.
	 */
	ByteReceiver slice(int index, int length);

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
		return setEndian(index, value, Short.BYTES, BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setShortMsb(int index, int value) {
		return setEndian(index, value, Short.BYTES, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setShortLsb(int index, int value) {
		return setEndian(index, value, Short.BYTES, false);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setInt(int index, int value) {
		return setEndian(index, value, Integer.BYTES, BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setIntMsb(int index, int value) {
		return setEndian(index, value, Integer.BYTES, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setIntLsb(int index, int value) {
		return setEndian(index, value, Integer.BYTES, false);
	}

	/**
	 * Sets the value in native-order bytes at the index. Returns the index after the written bytes.
	 */
	default int setLong(int index, long value) {
		return setEndian(index, value, Long.BYTES, BIG_ENDIAN);
	}

	/**
	 * Sets the value in big-endian bytes at the index. Returns the index after the written bytes.
	 */
	default int setLongMsb(int index, long value) {
		return setEndian(index, value, Long.BYTES, true);
	}

	/**
	 * Sets the value in little-endian bytes at the index. Returns the index after the written
	 * bytes.
	 */
	default int setLongLsb(int index, long value) {
		return setEndian(index, value, Long.BYTES, false);
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
	default int setEndian(int index, long value, int size, boolean msb) {
		byte[] bytes = msb ? ByteUtil.toMsb(value, size) : ByteUtil.toLsb(value, size);
		return copyFrom(index, bytes);
	}

	/**
	 * Encodes string as ISO-Latin-1 bytes. Returns the index after the written bytes.
	 */
	default int setAscii(String s) {
		return setAscii(0, s);
	}

	/**
	 * Encodes string as ISO-Latin-1 bytes from index. Returns the index after the written bytes.
	 */
	default int setAscii(int index, String s) {
		return setString(index, s, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Encodes string as UTF-8 bytes. Returns the index after the written bytes.
	 */
	default int setUtf8(String s) {
		return setUtf8(0, s);
	}

	/**
	 * Encodes string as UTF-8 bytes from index. Returns the index after the written bytes.
	 */
	default int setUtf8(int index, String s) {
		return setString(index, s, StandardCharsets.UTF_8);
	}

	/**
	 * Encodes string as bytes using the default character set. Returns the index after the written
	 * bytes.
	 */
	default int setString(String s) {
		return setString(0, s);
	}

	/**
	 * Encodes string as bytes from index using the default character set. Returns the index after
	 * the written bytes.
	 */
	default int setString(int index, String s) {
		return setString(index, s, Charset.defaultCharset());
	}

	/**
	 * Encodes string as bytes using the character set. Returns the index after the written bytes.
	 */
	default int setString(String s, Charset charset) {
		return setString(0, s, charset);
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
	 * Creates a byte provider view from index.
	 */
	default ByteReceiver slice(int index) {
		return slice(index, length() - index);
	}

	/**
	 * Fills bytes with the same value. Returns the index after the written bytes.
	 */
	default int fill(int value) {
		return fill(0, value);
	}

	/**
	 * Fills bytes from the index with the same value. Returns the index after the written bytes.
	 */
	default int fill(int index, int value) {
		return fill(index, value, length() - index);
	}

	/**
	 * Fills bytes from the index with the same value. Returns the index after the written bytes.
	 * Default implementation copies one byte at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int fill(int index, int value, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setByte(index++, value);
		return index;
	}

	/**
	 * Copies bytes from the array. Returns the index after the written bytes.
	 */
	default int copyFrom(int... array) {
		return copyFrom(ArrayUtil.bytes(array), 0);
	}

	/**
	 * Copies bytes from the array. Returns the index after the written bytes.
	 */
	default int copyFrom(byte[] array) {
		return copyFrom(array, 0);
	}

	/**
	 * Copies bytes from the array. Returns the index after the written bytes.
	 */
	default int copyFrom(byte[] array, int offset) {
		return copyFrom(array, offset, min(length(), array.length - offset));
	}

	/**
	 * Copies bytes from the array. Returns the index after the written bytes.
	 */
	default int copyFrom(byte[] array, int offset, int length) {
		return copyFrom(0, array, offset, length);
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
		ArrayUtil.validateSlice(array.length, offset, length);
		ArrayUtil.validateSlice(length(), index, length);
		while (length-- > 0)
			setByte(index++, array[offset++]);
		return index;
	}

	/**
	 * Copies bytes from the provider. Returns the index after the written bytes.
	 */
	default int copyFrom(ByteProvider array) {
		return copyFrom(array, 0);
	}

	/**
	 * Copies bytes from the provider. Returns the index after the written bytes.
	 */
	default int copyFrom(ByteProvider array, int offset) {
		return copyFrom(0, array, offset);
	}

	/**
	 * Copies bytes from the provider. Returns the index after the written bytes.
	 */
	default int copyFrom(ByteProvider array, int offset, int length) {
		return copyFrom(0, array, offset, length);
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
		return copyFrom(index, array, offset, min(length() - index, array.length() - offset));
	}

	/**
	 * Copies bytes from the provider to the index. Returns the index after the written bytes.
	 * Default implementation copies one byte at a time; efficiency may be improved by overriding
	 * this method.
	 */
	default int copyFrom(int index, ByteProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(length(), index, length);
		ArrayUtil.validateSlice(provider.length(), offset, length);
		while (length-- > 0)
			setByte(index++, provider.getByte(offset++));
		return index;
	}

	/**
	 * Reads bytes from the input stream and writes to the receiver. The number of bytes read may be
	 * less than requested if EOF occurs. Returns the index after the written bytes.
	 */
	default int readFrom(InputStream in) throws IOException {
		return readFrom(0, in);
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
		ArrayUtil.validateSlice(length(), index, length);
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
		ArrayUtil.validateSlice(receiver.length(), index, length);
		byte[] buffer = in.readNBytes(length); // < length if EOF
		return receiver.copyFrom(index, buffer);
	}

}
