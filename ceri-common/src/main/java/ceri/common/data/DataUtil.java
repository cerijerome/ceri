package ceri.common.data;

import static ceri.common.text.StringUtil.DECIMAL_RADIX;
import static ceri.common.text.StringUtil.escape;
import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.collection.ArrayUtil;
import ceri.common.exception.Exceptions;
import ceri.common.validation.ValidationUtil;

public class DataUtil {

	private DataUtil() {}

	/**
	 * Validate minimum data size.
	 */
	public static void requireMin(ByteProvider.Reader<?> r, int size) {
		validateMin(r.remaining(), size, "Remaining bytes");
	}

	/**
	 * Expect next ascii char values. Reading stops if a byte does not match, and an exception is
	 * thrown.
	 */
	public static void expectAscii(ByteReader r, char... expected) {
		for (int i = 0; i < expected.length; i++) {
			char c = (char) r.readUbyte();
			if (c == expected[i]) continue;
			throw Exceptions.illegalArg("Expected '%s': '%s'", escape(expected[i]), escape(c));
		}
	}

	/**
	 * Expect next ascii char values. Reading stops if a byte does not match, and an exception is
	 * thrown.
	 */
	public static void expectAscii(ByteReader r, String expected) {
		for (int i = 0; i < expected.length(); i++) {
			char c = (char) r.readUbyte();
			if (c == expected.charAt(i)) continue;
			throw Exceptions.illegalArg("Expected '%s': '%s'", escape(expected.charAt(i)), escape(c));
		}
	}

	/**
	 * Expect next ascii char values. If a byte does not match, an exception will be thrown after
	 * reading the full length of bytes.
	 */
	public static void expectAsciiAll(ByteReader r, char... expected) {
		for (int i = 0; i < expected.length; i++) {
			char c = (char) r.readUbyte();
			if (c == expected[i]) continue;
			if (i < expected.length - 1) r.skip(expected.length - i - 1);
			throw Exceptions.illegalArg("Expected '%s': '%s'", escape(expected[i]), escape(c));
		}
	}

	/**
	 * Expect next ascii char values. If a byte does not match, an exception will be thrown after
	 * reading the full length of bytes.
	 */
	public static void expectAsciiAll(ByteReader r, String expected) {
		for (int i = 0; i < expected.length(); i++) {
			char c = (char) r.readUbyte();
			if (c == expected.charAt(i)) continue;
			if (i < expected.length() - 1) r.skip(expected.length() - i - 1);
			throw Exceptions.illegalArg("Expected '%s': '%s'", escape(expected.charAt(i)), escape(c));
		}
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 */
	public static void expect(ByteReader r, int... bytes) {
		expect(r, ArrayUtil.bytes(bytes));
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, byte[] bytes) {
		expect(r, bytes, 0);
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, byte[] bytes, int offset) {
		expect(r, bytes, offset, bytes.length - offset);
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, byte[] bytes, int offset, int length) {
		ValidationUtil.validateSlice(bytes.length, offset, length);
		ValidationUtil.validateSlice(bytes.length, offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes[offset]) continue;
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)", bytes[offset], b);
		}
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the byte provider is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, ByteProvider bytes) {
		expect(r, bytes, 0);
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the byte provider is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, ByteProvider bytes, int offset) {
		expect(r, bytes, offset, bytes.length() - offset);
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 * If the byte provider is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expect(ByteReader r, ByteProvider bytes, int offset, int length) {
		ValidationUtil.validateSlice(bytes.length(), offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes.getByte(offset)) continue;
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)", bytes.getByte(offset), b);
		}
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes.
	 */
	public static void expectAll(ByteReader r, int... bytes) {
		expectAll(r, ArrayUtil.bytes(bytes));
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expectAll(ByteReader r, byte[] bytes) {
		expectAll(r, bytes, 0);
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expectAll(ByteReader r, byte[] bytes, int offset) {
		expectAll(r, bytes, offset, bytes.length - offset);
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the array is invalid, reading does not start, and an exception is thrown.
	 */
	public static void expectAll(ByteReader r, byte[] bytes, int offset, int length) {
		ValidationUtil.validateSlice(bytes.length, offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes[offset]) continue;
			if (i < length - 1) r.skip(length - i - 1);
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)", bytes[offset], b);
		}
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the byte provider is invalid, reading does not start, and an exception is
	 * thrown.
	 */
	public static void expectAll(ByteReader r, ByteProvider bytes) {
		expectAll(r, bytes, 0);
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the byte provider is invalid, reading does not start, and an exception is
	 * thrown.
	 */
	public static void expectAll(ByteReader r, ByteProvider bytes, int offset) {
		expectAll(r, bytes, offset, bytes.length() - offset);
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes. If the byte provider is invalid, reading does not start, and an exception is
	 * thrown.
	 */
	public static void expectAll(ByteReader r, ByteProvider bytes, int offset, int length) {
		ValidationUtil.validateSlice(bytes.length(), offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes.getByte(offset)) continue;
			if (i < length - 1) r.skip(length - i - 1);
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)", bytes.getByte(offset), b);
		}
	}

	/**
	 * Integer value from ascii digits.
	 */
	public static int digits(ByteReader r, int length) {
		int sum = 0;
		for (int i = 0; i < length; i++)
			sum = sum * DECIMAL_RADIX + digit(r);
		return sum;
	}

	/**
	 * Integer value from ascii digit.
	 */
	public static int digit(ByteReader r) {
		return digit(r.readUbyte());
	}

	/**
	 * Integer value from ascii digit.
	 */
	private static int digit(int b) {
		if (b >= '0' && b <= '9') return b - '0';
		throw Exceptions.illegalArg("Not a digit: '%s'" + escape((char) b));
	}

}
