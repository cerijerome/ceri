package ceri.common.data;

import ceri.common.array.Array;
import ceri.common.except.Exceptions;
import ceri.common.text.Chars;
import ceri.common.util.Validate;

public class Data {

	private Data() {}

	/**
	 * Validate minimum data size.
	 */
	public static void requireMin(ByteProvider.Reader<?> r, int size) {
		Validate.min(r.remaining(), size, "Remaining bytes");
	}

	/**
	 * Expect next ascii char values. Reading stops if a byte does not match, and an exception is
	 * thrown.
	 */
	public static void expectAscii(ByteReader r, char... expected) {
		for (int i = 0; i < expected.length; i++) {
			char c = (char) r.readUbyte();
			if (c == expected[i]) continue;
			throw Exceptions.illegalArg("Expected '%s': '%s'", Chars.escape(expected[i]),
				Chars.escape(c));
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
			throw Exceptions.illegalArg("Expected '%s': '%s'", Chars.escape(expected.charAt(i)),
				Chars.escape(c));
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
			throw Exceptions.illegalArg("Expected '%s': '%s'", Chars.escape(expected[i]),
				Chars.escape(c));
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
			throw Exceptions.illegalArg("Expected '%s': '%s'", Chars.escape(expected.charAt(i)),
				Chars.escape(c));
		}
	}

	/**
	 * Expect next byte values. Reading stops if a byte does not match, and an exception is thrown.
	 */
	public static void expect(ByteReader r, int... bytes) {
		expect(r, Array.bytes.of(bytes));
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
		Validate.slice(bytes.length, offset, length);
		Validate.slice(bytes.length, offset, length);
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
		Validate.slice(bytes.length(), offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes.getByte(offset)) continue;
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)",
				bytes.getByte(offset), b);
		}
	}

	/**
	 * Expect next byte values. If a byte does not match an exception will be thrown after reading
	 * length bytes.
	 */
	public static void expectAll(ByteReader r, int... bytes) {
		expectAll(r, Array.bytes.of(bytes));
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
		Validate.slice(bytes.length, offset, length);
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
		Validate.slice(bytes.length(), offset, length);
		for (int i = 0; i < length; i++, offset++) {
			byte b = r.readByte();
			if (b == bytes.getByte(offset)) continue;
			if (i < length - 1) r.skip(length - i - 1);
			throw Exceptions.illegalArg("Expected %1$d (0x%1$x): %2$d (0x%2$x)",
				bytes.getByte(offset), b);
		}
	}

	/**
	 * Integer value from ascii digits.
	 */
	public static int digits(ByteReader r, int length) {
		int sum = 0;
		for (int i = 0; i < length; i++)
			sum = sum * 10 + digit(r);
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
		throw Exceptions.illegalArg("Not a digit: '%s'" + Chars.escape((char) b));
	}
}
