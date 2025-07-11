package ceri.common.data;

import static ceri.common.data.ByteUtil.IS_BIG_ENDIAN;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import ceri.common.collection.ArrayUtil.Empty;
import ceri.common.math.MathUtil;
import ceri.common.validation.ValidationUtil;

/**
 * Interface that provides sequential access to bytes. Reads are of known length, or require a given
 * length. For bulk efficiency, consider overriding these methods that process one byte at a time,
 * or make a sub-array copy:
 *
 * <pre>
 * ByteReader skip(int length); [1-byte]
 * long readEndian(int size, boolean msb); [copy]
 * String readString(int length, Charset charset); [copy]
 * int readInto(byte[] dest, int offset, int length); [1-byte]
 * int readInto(ByteReceiver receiver, int offset, int length); [1-byte]
 * ByteReader transferTo(OutputStream out, int length) throws IOException; [1-byte]
 * </pre>
 *
 * @see ceri.common.data.ByteProvider.Reader
 * @see ceri.common.data.ByteStream.Reader
 */
public interface ByteReader {

	/**
	 * Returns the next byte value. May throw unchecked exception if no bytes remain.
	 */
	byte readByte();

	/**
	 * Skip a number of bytes. Default implementation skips one byte at a time; efficiency may be
	 * improved by overriding.
	 */
	default ByteReader skip(int length) {
		while (length-- > 0)
			readByte();
		return this;
	}

	/**
	 * Returns the unsigned byte value.
	 */
	default short readUbyte() {
		return MathUtil.ubyte(readByte());
	}

	/**
	 * Returns true if byte is non-zero.
	 */
	default boolean readBool() {
		return readByte() != 0;
	}

	/**
	 * Returns the value from native-order bytes.
	 */
	default short readShort() {
		return (short) readEndian(Short.BYTES, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default short readShortMsb() {
		return (short) readEndian(Short.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default short readShortLsb() {
		return (short) readEndian(Short.BYTES, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes.
	 */
	default int readUshort() {
		return MathUtil.ushort(readShort());
	}

	/**
	 * Returns the unsigned value from big-endian bytes.
	 */
	default int readUshortMsb() {
		return MathUtil.ushort(readShortMsb());
	}

	/**
	 * Returns the unsigned value from little-endian bytes.
	 */
	default int readUshortLsb() {
		return MathUtil.ushort(readShortLsb());
	}

	/**
	 * Returns the value from native-order bytes.
	 */
	default int readInt() {
		return (int) readEndian(Integer.BYTES, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default int readIntMsb() {
		return (int) readEndian(Integer.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default int readIntLsb() {
		return (int) readEndian(Integer.BYTES, false);
	}

	/**
	 * Returns the unsigned value from native-order bytes.
	 */
	default long readUint() {
		return MathUtil.uint(readInt());
	}

	/**
	 * Returns the unsigned value from big-endian bytes.
	 */
	default long readUintMsb() {
		return MathUtil.uint(readIntMsb());
	}

	/**
	 * Returns the unsigned value from little-endian bytes.
	 */
	default long readUintLsb() {
		return MathUtil.uint(readIntLsb());
	}

	/**
	 * Returns the value from native-order bytes.
	 */
	default long readLong() {
		return readEndian(Long.BYTES, IS_BIG_ENDIAN);
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default long readLongMsb() {
		return readEndian(Long.BYTES, true);
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default long readLongLsb() {
		return readEndian(Long.BYTES, false);
	}

	/**
	 * Returns the value from native-order bytes.
	 */
	default float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default float readFloatMsb() {
		return Float.intBitsToFloat(readIntMsb());
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default float readFloatLsb() {
		return Float.intBitsToFloat(readIntLsb());
	}

	/**
	 * Returns the value from native-order bytes.
	 */
	default double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default double readDoubleMsb() {
		return Double.longBitsToDouble(readLongMsb());
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default double readDoubleLsb() {
		return Double.longBitsToDouble(readLongLsb());
	}

	/**
	 * Returns the value from endian bytes. Default implementation reads a copy of the bytes
	 * required; efficiency may be improved by overriding.
	 */
	default long readEndian(int size, boolean msb) {
		byte[] bytes = readBytes(size);
		return msb ? ByteUtil.fromMsb(bytes) : ByteUtil.fromLsb(bytes);
	}

	/**
	 * Returns the string from ISO-Latin-1 bytes.
	 */
	default String readAscii(int length) {
		return readString(length, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Returns the string from UTF-8 bytes.
	 */
	default String readUtf8(int length) {
		return readString(length, StandardCharsets.UTF_8);
	}

	/**
	 * Returns the string from default character-set bytes.
	 */
	default String readString(int length) {
		return readString(length, Charset.defaultCharset());
	}

	/**
	 * Returns the string from character-set encoded bytes. Default implementation reads a copy of
	 * the bytes required; efficiency may be improved by overriding.
	 */
	default String readString(int length, Charset charset) {
		byte[] bytes = readBytes(length);
		return new String(bytes, charset);
	}

	/**
	 * Reads a copied array of bytes.
	 */
	default byte[] readBytes(int length) {
		if (length == 0) return Empty.BYTES;
		byte[] copy = new byte[length];
		readInto(copy);
		return copy;
	}

	/**
	 * Reads bytes into array. Returns the array offset after reading.
	 */
	default int readInto(byte[] array) {
		return readInto(array, 0);
	}

	/**
	 * Reads bytes into array. Returns the array offset after reading.
	 */
	default int readInto(byte[] array, int offset) {
		return readInto(array, offset, array.length - offset);
	}

	/**
	 * Reads bytes into array. Returns the array offset after reading. Default implementation reads
	 * one byte at a time; efficiency may be improved by overriding.
	 */
	default int readInto(byte[] array, int offset, int length) {
		ValidationUtil.validateSlice(array.length, offset, length);
		while (length-- > 0)
			array[offset++] = readByte();
		return offset;
	}

	/**
	 * Reads bytes into byte receiver. Returns the receiver offset after reading.
	 */
	default int readInto(ByteReceiver receiver) {
		return readInto(receiver, 0);
	}

	/**
	 * Reads bytes into byte receiver. Returns the receiver offset after reading.
	 */
	default int readInto(ByteReceiver receiver, int offset) {
		return readInto(receiver, offset, receiver.length() - offset);
	}

	/**
	 * Reads bytes into byte receiver. Returns the receiver offset after reading. Default
	 * implementation reads one byte at a time; efficiency may be improved by overriding.
	 */
	default int readInto(ByteReceiver receiver, int offset, int length) {
		ValidationUtil.validateSlice(receiver.length(), offset, length);
		while (length-- > 0)
			receiver.setByte(offset++, readByte());
		return offset;
	}

	/**
	 * Transfers bytes to the output stream, and returns the number of bytes transferred. Default
	 * implementation transfers one byte at a time; efficiency may be improved by overriding, or
	 * calling:
	 *
	 * <pre>
	 * return transferBufferTo(this, out, length);
	 * </pre>
	 */
	default int transferTo(OutputStream out, int length) throws IOException {
		for (int i = 0; i < length; i++)
			out.write(readByte());
		return length;
	}

	/**
	 * Transfers bytes from the reader to the output stream. Returns the number of bytes
	 * transferred. Implementing classes can call this in transferTo() if buffering is more
	 * efficient.
	 */
	static int transferBufferTo(ByteReader reader, OutputStream out, int length)
		throws IOException {
		byte[] buffer = reader.readBytes(length);
		out.write(buffer);
		return buffer.length;
	}

	/**
	 * Provides unsigned bytes as a stream, starting at offset, for given length.
	 */
	default IntStream ustream(int length) {
		return IntStream.range(0, length).map(_ -> readUbyte());
	}

}
