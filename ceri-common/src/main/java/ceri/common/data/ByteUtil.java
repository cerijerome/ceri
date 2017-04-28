package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMax;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;

public class ByteUtil {
	public static final int BITS_PER_BYTE = 8;
	public static final int BYTE_MASK = 0xff;
	public static final int SHORT_BYTES = 2;
	public static final int INT_BYTES = 4;
	public static final int LONG_BYTES = 8;

	private ByteUtil() {}

	public static void writeTo(ByteArrayOutputStream out, byte... bytes) {
		try {
			out.write(bytes);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	public static void writeTo(ByteArrayOutputStream out, byte[] bytes, int offset) {
		out.write(bytes, offset, bytes.length - offset);
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b) {
		try {
			b.writeTo(out);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b, int offset) {
		try {
			b.writeTo(out, offset);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b, int offset,
		int length) {
		try {
			b.writeTo(out, offset, length);
		} catch (IOException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	public static byte[] toAscii(String s) {
		return s.getBytes(StandardCharsets.ISO_8859_1);
	}

	public static String fromAscii(byte[] data) {
		return fromAscii(data, 0);
	}

	public static String fromAscii(byte[] data, int offset) {
		return fromAscii(data, offset, data.length - offset);
	}

	public static String fromAscii(byte[] data, int offset, int length) {
		return new String(data, offset, length, StandardCharsets.ISO_8859_1);
	}

	public static String fromAscii(ImmutableByteArray data) {
		return fromAscii(data, 0);
	}

	public static String fromAscii(ImmutableByteArray data, int offset) {
		return fromAscii(data, offset, data.length - offset);
	}

	public static String fromAscii(ImmutableByteArray data, int offset, int length) {
		return fromAscii(data.copy(), offset, length);
	}

	public static ImmutableByteArray wrap(int... values) {
		return ImmutableByteArray.wrap(bytes(values));
	}
	
	public static byte[] bytes(int... values) {
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) values[i];
		return bytes;
	}

	public static int mask(int... bits) {
		int value = 0;
		for (int bit : bits)
			value |= 1 << bit;
		return value;
	}

	public static int mask(boolean flag, int bit) {
		if (!flag) return 0;
		return 1 << bit;
	}

	public static boolean bit(int value, int bit) {
		return (value & (1 << bit)) != 0;
	}

	public static byte[] toBigEndian(int value) {
		return toBigEndian(value, INT_BYTES);
	}

	public static byte[] toBigEndian(int value, int length) {
		byte[] data = new byte[length];
		writeBigEndian(value, data, 0, length);
		return data;
	}

	public static byte[] toLittleEndian(int value) {
		return toLittleEndian(value, INT_BYTES);
	}

	public static byte[] toLittleEndian(int value, int length) {
		byte[] data = new byte[length];
		writeLittleEndian(value, data, 0, length);
		return data;
	}

	public static int writeBigEndian(int value, byte[] data) {
		return writeBigEndian(value, data, 0);
	}

	public static int writeBigEndian(int value, byte[] data, int offset) {
		return writeBigEndian(value, data, offset, data.length - offset);
	}

	public static int writeBigEndian(int value, byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		validateMax(length, INT_BYTES);
		for (int i = 0; i < length; i++)
			data[offset + i] = byteAt(value, length - i - 1);
		return offset + length;
	}

	public static int writeLittleEndian(int value, byte[] data) {
		return writeLittleEndian(value, data, 0);
	}

	public static int writeLittleEndian(int value, byte[] data, int offset) {
		return writeLittleEndian(value, data, offset, data.length - offset);
	}

	public static int writeLittleEndian(int value, byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		validateMax(length, INT_BYTES);
		for (int i = 0; i < length; i++)
			data[offset + i] = byteAt(value, i);
		return offset + length;
	}

	public static int fromBigEndian(ImmutableByteArray array) {
		return fromBigEndian(array, 0);
	}

	public static int fromBigEndian(ImmutableByteArray array, int offset) {
		return fromBigEndian(array, offset, array.length - offset);
	}

	public static int fromBigEndian(ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, INT_BYTES);
		int value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftLeft(array.at(offset + i), length - i - 1);
		return value;
	}

	public static int fromBigEndian(byte... array) {
		return fromBigEndian(array, 0);
	}

	public static int fromBigEndian(byte[] array, int offset) {
		return fromBigEndian(array, offset, array.length - offset);
	}

	public static int fromBigEndian(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, INT_BYTES);
		int value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftLeft(array[offset + i], length - i - 1);
		return value;
	}

	public static int fromLittleEndian(ImmutableByteArray array) {
		return fromLittleEndian(array, 0);
	}

	public static int fromLittleEndian(ImmutableByteArray array, int offset) {
		return fromLittleEndian(array, offset, array.length - offset);
	}

	public static int fromLittleEndian(ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, INT_BYTES);
		int value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftLeft(array.at(offset + i), i);
		return value;
	}

	public static int fromLittleEndian(byte... array) {
		return fromLittleEndian(array, 0);
	}

	public static int fromLittleEndian(byte[] array, int offset) {
		return fromLittleEndian(array, offset, array.length - offset);
	}

	public static int fromLittleEndian(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, INT_BYTES);
		int value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftLeft(array[offset + i], i);
		return value;
	}

	public static byte byteAt(int value, int byteOffset) {
		if (byteOffset == 0) return (byte) value;
		return shiftRight(value, byteOffset);
	}

	public static int shiftLeft(int b, int bytes) {
		return (b & BYTE_MASK) << (BITS_PER_BYTE * bytes);
	}

	public static byte shiftRight(int b, int bytes) {
		return (byte) ((b >> (BITS_PER_BYTE * bytes)) & BYTE_MASK);
	}

	public static byte invert(byte b) {
		return (byte) (~b & 0xff);
	}

}
