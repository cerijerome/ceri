package ceri.common.data;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Collectors;
import ceri.common.array.Array;
import ceri.common.collect.Iterators;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.math.Radix;
import ceri.common.stream.IntStream;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;
import ceri.common.text.Format;
import ceri.common.util.Validate;

public class Bytes {
	public static final int BITS_PER_NYBBLE = 4;
	public static final int HEX_DIGIT_BITS = BITS_PER_NYBBLE;
	public static final int BYTE_MASK = 0xff;
	public static final int SHORT_MASK = 0xffff;
	public static final long INT_MASK = 0xffff_ffffL;
	public static final long LONG_MASK = 0xffff_ffff_ffff_ffffL;
	public static final boolean IS_BIG_ENDIAN =
		Objects.equals(ByteOrder.nativeOrder(), ByteOrder.BIG_ENDIAN);

	private Bytes() {}

	/**
	 * Byte order as an enum.
	 */
	public enum Order {
		unspecified(null),
		platform(ByteOrder.nativeOrder()),
		big(ByteOrder.BIG_ENDIAN),
		little(ByteOrder.LITTLE_ENDIAN);

		public final ByteOrder order;

		public static String symbol(Order order) {
			return isSpecific(order) ? symbol(order.order) : "";
		}

		public static String symbol(ByteOrder order) {
			if (order == null) return "";
			return order == ByteOrder.BIG_ENDIAN ? ">" : "<";
		}

		public static boolean isValid(Order order) {
			return order != null && order != unspecified;
		}

		public static boolean isSpecific(Order order) {
			return order == big || order == little;
		}

		public static ByteOrder order(Order order) {
			return isSpecific(order) ? order.order : platform.order;
		}

		private Order(ByteOrder order) {
			this.order = order;
		}
	}

	/**
	 * Functional interface to iterate over mask bits and modify a value.
	 */
	public interface BitReducerLong<E extends Exception> {
		long applyAsLong(int bit, long value) throws E;
	}

	/**
	 * Functional interface to iterate over mask bits and modify a value.
	 */
	public interface BitReducerInt<E extends Exception> {
		int applyAsInt(int bit, int value) throws E;
	}

	/**
	 * Iterate bits of bytes. A true highBit iterates each byte from bit 7 to 0.
	 */
	public static Iterator<Boolean> bitIterator(boolean highBit, ByteProvider bytes) {
		Functions.IntOperator bitFn = highBit ? i -> Byte.SIZE - i - 1 : i -> i;
		return Iterators.indexed(bytes.length() * Byte.SIZE, i -> {
			int value = bytes.getByte(i / Byte.SIZE);
			int bit = bitFn.applyAsInt(i % Byte.SIZE);
			return bit(value, bit);
		});
	}

	/**
	 * Iterate over the set bits of a mask. A true highBit iterates down.
	 */
	public static <E extends Exception> void iterateMask(boolean highBit, int mask,
		Excepts.IntConsumer<E> consumer) throws E {
		int min = Integer.numberOfTrailingZeros(mask);
		int max = Integer.SIZE - Integer.numberOfLeadingZeros(mask) - 1;
		acceptMask(highBit, Maths.uint(mask), consumer, min, max);
	}

	/**
	 * Iterate over the set bits of a mask. A true highBit iterates down.
	 */
	public static <E extends Exception> void iterateMask(boolean highBit, long mask,
		Excepts.IntConsumer<E> consumer) throws E {
		int min = Long.numberOfTrailingZeros(mask);
		int max = Long.SIZE - Long.numberOfLeadingZeros(mask) - 1;
		acceptMask(highBit, mask, consumer, min, max);
	}

	/**
	 * Iterate over the set bits of a mask. A true highBit iterates down.
	 */
	public static <E extends Exception> long reduceMask(boolean highBit, long mask, long init,
		BitReducerLong<E> function) throws E {
		return applyMask(highBit, mask, init, function);
	}

	/**
	 * Iterate over the set bits of a mask. A true highBit iterates down.
	 */
	public static <E extends Exception> int reduceMaskInt(boolean highBit, int mask, int init,
		BitReducerInt<E> function) throws E {
		return applyMask(highBit, mask, init, function);
	}

	/**
	 * Converts hex string to its minimal byte array. Uses BigInteger.toByteArray, but removes
	 * leading 0 byte if it exists. Remove any delimiters before calling this.
	 */
	public static ByteProvider fromHex(String hex) {
		if (hex == null) return null;
		if (hex.isEmpty()) return ByteArray.Immutable.EMPTY;
		// may have extra leading byte
		byte[] array = new BigInteger(hex, Radix.HEX.n).toByteArray();
		return ByteArray.Immutable.wrap(array, array[0] == 0 ? 1 : 0);
	}

	/**
	 * Creates a hex string from bytes, with given delimiter.
	 */
	public static String toHex(byte[] array, String delimiter) {
		return toHex(array, 0, delimiter);
	}

	/**
	 * Creates a hex string from bytes, with given delimiter.
	 */
	public static String toHex(byte[] array, int offset, String delimiter) {
		if (array == null) return null;
		return toHex(array, offset, array.length - offset, delimiter);
	}

	/**
	 * Creates a hex string from bytes, with given delimiter.
	 */
	public static String toHex(byte[] array, int offset, int len, String delimiter) {
		if (array == null) return null;
		return toHex(ustream(array, offset, len), delimiter);
	}

	/**
	 * Creates a hex string from bytes, with given delimiter.
	 */
	public static <E extends Exception> String toHex(IntStream<E> stream, String delimiter)
		throws E {
		return stream.mapToObj(b -> Format.hex(b, "", Radix.HEX.digits.ubyte()))
			.collect(Collectors.joining(delimiter));
	}

	/**
	 * Stream array as unsigned bytes.
	 */
	public static IntStream<RuntimeException> ustream(int... array) {
		return Streams.slice(0, array.length).map(i -> Maths.ubyte(array[i]));
	}

	/**
	 * Stream array as unsigned bytes.
	 */
	public static IntStream<RuntimeException> ustream(byte[] array) {
		return ustream(array, 0);
	}

	/**
	 * Stream array as unsigned bytes.
	 */
	public static IntStream<RuntimeException> ustream(byte[] array, int offset) {
		return ustream(array, offset, array.length - offset);
	}

	/**
	 * Stream array as unsigned bytes.
	 */
	public static IntStream<RuntimeException> ustream(byte[] array, int offset, int len) {
		return Streams.slice(offset, len).map(i -> Maths.ubyte(array[i]));
	}

	/**
	 * Capture the integer collection as bytes.
	 */
	public static byte[] bytes(Collection<Integer> values) {
		return bytes(Streams.from(values));
	}

	/**
	 * Capture the integer stream as bytes.
	 * @throws E
	 */
	public static <E extends Exception> byte[] bytes(Stream<E, Integer> stream) throws E {
		return bytes(stream.mapToInt(Integer::intValue));
	}

	/**
	 * Capture the int stream as bytes.
	 * @throws E
	 */
	public static <E extends Exception> byte[] bytes(IntStream<E> stream) throws E {
		var out = new ByteArrayOutputStream();
		stream.forEach(out::write);
		return out.toByteArray();
	}

	/**
	 * Creates a byte array of given value.
	 */
	public static byte[] fill(int length, int value) {
		if (length == 0) return Array.BYTE.empty;
		byte[] bytes = new byte[length];
		Arrays.fill(bytes, (byte) value);
		return bytes;
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, int... bytes) {
		writeTo(out, Array.BYTE.of(bytes));
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, byte[] bytes) {
		writeTo(out, bytes, 0);
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, byte[] bytes, int offset) {
		out.write(bytes, offset, bytes.length - offset);
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, ByteProvider b) {
		writeTo(out, b, 0);
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, ByteProvider b, int offset) {
		writeTo(out, b, offset, b.length() - offset);
	}

	/**
	 * Writes bytes to output stream.
	 */
	public static void writeTo(ByteArrayOutputStream out, ByteProvider b, int offset, int length) {
		ExceptionAdapter.runtime.run(() -> b.writeTo(offset, out, length));
	}

	/**
	 * Encodes string to latin-1 bytes.
	 */
	public static byte[] toAsciiBytes(String s) {
		return s.getBytes(StandardCharsets.ISO_8859_1);
	}

	/**
	 * Encodes string to latin-1 bytes.
	 */
	public static ByteProvider toAscii(String s) {
		return ByteProvider.of(toAsciiBytes(s));
	}

	/**
	 * Decodes string from latin-1 bytes.
	 */
	public static String fromAscii(int... data) {
		return fromAscii(Array.BYTE.of(data));
	}

	/**
	 * Decodes string from latin-1 bytes.
	 */
	public static String fromAscii(byte[] data) {
		return fromAscii(data, 0);
	}

	/**
	 * Decodes string from latin-1 bytes.
	 */
	public static String fromAscii(byte[] data, int offset) {
		return fromAscii(data, offset, data.length - offset);
	}

	/**
	 * Decodes string from latin-1 bytes.
	 */
	public static String fromAscii(byte[] data, int offset, int length) {
		return new String(data, offset, length, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(byte[] data, Charset charset) {
		return fromNullTerm(data, 0, charset);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(byte[] data, int offset, Charset charset) {
		return fromNullTerm(data, offset, data.length - offset, charset);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(byte[] data, int offset, int maxLen, Charset charset) {
		return fromNullTerm(ByteArray.Immutable.wrap(data, offset, maxLen), 0, maxLen, charset);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(ByteProvider data, Charset charset) {
		return fromNullTerm(data, 0, charset);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(ByteProvider data, int offset, Charset charset) {
		return fromNullTerm(data, offset, data.length() - offset, charset);
	}

	/**
	 * Extract a null-terminated string, up to maximum length. If no null-termination, the maximum
	 * length string is returned.
	 */
	public static String fromNullTerm(ByteProvider data, int offset, int maxLen, Charset charset) {
		Validate.slice(data.length(), offset, maxLen);
		for (int i = 0; i < maxLen; i++)
			if (data.getByte(offset + i) == 0) return data.getString(offset, i, charset);
		return data.getString(offset, maxLen, charset);
	}

	/**
	 * Extracts a value at bit offset and bit count in current value.
	 */
	public static long getValue(long current, int bits, int bit) {
		return (current & (mask(bits) << bit)) >>> bit;
	}

	/**
	 * Extracts a value at bit offset and bit count in current value.
	 */
	public static int getValueInt(long current, int bits, int bit) {
		return (int) getValue(Maths.uint(current), bits, bit);
	}

	/**
	 * Applies a value at bit offset and bit count in current value, using a mask.
	 */
	public static long setValue(long current, int bits, int bit, long value) {
		long mask = mask(bits);
		return (current & ~(mask << bit)) | ((value & mask) << bit);
	}

	/**
	 * Applies a value at bit offset and bit count in current value, using a mask.
	 */
	public static int setValueInt(int current, int bits, int bit, int value) {
		return (int) setValue(Maths.uint(current), bits, bit, Maths.uint(value));
	}

	/**
	 * Applies a single bit mask inclusively or exclusively. The bit does not wrap.
	 */
	public static long applyBits(long value, boolean on, int... bits) {
		return applyMask(value, maskOfBits(bits), on);
	}

	/**
	 * Applies a single bit mask inclusively or exclusively. The bit does not wrap.
	 */
	public static int applyBitsInt(int value, boolean on, int... bits) {
		return applyMaskInt(value, maskOfBitsInt(bits), on);
	}

	/**
	 * Applies a mask inclusively or exclusively.
	 */
	public static long applyMask(long value, long mask, boolean on) {
		return on ? value | mask : value & (~mask);
	}

	/**
	 * Applies a mask inclusively or exclusively.
	 */
	public static int applyMaskInt(int value, int mask, boolean on) {
		return on ? value | mask : value & (~mask);
	}

	/**
	 * Applies a mask with on/off state.
	 */
	public static long applyMask(long value, long mask, long state) {
		long on = mask & state;
		long off = mask & ~state;
		return (value | on) & (~off);
	}

	/**
	 * Applies a mask inclusively or exclusively.
	 */
	public static int applyMaskInt(int value, int mask, int state) {
		return (int) applyMask(value, mask, state);
	}

	/**
	 * Creates a 32-bit mask with given number of bits.
	 */
	public static int maskInt(int bitCount) {
		if (bitCount == 0) return 0;
		if (bitCount >= Integer.SIZE) return -1;
		return (1 << bitCount) - 1;
	}

	/**
	 * Creates a 32-bit mask with given number of bits from start bit. Bits do not wrap.
	 */
	public static int maskInt(int startBit, int bitCount) {
		if (startBit >= Integer.SIZE) return 0;
		return maskInt(bitCount) << startBit;
	}

	/**
	 * Creates a 64-bit mask with given number of bits.
	 */
	public static long mask(int bitCount) {
		if (bitCount >= Long.SIZE) return -1L;
		return (1L << bitCount) - 1L;
	}

	/**
	 * Creates a mask with given number of bits from start bit. Bits do not wrap.
	 */
	public static long mask(int startBit, int bitCount) {
		if (startBit >= Long.SIZE) return 0L;
		return mask(bitCount) << startBit;
	}

	/**
	 * Creates a 64-bit mask from given true bits. Bits do not wrap.
	 */
	public static long maskOfBits(Iterable<Integer> bits) {
		if (bits == null) return 0L;
		long mask = 0L;
		for (int bit : bits)
			if (bit >= 0 && bit < Long.SIZE) mask |= 1L << bit;
		return mask;
	}

	/**
	 * Creates a 64-bit mask from given true bits. Bits do not wrap.
	 */
	public static long maskOfBits(int... bits) {
		if (bits == null) return 0L;
		long mask = 0L;
		for (int bit : bits)
			if (bit >= 0 && bit < Long.SIZE) mask |= 1L << bit;
		return mask;
	}

	/**
	 * Creates a value with given bit on or off. Used to construct a mask from bits. The bit does
	 * not wrap.
	 */
	public static long maskOfBit(boolean flag, int bit) {
		if (!flag || bit < 0 || bit >= Long.SIZE) return 0L;
		return 1L << bit;
	}

	/**
	 * Creates a 32-bit mask from given true bits.
	 */
	public static int maskOfBitsInt(Iterable<Integer> bits) {
		return (int) maskOfBits(bits);
	}

	/**
	 * Creates a 32-bit mask from given true bits.
	 */
	public static int maskOfBitsInt(int... bits) {
		return (int) maskOfBits(bits);
	}

	/**
	 * Creates a value with given bit on or off. Used to construct a mask from bits.
	 */
	public static int maskOfBitInt(boolean flag, int bit) {
		return (int) maskOfBit(flag, bit);
	}

	/**
	 * Determines if all the bits are in the mask.
	 */
	public static boolean masked(long mask, int... bits) {
		long bitMask = maskOfBits(bits);
		return (mask & bitMask) == bitMask;
	}

	/**
	 * Returns an array of the bits that are set.
	 */
	public static int[] bits(int value) {
		return bits(Maths.uint(value));
	}

	/**
	 * Returns an array of the bits that are set.
	 */
	public static int[] bits(long value) {
		if (value == 0L) return Array.INT.empty;
		int[] bits = new int[Long.bitCount(value)];
		for (int i = Long.numberOfTrailingZeros(value), j = 0; j < bits.length; i++)
			if (bit(value, i)) bits[j++] = i;
		return bits;
	}

	/**
	 * Returns true if the given bit is set in the value.
	 */
	public static boolean bit(long value, int bit) {
		return (value & maskOfBit(true, bit)) != 0;
	}

	/**
	 * Converts value to bytes, msb first.
	 */
	public static byte[] toMsb(short value) {
		return toMsb(value, Short.BYTES);
	}

	/**
	 * Converts value to bytes, msb first.
	 */
	public static byte[] toMsb(int value) {
		return toMsb(value, Integer.BYTES);
	}

	/**
	 * Converts value to bytes, msb first.
	 */
	public static byte[] toMsb(long value) {
		return toMsb(value, Long.BYTES);
	}

	/**
	 * Converts value to bytes, msb first.
	 */
	public static byte[] toMsb(long value, int length) {
		byte[] data = new byte[length];
		writeMsb(value, data, 0, length);
		return data;
	}

	/**
	 * Converts value to bytes, lsb first.
	 */
	public static byte[] toLsb(short value) {
		return toLsb(value, Short.BYTES);
	}

	/**
	 * Converts value to bytes, lsb first.
	 */
	public static byte[] toLsb(int value) {
		return toLsb(value, Integer.BYTES);
	}

	/**
	 * Converts value to bytes, lsb first.
	 */
	public static byte[] toLsb(long value) {
		return toLsb(value, Long.BYTES);
	}

	/**
	 * Converts value to bytes, lsb first.
	 */
	public static byte[] toLsb(long value, int length) {
		byte[] data = new byte[length];
		writeLsb(value, data, 0, length);
		return data;
	}

	/**
	 * Writes converted value to byte array, msb first.
	 */
	public static int writeMsb(long value, byte[] data) {
		return writeMsb(value, data, 0);
	}

	/**
	 * Writes converted value to byte array, msb first.
	 */
	public static int writeMsb(long value, byte[] data, int offset) {
		return writeMsb(value, data, offset, data.length - offset);
	}

	/**
	 * Writes converted value to byte array, msb first.
	 */
	public static int writeMsb(long value, byte[] data, int offset, int length) {
		Validate.slice(data.length, offset, length);
		Validate.max(length, Long.BYTES);
		while (--length >= 0)
			data[offset++] = byteAt(value, length);
		return offset;
	}

	/**
	 * Writes converted value to byte array, lsb first.
	 */
	public static int writeLsb(long value, byte[] data) {
		return writeLsb(value, data, 0);
	}

	/**
	 * Writes converted value to byte array, lsb first.
	 */
	public static int writeLsb(long value, byte[] data, int offset) {
		return writeLsb(value, data, offset, data.length - offset);
	}

	/**
	 * Writes converted value to byte array, lsb first.
	 */
	public static int writeLsb(long value, byte[] data, int offset, int length) {
		Validate.slice(data.length, offset, length);
		Validate.max(length, Long.BYTES);
		for (int i = 0; i < length; i++)
			data[offset++] = byteAt(value, i);
		return offset;
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromMsb(int... array) {
		return fromMsb(Array.BYTE.of(array));
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromMsb(byte[] array) {
		return fromMsb(array, 0);
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromMsb(byte[] array, int offset) {
		return fromMsb(array, offset, array.length - offset);
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromMsb(byte[] array, int offset, int length) {
		Validate.slice(array.length, offset, length);
		Validate.max(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array[offset + i], length - i - 1);
		return value;
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromLsb(int... array) {
		return fromLsb(Array.BYTE.of(array));
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromLsb(byte[] array) {
		return fromLsb(array, 0);
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromLsb(byte[] array, int offset) {
		return fromLsb(array, offset, array.length - offset);
	}

	/**
	 * Creates a byte-ordered value from byte array.
	 */
	public static long fromLsb(byte[] array, int offset, int length) {
		Validate.slice(array.length, offset, length);
		Validate.max(length, Long.BYTES);
		long value = 0;
		for (int i = 0; i < length; i++)
			value |= shiftByteLeft(array[offset + i], i);
		return value;
	}

	/**
	 * Returns an unsigned byte from the given byte offset within a 64-bit value.
	 */
	public static short ubyteAt(long value, int byteOffset) {
		return Maths.ubyte(byteAt(value, byteOffset));
	}

	/**
	 * Returns an byte from the given byte offset within a 64-bit value.
	 */
	public static byte byteAt(long value, int byteOffset) {
		if (byteOffset == 0) return (byte) value;
		return (byte) shift(value, byteOffset);
	}

	/**
	 * Shifts first 8-bits left by given number of bytes.
	 */
	public static long shiftByteLeft(long value, int bytes) {
		return shift(value & BYTE_MASK, -bytes);
	}

	/**
	 * Shifts 16-bit value by given number of bytes.
	 */
	public static short shift(short value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	/**
	 * Shifts 32-bit value by given number of bytes.
	 */
	public static int shift(int value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	/**
	 * Shifts 64-bit value by given number of bytes.
	 */
	public static long shift(long value, int bytes) {
		return shiftBits(value, Byte.SIZE * bytes);
	}

	/**
	 * Shifts 8-bit value by given number of bits.
	 */
	public static byte shiftBits(byte value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Byte.SIZE) return 0;
		return (byte) (bits > 0 ? (value & BYTE_MASK) >>> bits : value << -bits);
	}

	/**
	 * Shifts 16-bit value by given number of bits.
	 */
	public static short shiftBits(short value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Short.SIZE) return 0;
		return (short) (bits > 0 ? (value & SHORT_MASK) >>> bits : value << -bits);
	}

	/**
	 * Shifts 32-bit value by given number of bits.
	 */
	public static int shiftBits(int value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Integer.SIZE) return 0;
		return bits > 0 ? value >>> bits : value << -bits;
	}

	/**
	 * Shifts 64-bit value by given number of bits.
	 */
	public static long shiftBits(long value, int bits) {
		if (bits == 0) return value;
		if (value == 0 || Math.abs(bits) >= Long.SIZE) return 0;
		return bits > 0 ? value >>> bits : value << -bits;
	}

	/**
	 * Inverts bits of 8-bit value.
	 */
	public static byte invertByte(int value) {
		return (byte) ~value;
	}

	/**
	 * Inverts bits of 16-bit value.
	 */
	public static short invertShort(int value) {
		return (short) ~value;
	}

	/**
	 * Reverses bits of 8-bit value.
	 */
	public static byte reverseByte(int value) {
		return (byte) reverseAsInt(value, Byte.SIZE);
	}

	/**
	 * Reverses bits of 16-bit value.
	 */
	public static short reverseShort(int value) {
		return (short) reverseAsInt(value, Short.SIZE);
	}

	/**
	 * Reverses value bits, removing bits outside the range.
	 */
	public static int reverseAsInt(int value, int bits) {
		return Integer.reverse(value) >>> (Integer.SIZE - bits);
	}

	/**
	 * Reverses value bits, removing bits outside the range.
	 */
	public static long reverse(long value, int bits) {
		return Long.reverse(value) >>> (Long.SIZE - bits);
	}

	/**
	 * Iterate over the set bits of a mask. A true highBit iterates down.
	 */
	private static <E extends Exception> void acceptMask(boolean highBit, long mask,
		Excepts.IntConsumer<E> consumer, int min, int max) throws E {
		if (highBit) {
			for (int i = max; i >= min; i--)
				if (bit(mask, i)) consumer.accept(i);
		} else {
			for (int i = min; i <= max; i++)
				if (bit(mask, i)) consumer.accept(i);
		}
	}

	/**
	 * Iterate over the set bits of a mask, processing a value. A true highBit iterates down.
	 */
	private static <E extends Exception> long applyMask(boolean highBit, long mask, long value,
		BitReducerLong<E> function) throws E {
		int min = Long.numberOfTrailingZeros(mask);
		int max = Long.SIZE - Long.numberOfLeadingZeros(mask) - 1;
		if (highBit) {
			for (int i = max; i >= min; i--)
				if (bit(mask, i)) value = function.applyAsLong(i, value);
		} else {
			for (int i = min; i <= max; i++)
				if (bit(mask, i)) value = function.applyAsLong(i, value);
		}
		return value;
	}

	/**
	 * Iterate over the set bits of a mask, processing a value. A true highBit iterates down.
	 */
	private static <E extends Exception> int applyMask(boolean highBit, int mask, int value,
		BitReducerInt<E> function) throws E {
		int min = Integer.numberOfTrailingZeros(mask);
		int max = Integer.SIZE - Long.numberOfLeadingZeros(mask) - 1;
		if (highBit) {
			for (int i = max; i >= min; i--)
				if (bit(mask, i)) value = function.applyAsInt(i, value);
		} else {
			for (int i = min; i <= max; i++)
				if (bit(mask, i)) value = function.applyAsInt(i, value);
		}
		return value;
	}
}
