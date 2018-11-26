package ceri.common.data;

import static ceri.common.text.StringUtil.BYTE_HEX_DIGITS;
import static ceri.common.text.StringUtil.HEX_RADIX;
import static ceri.common.util.BasicUtil.shouldNotThrow;
import static ceri.common.validation.ValidationUtil.validateMax;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;

public class ByteUtil {
	public static final int BITS_PER_NYBBLE = 4;
	public static final int HEX_DIGIT_BITS = BITS_PER_NYBBLE;
	public static final int BYTE_MASK = 0xff;
	public static final int SHORT_MASK = 0xffff;
	public static final long INT_MASK = 0xffff_ffff;
	public static final long LONG_MASK = 0xffff_ffff_ffff_ffffL;
	private static final Pattern HEX_SPLIT_REGEX =
		RegexUtil.compile("(?<=\\G.{%d})", BYTE_HEX_DIGITS);

	private ByteUtil() {}

	public static ImmutableByteArray fromHex(String hex) {
		if (hex == null) return null;
		if (hex.isEmpty()) return ImmutableByteArray.EMPTY;
		if (hex.length() % BYTE_HEX_DIGITS != 0) hex += "0"; // pad the end
		return ImmutableByteArray.wrap(
			Stream.of(HEX_SPLIT_REGEX.split(hex)).mapToInt(s -> Integer.parseInt(s, HEX_RADIX)));
	}

	public static String toHex(ImmutableByteArray array) {
		return toHex(array, " ");
	}
	
	public static String toHex(ImmutableByteArray array, String delimiter) {
		if (array == null) return null;
		return array.stream().mapToObj(b -> StringUtil.toHex((byte) b))
			.collect(Collectors.joining(delimiter));
	}

	public static IntStream streamOf(byte... array) {
		return IntStream.range(0, array.length).map(i -> array[i] & 0xff);
	}

	public static byte[] toByteArray(IntStream stream) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		stream.forEach(b -> out.write(b));
		return out.toByteArray();
	}

	public static void writeTo(ByteArrayOutputStream out, int... bytes) {
		writeTo(out, bytes(bytes));
	}

	public static void writeTo(ByteArrayOutputStream out, byte... bytes) {
		shouldNotThrow(() -> out.write(bytes));
	}

	public static void writeTo(ByteArrayOutputStream out, byte[] bytes, int offset) {
		out.write(bytes, offset, bytes.length - offset);
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b) {
		shouldNotThrow(() -> b.writeTo(out));
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b, int offset) {
		shouldNotThrow(() -> b.writeTo(out, offset));
	}

	public static void writeTo(ByteArrayOutputStream out, ImmutableByteArray b, int offset,
		int length) {
		shouldNotThrow(() -> b.writeTo(out, offset, length));
	}

	public static ImmutableByteArray toAscii(String s) {
		return ImmutableByteArray.wrap(s.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static String fromAscii(int... data) {
		return fromAscii(bytes(data));
	}

	public static String fromAscii(byte... data) {
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

	public static byte[] bytes(int... values) {
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) values[i];
		return bytes;
	}

	public static long mask(int bitCount) {
		if (bitCount == 0) return 0;
		if (bitCount == Long.SIZE) return -1L;
		return (1L << bitCount) - 1;
	}

	public static long maskOfBits(int... bits) {
		long value = 0;
		for (int bit : bits)
			if (bit < Long.SIZE) value |= 1L << bit;
		return value;
	}

	public static long maskOfBit(boolean flag, int bit) {
		if (!flag) return 0;
		return 1L << bit;
	}

	public static boolean bit(long value, int bit) {
		return (value & (1L << bit)) != 0;
	}

	public static byte[] toBigEndian(short value) {
		return toBigEndian(value, Short.BYTES);
	}

	public static byte[] toBigEndian(int value) {
		return toBigEndian(value, Integer.BYTES);
	}

	public static byte[] toBigEndian(long value) {
		return toBigEndian(value, Long.BYTES);
	}

	public static byte[] toBigEndian(long value, int length) {
		byte[] data = new byte[length];
		writeBigEndian(value, data, 0, length);
		return data;
	}

	public static byte[] toLittleEndian(short value) {
		return toLittleEndian(value, Short.BYTES);
	}

	public static byte[] toLittleEndian(int value) {
		return toLittleEndian(value, Integer.BYTES);
	}

	public static byte[] toLittleEndian(long value) {
		return toLittleEndian(value, Long.BYTES);
	}

	public static byte[] toLittleEndian(long value, int length) {
		byte[] data = new byte[length];
		writeLittleEndian(value, data, 0, length);
		return data;
	}

	public static int writeBigEndian(long value, byte[] data) {
		return writeBigEndian(value, data, 0);
	}

	public static int writeBigEndian(long value, byte[] data, int offset) {
		return writeBigEndian(value, data, offset, data.length - offset);
	}

	public static int writeBigEndian(long value, byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		validateMax(length, Long.BYTES);
		for (int i = 0; i < length; i++)
			data[offset + i] = byteAt(value, length - i - 1);
		return offset + length;
	}

	public static int writeLittleEndian(long value, byte[] data) {
		return writeLittleEndian(value, data, 0);
	}

	public static int writeLittleEndian(long value, byte[] data, int offset) {
		return writeLittleEndian(value, data, offset, data.length - offset);
	}

	public static int writeLittleEndian(long value, byte[] data, int offset, int length) {
		ArrayUtil.validateSlice(data.length, offset, length);
		validateMax(length, Long.BYTES);
		for (int i = 0; i < length; i++)
			data[offset + i] = byteAt(value, i);
		return offset + length;
	}

	public static long fromBigEndian(ImmutableByteArray array) {
		return fromBigEndian(array, 0);
	}

	public static long fromBigEndian(ImmutableByteArray array, int offset) {
		return fromBigEndian(array, offset, array.length - offset);
	}

	public static long fromBigEndian(ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array.at(offset + i), length - i - 1);
		return value;
	}

	public static long fromBigEndian(int... array) {
		return fromBigEndian(bytes(array));
	}

	public static long fromBigEndian(byte... array) {
		return fromBigEndian(array, 0);
	}

	public static long fromBigEndian(byte[] array, int offset) {
		return fromBigEndian(array, offset, array.length - offset);
	}

	public static long fromBigEndian(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array[offset + i], length - i - 1);
		return value;
	}

	public static long fromLittleEndian(ImmutableByteArray array) {
		return fromLittleEndian(array, 0);
	}

	public static long fromLittleEndian(ImmutableByteArray array, int offset) {
		return fromLittleEndian(array, offset, array.length - offset);
	}

	public static long fromLittleEndian(ImmutableByteArray array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array.at(offset + i), i);
		return value;
	}

	public static long fromLittleEndian(int... array) {
		return fromLittleEndian(bytes(array));
	}

	public static long fromLittleEndian(byte... array) {
		return fromLittleEndian(array, 0);
	}

	public static long fromLittleEndian(byte[] array, int offset) {
		return fromLittleEndian(array, offset, array.length - offset);
	}

	public static long fromLittleEndian(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array[offset + i], i);
		return value;
	}

	public static int byteValueAt(long value, int byteOffset) {
		return byteAt(value, byteOffset) & 0xff;
	}
	
	public static byte byteAt(long value, int byteOffset) {
		if (byteOffset == 0) return (byte) value;
		return (byte) shift(value, byteOffset);
	}

	public static long shiftByteLeft(long value, int bytes) {
		return shift(value & BYTE_MASK, -bytes);
	}

	public static short shift(short value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	public static int shift(int value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	public static long shift(long value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	public static byte shiftBits(byte value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Byte.SIZE) return 0;
		return (byte) (bits > 0 ? (value & BYTE_MASK) >>> bits : value << -bits);
	}

	public static short shiftBits(short value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Short.SIZE) return 0;
		return (short) (bits > 0 ? (value & SHORT_MASK) >>> bits : value << -bits);
	}

	public static int shiftBits(int value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Integer.SIZE) return 0;
		return bits > 0 ? value >>> bits : value << -bits;
	}

	public static long shiftBits(long value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Long.SIZE) return 0;
		return bits > 0 ? value >>> bits : value << -bits;
	}

	public static byte invert(byte value) {
		return (byte) (~value & BYTE_MASK);
	}

	public static short invert(short value) {
		return (short) (~value & SHORT_MASK);
	}

	public static byte reverse(byte value) {
		return (byte) (Integer.reverse(value) >>> (Integer.SIZE - Byte.SIZE));
	}

	public static short reverse(short value) {
		return (short) (Integer.reverse(value) >>> (Integer.SIZE - Short.SIZE));
	}

	public static void fill(byte[] array, int fill, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		for (int i = 0; i < length; i++)
			array[offset + i] = (byte) fill;
	}

	/**
	 * Alignment for padding.
	 */
	public static enum Align {
		LEFT,
		RIGHT
	}

	public static byte[] padL(byte[] array, int length) {
		return padL(array, length, 0);
	}

	public static byte[] padL(byte[] array, int length, int padByte) {
		return pad(array, padByte, length, Align.LEFT);
	}

	public static byte[] padR(byte[] array, int length) {
		return padR(array, length, 0);
	}

	public static byte[] padR(byte[] array, int length, int padByte) {
		return pad(array, padByte, length, Align.RIGHT);
	}

	private static byte[] pad(byte[] array, int padByte, int length, Align align) {
		if (length == 0 || array.length >= length) return array;
		byte[] bytes = new byte[length];
		int count = length - array.length;
		int fillOffset = align == Align.LEFT ? 0 : array.length;
		int copyOffset = align == Align.LEFT ? count : 0;
		if (padByte != 0) fill(bytes, padByte, fillOffset, count);
		System.arraycopy(array, 0, bytes, copyOffset, array.length);
		return bytes;
	}

}
