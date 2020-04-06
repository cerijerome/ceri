package ceri.common.data;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.BasicUtil;

/**
 * Interface that writes bytes sequentially. Type T must be the sub-class type; this allows fluent
 * method calls without redefining all the methods with covariant return types.
 * <p>
 * For bulk efficiency, consider overriding these methods that process one byte at a time, or make a
 * sub-array copy:
 * 
 * <pre>
 * T skip(int length); [1-byte]
 * T writeEndian(long value, int size, boolean msb); [copy]
 * T writeString(String s, Charset charset); [copy]
 * T fill(int length, int value); [1-byte]
 * T writeFrom(byte[] dest, int offset, int length); [1-byte]
 * T writeFrom(ByteProvider provider, int offset, int length); [1-byte]
 * int transferFrom(InputStream in, int length) throws IOException; [1-byte]
 * </pre>
 * 
 * @see ceri.common.data.NavigableByteReader
 */
public interface ByteWriter<T extends ByteWriter<T>> {

	/**
	 * Writes a byte. May throw unchecked exception if no capacity to write.
	 */
	T writeByte(int value);

	/**
	 * Skips bytes. By default this writes bytes with value 0.
	 */
	default T skip(int length) {
		return fill(length, 0);
	}

	/**
	 * Writes byte 1 for true, 0 for false.
	 */
	default T writeBool(boolean value) {
		return writeByte(value ? 1 : 0);
	}

	/**
	 * Writes native-order bytes.
	 */
	default T writeShort(int value) {
		return writeEndian(value, Short.BYTES, BIG_ENDIAN);
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeShortMsb(int value) {
		return writeEndian(value, Short.BYTES, true);
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeShortLsb(int value) {
		return writeEndian(value, Short.BYTES, false);
	}

	/**
	 * Writes native-order bytes.
	 */
	default T writeInt(int value) {
		return writeEndian(value, Integer.BYTES, BIG_ENDIAN);
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeIntMsb(int value) {
		return writeEndian(value, Integer.BYTES, true);
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeIntLsb(int value) {
		return writeEndian(value, Integer.BYTES, false);
	}

	/**
	 * Writes native-order bytes.
	 */
	default T writeLong(long value) {
		return writeEndian(value, Long.BYTES, BIG_ENDIAN);
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeLongMsb(long value) {
		return writeEndian(value, Long.BYTES, true);
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeLongLsb(long value) {
		return writeEndian(value, Long.BYTES, false);
	}

	/**
	 * Writes native-order bytes.
	 */
	default T writeFloat(float value) {
		return writeInt(Float.floatToIntBits(value));
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeFloatMsb(float value) {
		return writeIntMsb(Float.floatToIntBits(value));
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeFloatLsb(float value) {
		return writeIntLsb(Float.floatToIntBits(value));
	}

	/**
	 * Writes native-order bytes.
	 */
	default T writeDouble(double value) {
		return writeLong(Double.doubleToLongBits(value));
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeDoubleMsb(double value) {
		return writeLongMsb(Double.doubleToLongBits(value));
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeDoubleLsb(double value) {
		return writeLongLsb(Double.doubleToLongBits(value));
	}

	/**
	 * Writes endian bytes. Default implementation creates and writes a byte array; efficiency may
	 * be improved by overriding.
	 */
	default T writeEndian(long value, int size, boolean msb) {
		byte[] bytes = msb ? ByteUtil.toMsb(value, size) : ByteUtil.toLsb(value, size);
		return writeFrom(bytes);
	}

	/**
	 * Writes the string as ISO-Latin-1 bytes.
	 */
	default T writeAscii(String s) {
		return writeString(s, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Writes the string as UTF-8 bytes.
	 */
	default T writeUtf8(String s) {
		return writeString(s, StandardCharsets.UTF_8);
	}

	/**
	 * Writes the string as default character-set bytes.
	 */
	default T writeString(String s) {
		return writeString(s, Charset.defaultCharset());
	}

	/**
	 * Writes the string from character-set encoded bytes. Default implementation reads a copy of
	 * the bytes required; efficiency may be improved by overriding.
	 */
	default T writeString(String s, Charset charset) {
		byte[] bytes = s.getBytes(charset);
		return writeFrom(bytes);
	}

	/**
	 * Fill bytes with same value. Default implementation fills one byte at a time; efficiency may
	 * be improved by overriding.
	 */
	default T fill(int length, int value) {
		while (length-- > 0)
			writeByte(value);
		return BasicUtil.uncheckedCast(this);
	}

	/**
	 * Writes bytes from array.
	 */
	default T writeFrom(int... array) {
		return writeFrom(ArrayUtil.bytes(array));
	}

	/**
	 * Writes bytes from array.
	 */
	default T writeFrom(byte[] array) {
		return writeFrom(array, 0);
	}

	/**
	 * Writes bytes from array.
	 */
	default T writeFrom(byte[] array, int offset) {
		return writeFrom(array, offset, array.length - offset);
	}

	/**
	 * Writes bytes from array. Default implementation writes one byte at a time; efficiency may be
	 * improved by overriding.
	 */
	default T writeFrom(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		for (int i = 0; i < length; i++)
			writeByte(array[offset + i]);
		return BasicUtil.uncheckedCast(this);
	}

	/**
	 * Writes bytes from byte provider.
	 */
	default T writeFrom(ByteProvider provider) {
		return writeFrom(provider, 0);
	}

	/**
	 * Writes bytes from byte provider.
	 */
	default T writeFrom(ByteProvider provider, int offset) {
		return writeFrom(provider, offset, provider.length() - offset);
	}

	/**
	 * Writes bytes from byte provider. Default implementation writes one byte at a time; efficiency
	 * may be improved by overriding.
	 */
	default T writeFrom(ByteProvider provider, int offset, int length) {
		ArrayUtil.validateSlice(provider.length(), offset, length);
		for (int i = 0; i < length; i++)
			writeByte(provider.getByte(offset + i));
		return BasicUtil.uncheckedCast(this);
	}

	/**
	 * Writes bytes from the input stream, and returns the number of bytes transferred. Default
	 * implementation transfers one byte at a time; efficiency may be improved by overriding, or
	 * calling:
	 * 
	 * <pre>
	 * return transferBufferFrom(this, in, length);
	 * </pre>
	 */
	default int transferFrom(InputStream in, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			int value = in.read();
			if (value < 0) return i;
			writeByte(value);
		}
		return length;
	}

	/**
	 * Reads bytes from the input stream and writes bytes to writer. The number of bytes read may be
	 * less than requested if EOF occurs. Returns the number of bytes written. Implementing classes
	 * can call this in transferFrom() if buffering is more efficient.
	 */
	static int transferBufferFrom(ByteWriter<?> writer, InputStream in, int length)
		throws IOException {
		byte[] buffer = in.readNBytes(length); // < length if EOF
		writer.writeFrom(buffer);
		return buffer.length;
	}

}
