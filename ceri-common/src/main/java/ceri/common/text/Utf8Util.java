package ceri.common.text;

import static java.nio.charset.CodingErrorAction.REPLACE;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.PrimitiveIterator.OfInt;
import ceri.common.collection.ArrayUtil;

/**
 * Utility for fine control over UTF8 manipulation.
 */
public class Utf8Util {
	public static final int MAX_BYTES_PER_CODE_POINT = 4;
	private static final String UTF8 = "UTF8";
	private static final int ONE_BYTE_START_MASK = 0x80;
	private static final int ONE_BYTE_START_VALUE = 0x00;
	private static final int ONE_BYTE_MAX_CODE_POINT = 0x7f;
	private static final int TWO_BYTE_START_MASK = 0xe0;
	private static final int TWO_BYTE_START_MASK_INVERSE = 0xff ^ TWO_BYTE_START_MASK;
	private static final int TWO_BYTE_START_VALUE = 0xc0;
	private static final int TWO_BYTE_MAX_CODE_POINT = 0x7ff;
	private static final int THREE_BYTE_START_MASK = 0xf0;
	private static final int THREE_BYTE_START_MASK_INVERSE = 0xff ^ THREE_BYTE_START_MASK;
	private static final int THREE_BYTE_START_VALUE = 0xe0;
	private static final int THREE_BYTE_MAX_CODE_POINT = 0xffff;
	private static final int FOUR_BYTE_START_MASK = 0xf8;
	private static final int FOUR_BYTE_START_MASK_INVERSE = 0xff ^ FOUR_BYTE_START_MASK;
	private static final int FOUR_BYTE_START_VALUE = 0xf0;
	private static final int FOUR_BYTE_MAX_CODE_POINT = 0x10ffff;
	private static final int SUBSEQUENT_BYTE_MASK = 0xc0;
	private static final int SUBSEQUENT_BYTE_MASK_INVERSE = 0xff ^ SUBSEQUENT_BYTE_MASK;
	private static final int SUBSEQUENT_BYTE_VALUE = 0x80;

	private Utf8Util() {}

	/**
	 * Returns the string as UTF8 bytes.
	 */
	public static byte[] encode(String s) {
		try {
			return s.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Should not happen");
		}
	}

	/**
	 * Returns the UTF8 charset.
	 */
	public static Charset charset() {
		return Charset.forName(UTF8);
	}

	/**
	 * Returns a configured UTF8 encoder.
	 */
	public static CharsetEncoder encoder() {
		return charset().newEncoder().onMalformedInput(REPLACE).onUnmappableCharacter(REPLACE);
	}

	/**
	 * Finds a UTF8 starting byte at the lowest index greater than or equal to the given offset.
	 * Returns -1 if not found before the end of the array.
	 */
	public static int ceilingStart(byte[] array, int i) {
		if (i < 0 || i >= array.length) throw new ArrayIndexOutOfBoundsException(i);
		for (; i < array.length; i++)
			if (isStart(array[i])) return i;
		return -1;
	}

	/**
	 * Finds a UTF8 starting byte at the lowest index greater than the given offset. Returns -1 if
	 * not found before the end of the array.
	 */
	public static int higherStart(byte[] array, int i) {
		if (i < 0 || i >= array.length) throw new ArrayIndexOutOfBoundsException(i);
		if (i == array.length - 1) return -1;
		return ceilingStart(array, i + 1);
	}

	/**
	 * Finds a UTF8 starting byte at the greatest index less than or equal to the given offset.
	 * Returns -1 if not found before the start of the array.
	 */
	public static int floorStart(byte[] array, int i) {
		if (i < 0 || i >= array.length) throw new ArrayIndexOutOfBoundsException(i);
		for (; i >= 0; i--)
			if (isStart(array[i])) return i;
		return -1;
	}

	/**
	 * Finds a UTF8 starting byte at the greatest index lower than the given offset. Returns -1 if
	 * not found before the start of the array.
	 */
	public static int lowerStart(byte[] array, int i) {
		if (i < 0 || i >= array.length) throw new ArrayIndexOutOfBoundsException(i);
		if (i == 0) return -1;
		return floorStart(array, i - 1);
	}

	/**
	 * Returns true if the byte is the start of a UTF8 code point.
	 */
	public static boolean isStart(byte b) {
		return is1Byte(b) || is2ByteStart(b) || is3ByteStart(b) || is4ByteStart(b);
	}

	/**
	 * Returns true if the byte is the start of a 1-byte UTF8 code point.
	 */
	public static boolean is1Byte(byte b) {
		return byteMatch(ONE_BYTE_START_MASK, ONE_BYTE_START_VALUE, b);
	}

	/**
	 * Returns true if the byte is the start of a 2-byte UTF8 code point.
	 */
	public static boolean is2ByteStart(byte b) {
		return byteMatch(TWO_BYTE_START_MASK, TWO_BYTE_START_VALUE, b);
	}

	/**
	 * Returns true if the byte is the start of a 3-byte UTF8 code point.
	 */
	public static boolean is3ByteStart(byte b) {
		return byteMatch(THREE_BYTE_START_MASK, THREE_BYTE_START_VALUE, b);
	}

	/**
	 * Returns true if the byte is the start of a 4-byte UTF8 code point.
	 */
	public static boolean is4ByteStart(byte b) {
		return byteMatch(FOUR_BYTE_START_MASK, FOUR_BYTE_START_VALUE, b);
	}

	/**
	 * Returns true if the byte is a subsequent byte of a UTF8 code point.
	 */
	public static boolean isSubsequent(byte b) {
		return byteMatch(SUBSEQUENT_BYTE_MASK, SUBSEQUENT_BYTE_VALUE, b);
	}

	/**
	 * Returns the total number of UTF8 bytes representing this character sequence. Does not count
	 * invalid code points.
	 */
	public static int byteCount(CharSequence text) {
		return text.codePoints().map(Utf8Util::byteCount).sum();
	}

	/**
	 * Returns the number of UTF8 bytes representing this code point. Returns 0 if the code point is
	 * invalid.
	 */
	public static int byteCount(int codePoint) {
		if (codePoint < 0) return 0;
		if (codePoint <= ONE_BYTE_MAX_CODE_POINT) return 1;
		if (codePoint <= TWO_BYTE_MAX_CODE_POINT) return 2;
		if (codePoint <= THREE_BYTE_MAX_CODE_POINT) return 3;
		if (codePoint <= FOUR_BYTE_MAX_CODE_POINT) return 4;
		return 0;
	}

	/**
	 * Encodes text into bytes, writing to the given array. Writes nothing if a code point is
	 * invalid.Throws ArrayIndexOutOfBoundsException if the array writing goes past the end of the
	 * array. Returns the index in the array after writing is complete.
	 */
	public static int encodeTo(CharSequence text, byte[] array, int offset) {
		OfInt i = text.codePoints().iterator();
		while (i.hasNext())
			offset = encodeTo(i.nextInt(), array, offset);
		return offset;
	}

	/**
	 * Encodes a code point into bytes, writing to the given array. Writes nothing if the code point
	 * is invalid. Throws ArrayIndexOutOfBoundsException if the array writing goes past the end of
	 * the array. Returns the index in the array after writing is complete.
	 */
	public static int encodeTo(int codePoint, byte[] array, int offset) {
		if (codePoint < 0) return offset;
		if (codePoint <= ONE_BYTE_MAX_CODE_POINT) return writeOneByte(codePoint, array, offset);
		if (codePoint <= TWO_BYTE_MAX_CODE_POINT) return writeTwoByte(codePoint, array, offset);
		if (codePoint <= THREE_BYTE_MAX_CODE_POINT) return writeThreeByte(codePoint, array, offset);
		if (codePoint <= FOUR_BYTE_MAX_CODE_POINT) return writeFourByte(codePoint, array, offset);
		return offset;
	}

	/**
	 * Encodes a code point into bytes. Returns an empty array if the code point is invalid.
	 */
	public static byte[] encode(int codePoint) {
		if (codePoint < 0) return ArrayUtil.EMPTY_BYTE;
		if (codePoint <= ONE_BYTE_MAX_CODE_POINT) return new byte[] { (byte) codePoint };
		if (codePoint <= TWO_BYTE_MAX_CODE_POINT) return twoByte(codePoint);
		if (codePoint <= THREE_BYTE_MAX_CODE_POINT) return threeByte(codePoint);
		if (codePoint <= FOUR_BYTE_MAX_CODE_POINT) return fourByte(codePoint);
		return ArrayUtil.EMPTY_BYTE;
	}

	private static int writeOneByte(int codePoint, byte[] array, int offset) {
		array[offset++] = (byte) codePoint;
		return offset;
	}

	private static int writeTwoByte(int codePoint, byte[] array, int offset) {
		array[offset++] = twoByteStart(codePoint >> 6);
		array[offset++] = subsequent(codePoint);
		return offset;
	}

	private static byte[] twoByte(int codePoint) {
		return new byte[] { twoByteStart(codePoint >> 6), subsequent(codePoint) };
	}

	private static int writeThreeByte(int codePoint, byte[] array, int offset) {
		array[offset++] = threeByteStart(codePoint >> 12);
		array[offset++] = subsequent(codePoint >> 6);
		array[offset++] = subsequent(codePoint);
		return offset;
	}

	private static byte[] threeByte(int codePoint) {
		return new byte[] { threeByteStart(codePoint >> 12), subsequent(codePoint >> 6),
			subsequent(codePoint) };
	}

	private static int writeFourByte(int codePoint, byte[] array, int offset) {
		array[offset++] = fourByteStart(codePoint >> 18);
		array[offset++] = subsequent(codePoint >> 12);
		array[offset++] = subsequent(codePoint >> 6);
		array[offset++] = subsequent(codePoint);
		return offset;
	}

	private static byte[] fourByte(int codePoint) {
		return new byte[] { fourByteStart(codePoint >> 18), subsequent(codePoint >> 12),
			subsequent(codePoint >> 6), subsequent(codePoint) };
	}

	private static byte twoByteStart(int value) {
		return byteMask(TWO_BYTE_START_VALUE, TWO_BYTE_START_MASK_INVERSE, value);
	}

	private static byte threeByteStart(int value) {
		return byteMask(THREE_BYTE_START_VALUE, THREE_BYTE_START_MASK_INVERSE, value);
	}

	private static byte fourByteStart(int value) {
		return byteMask(FOUR_BYTE_START_VALUE, FOUR_BYTE_START_MASK_INVERSE, value);
	}

	private static byte subsequent(int value) {
		return byteMask(SUBSEQUENT_BYTE_VALUE, SUBSEQUENT_BYTE_MASK_INVERSE, value);
	}

	private static byte byteMask(int or, int mask, int value) {
		return (byte) (or | (mask & value));
	}

	private static boolean byteMatch(int mask, int maskedValue, int value) {
		return (mask & value) == maskedValue;
	}

}
