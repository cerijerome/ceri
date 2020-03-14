package ceri.common.data;

import static ceri.common.text.StringUtil.HEX_RADIX;
import static ceri.common.validation.ValidationUtil.validateMax;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.text.StringUtil;
import ceri.common.util.ExceptionUtil;

public class ByteUtil {
	public static final int BITS_PER_NYBBLE = 4;
	public static final int HEX_DIGIT_BITS = BITS_PER_NYBBLE;
	public static final int BYTE_MASK = 0xff;
	public static final int SHORT_MASK = 0xffff;
	public static final long INT_MASK = 0xffff_ffff;
	public static final long LONG_MASK = 0xffff_ffff_ffff_ffffL;

	private ByteUtil() {}

	/**
	 * Writes hex bytes to receiver. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, ByteReceiver data) {
		return fromHex(hex, data, 0);
	}

	/**
	 * Writes hex bytes to receiver. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, ByteReceiver data, int offset) {
		return fromHex(hex, data, offset, data.length() - offset);
	}

	/**
	 * Writes hex bytes to receiver. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, ByteReceiver data, int offset, int len) {
		if (hex == null || len == 0) return offset + len;
		byte[] bytes = new BigInteger(hex, HEX_RADIX).toByteArray();
		offset = data.fill(0, offset, Math.max(0, len - bytes.length));
		int pos = Math.max(0, bytes.length - len);
		return data.copyFrom(offset, bytes, pos, bytes.length - pos);
	}

	/**
	 * Writes hex bytes to array. Bytes are right-aligned or left-truncated as needed to fit the
	 * length. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, byte[] data) {
		return fromHex(hex, data, 0);
	}

	/**
	 * Writes hex bytes to array. Bytes are right-aligned or left-truncated as needed to fit the
	 * length. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, byte[] data, int offset) {
		return fromHex(hex, data, offset, data.length - offset);
	}

	/**
	 * Writes hex bytes to array. Bytes are right-aligned or left-truncated as needed to fit the
	 * length. Remove any delimiters before calling this.
	 */
	public static int fromHex(String hex, byte[] data, int offset, int len) {
		return fromHex(hex, ByteReceiver.wrap(data), offset, len);
	}

	/**
	 * Converts hex string to a fixed-length byte array. Bytes are right-aligned or left-truncated
	 * as needed to fit the specified length. Remove any delimiters before calling this.
	 */
	public static ImmutableByteArray fromHex(String hex, int len) {
		if (hex == null) return null;
		if (len == 0) return ImmutableByteArray.EMPTY;
		ImmutableByteArray bytes = fromHex(hex);
		if (bytes.length() == len) return bytes;
		if (bytes.length() > len) return bytes.slice(bytes.length() - len);
		return bytes.resize(-len); // right-justify on resize
	}

	/**
	 * Converts hex string to its minimal byte array. Remove any delimiters before calling this.
	 */
	public static ImmutableByteArray fromHex(String hex) {
		if (hex == null) return null;
		if (hex.isEmpty()) return ImmutableByteArray.EMPTY;
		byte[] array = new BigInteger(hex, HEX_RADIX).toByteArray(); // may have extra leading byte
		return ImmutableByteArray.wrap(array, array[0] == 0 ? 1 : 0); // remove leading byte
	}

	public static String toHex(ByteProvider array, String delimiter) {
		return toHex(array, 0, delimiter);
	}

	public static String toHex(ByteProvider array, int offset, String delimiter) {
		if (array == null) return null;
		return toHex(array, offset, array.length() - offset, delimiter);
	}

	public static String toHex(ByteProvider array, int offset, int len, String delimiter) {
		if (array == null) return null;
		return toHex(array.stream(offset, len), delimiter);
	}

	public static String toHex(byte[] array, String delimiter) {
		return toHex(array, 0, delimiter);
	}

	public static String toHex(byte[] array, int offset, String delimiter) {
		if (array == null) return null;
		return toHex(array, offset, array.length - offset, delimiter);
	}

	public static String toHex(byte[] array, int offset, int len, String delimiter) {
		if (array == null) return null;
		return toHex(streamOf(array, offset, len), delimiter);
	}

	private static String toHex(IntStream stream, String delimiter) {
		return stream.mapToObj(b -> StringUtil.toHex((byte) b))
			.collect(Collectors.joining(delimiter));
	}

	public static IntStream streamOf(byte... array) {
		return streamOf(array, 0);
	}

	public static IntStream streamOf(byte[] array, int offset) {
		return streamOf(array, offset, array.length - offset);
	}

	public static IntStream streamOf(byte[] array, int offset, int len) {
		return IntStream.range(offset, offset + len).map(i -> array[i] & 0xff);
	}

	public static byte[] toByteArray(Collection<Integer> values) {
		return toByteArray(values.stream());
	}

	public static byte[] toByteArray(Stream<Integer> stream) {
		return toByteArray(stream.mapToInt(Integer::intValue));
	}

	public static byte[] toByteArray(IntStream stream) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		stream.forEach(out::write);
		return out.toByteArray();
	}

	public static void writeTo(ByteArrayOutputStream out, int... bytes) {
		writeTo(out, ArrayUtil.bytes(bytes));
	}

	public static void writeTo(ByteArrayOutputStream out, byte... bytes) {
		ExceptionUtil.shouldNotThrow(() -> out.write(bytes));
	}

	public static void writeTo(ByteArrayOutputStream out, byte[] bytes, int offset) {
		out.write(bytes, offset, bytes.length - offset);
	}

	public static void writeTo(ByteArrayOutputStream out, ByteProvider b) {
		ExceptionUtil.shouldNotThrow(() -> b.writeTo(out));
	}

	public static void writeTo(ByteArrayOutputStream out, ByteProvider b, int offset) {
		ExceptionUtil.shouldNotThrow(() -> b.writeTo(out, offset));
	}

	public static void writeTo(ByteArrayOutputStream out, ByteProvider b, int offset, int length) {
		ExceptionUtil.shouldNotThrow(() -> b.writeTo(out, offset, length));
	}

	public static ImmutableByteArray toAscii(String s) {
		return ImmutableByteArray.wrap(s.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static int toAscii(String s, ByteReceiver data) {
		return toAscii(s, data, 0);
	}

	public static int toAscii(String s, ByteReceiver data, int offset) {
		return data.copyFrom(offset, s.getBytes(StandardCharsets.ISO_8859_1));
	}

	public static String fromAscii(int... data) {
		return fromAscii(ArrayUtil.bytes(data));
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

	public static String fromAscii(ByteProvider data) {
		return fromAscii(data, 0);
	}

	public static String fromAscii(ByteProvider data, int offset) {
		return fromAscii(data, offset, data.length() - offset);
	}

	public static String fromAscii(ByteProvider data, int offset, int length) {
		return fromAscii(data.copy(), offset, length);
	}

	public static long apply(long value, long mask, boolean on) {
		return on ? value | mask : value & (~mask);
	}

	public static int applyInt(int value, int mask, boolean on) {
		return on ? value | mask : value & (~mask);
	}

	public static int maskInt(int bitCount) {
		if (bitCount == 0) return 0;
		if (bitCount >= Integer.SIZE) return -1;
		return (1 << bitCount) - 1;
	}

	public static int maskInt(int startBit, int bitCount) {
		return maskInt(bitCount) << startBit;
	}

	public static long mask(int bitCount) {
		if (bitCount == 0) return 0;
		if (bitCount >= Long.SIZE) return -1L;
		return (1L << bitCount) - 1;
	}

	public static long mask(int startBit, int bitCount) {
		return mask(bitCount) << startBit;
	}

	public static long maskOfBits(Collection<Integer> bits) {
		if (bits == null) return 0;
		return maskOfBits(ArrayUtil.ints(bits));
	}

	public static long maskOfBits(int... bits) {
		if (bits == null) return 0;
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
		return writeBigEndian(value, ByteReceiver.wrap(data), offset, length);
	}

	public static int writeBigEndian(long value, ByteReceiver data) {
		return writeBigEndian(value, data, 0);
	}

	public static int writeBigEndian(long value, ByteReceiver data, int offset) {
		return writeBigEndian(value, data, offset, data.length() - offset);
	}

	public static int writeBigEndian(long value, ByteReceiver data, int offset, int length) {
		ArrayUtil.validateSlice(data.length(), offset, length);
		validateMax(length, Long.BYTES);
		for (int i = 0; i < length; i++)
			data.set(offset + i, byteAt(value, length - i - 1));
		return offset + length;
	}

	public static int writeLittleEndian(long value, byte[] data) {
		return writeLittleEndian(value, data, 0);
	}

	public static int writeLittleEndian(long value, byte[] data, int offset) {
		return writeLittleEndian(value, data, offset, data.length - offset);
	}

	public static int writeLittleEndian(long value, byte[] data, int offset, int length) {
		return writeLittleEndian(value, ByteReceiver.wrap(data), offset, length);
	}

	public static int writeLittleEndian(long value, ByteReceiver data) {
		return writeLittleEndian(value, data, 0);
	}

	public static int writeLittleEndian(long value, ByteReceiver data, int offset) {
		return writeLittleEndian(value, data, offset, data.length() - offset);
	}

	public static int writeLittleEndian(long value, ByteReceiver data, int offset, int length) {
		ArrayUtil.validateSlice(data.length(), offset, length);
		validateMax(length, Long.BYTES);
		for (int i = 0; i < length; i++)
			data.set(offset + i, byteAt(value, i));
		return offset + length;
	}

	public static long fromBigEndian(ByteProvider array) {
		return fromBigEndian(array, 0);
	}

	public static long fromBigEndian(ByteProvider array, int offset) {
		return fromBigEndian(array, offset, array.length() - offset);
	}

	public static long fromBigEndian(ByteProvider array, int offset, int length) {
		ArrayUtil.validateSlice(array.length(), offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array.get(offset + i), length - i - 1);
		return value;
	}

	public static long fromBigEndian(int... array) {
		return fromBigEndian(ArrayUtil.bytes(array));
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

	public static long fromLittleEndian(ByteProvider array) {
		return fromLittleEndian(array, 0);
	}

	public static long fromLittleEndian(ByteProvider array, int offset) {
		return fromLittleEndian(array, offset, array.length() - offset);
	}

	public static long fromLittleEndian(ByteProvider array, int offset, int length) {
		ArrayUtil.validateSlice(array.length(), offset, length);
		validateMax(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array.get(offset + i), i);
		return value;
	}

	public static long fromLittleEndian(int... array) {
		return fromLittleEndian(ArrayUtil.bytes(array));
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
		return (byte) reverse(value, Byte.SIZE);
	}

	public static short reverse(short value) {
		return (short) reverse(value, Short.SIZE);
	}

	public static int reverseInt(int value, int bits) {
		return Integer.reverse(value) >>> (Integer.SIZE - bits);
	}

	public static long reverse(long value, int bits) {
		return Long.reverse(value) >>> (Long.SIZE - bits);
	}

	public static int fill(byte[] array, int fill, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		for (int i = 0; i < length; i++)
			array[offset + i] = (byte) fill;
		return offset + length;
	}

	/**
	 * Alignment for padding.
	 */
	public enum Align {
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
		if (array.length >= length) return array;
		byte[] bytes = new byte[length];
		int count = length - array.length;
		int fillOffset = align == Align.LEFT ? 0 : array.length;
		int copyOffset = align == Align.LEFT ? count : 0;
		if (padByte != 0) fill(bytes, padByte, fillOffset, count);
		System.arraycopy(array, 0, bytes, copyOffset, array.length);
		return bytes;
	}

}
