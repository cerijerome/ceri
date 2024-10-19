package ceri.common.text;

import static ceri.common.text.StringUtil.BINARY_RADIX;
import static ceri.common.text.StringUtil.HEX_RADIX;
import static ceri.common.text.StringUtil.OCTAL_RADIX;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.Map;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.math.MathUtil;

/**
 * Provides utilities to parse and decode integer values from strings. Expands the range of decoding
 * and parsing from the integer primitive wrapper classes, and adds missing parse methods for short
 * and byte types.
 */
public class NumberParser {
	private static final Map<String, Integer> RADIX_MAP = ImmutableUtil.asMap("0x", HEX_RADIX, "0X",
		HEX_RADIX, "#", HEX_RADIX, "0b", BINARY_RADIX, "0B", BINARY_RADIX, "0", OCTAL_RADIX);
	// Convenience filters for Parser.String
	public static final ExceptionFunction<RuntimeException, String, Byte> BYTE =
		NumberParser::parseByte;
	public static final ExceptionFunction<RuntimeException, String, Byte> UBYTE =
		NumberParser::parseUbyte;
	public static final ExceptionFunction<RuntimeException, String, Byte> DBYTE =
		NumberParser::decodeByte;
	public static final ExceptionFunction<RuntimeException, String, Byte> DUBYTE =
		NumberParser::decodeUbyte;
	public static final ExceptionFunction<RuntimeException, String, Short> SHORT =
		NumberParser::parseShort;
	public static final ExceptionFunction<RuntimeException, String, Short> USHORT =
		NumberParser::parseUshort;
	public static final ExceptionFunction<RuntimeException, String, Short> DSHORT =
		NumberParser::decodeShort;
	public static final ExceptionFunction<RuntimeException, String, Short> DUSHORT =
		NumberParser::decodeUshort;
	public static final ExceptionFunction<RuntimeException, String, Integer> INT =
		NumberParser::parseInt;
	public static final ExceptionFunction<RuntimeException, String, Integer> UINT =
		NumberParser::parseUint;
	public static final ExceptionFunction<RuntimeException, String, Integer> DINT =
		NumberParser::decodeInt;
	public static final ExceptionFunction<RuntimeException, String, Integer> DUINT =
		NumberParser::decodeUint;
	public static final ExceptionFunction<RuntimeException, String, Long> LONG =
		NumberParser::parseLong;	
	public static final ExceptionFunction<RuntimeException, String, Long> ULONG =
		NumberParser::parseUlong;	
	public static final ExceptionFunction<RuntimeException, String, Long> DLONG =
		NumberParser::decodeLong;
	public static final ExceptionFunction<RuntimeException, String, Long> DULONG =
		NumberParser::decodeUlong;
	private boolean positive = true;
	private int radix = 10;
	private int i = 0;
	private final String s;

	/**
	 * Parses a string into a byte using radix. Same as {@link Byte#parseByte(String, int)}, but
	 * allows -0xff to 0xff range.
	 */
	public static byte parseByte(String s, int radix) {
		return new NumberParser(s).signed().radix(radix).noSign().parseByte();
	}

	/**
	 * Parses a string into a short using radix. Same as {@link Short#parseShort(String, int)}, but
	 * allows -0xffff to 0xffff range.
	 */
	public static short parseShort(String s, int radix) {
		return new NumberParser(s).signed().radix(radix).noSign().parseShort();
	}

	/**
	 * Parses a string into an int using radix. Same as {@link Integer#parseInt(String, int)}, but
	 * allows -0xffffffff to 0xffffffff range.
	 */
	public static int parseInt(String s, int radix) {
		return new NumberParser(s).signed().radix(radix).noSign().parseInt();
	}

	/**
	 * Parses a string into a long using radix. Same as {@link Long#parseLong(String, int)}, but
	 * allows -0xffffffffffffffff to 0xffffffffffffffff range.
	 */
	public static long parseLong(String s, int radix) {
		return new NumberParser(s).signed().radix(radix).noSign().parseLong();
	}

	/**
	 * Parses a string into a byte using decimal radix. Same as {@link Byte#parseByte(String)}, but
	 * allows -0xff to 0xff range.
	 */
	public static byte parseByte(String s) {
		return parseByte(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into a short using decimal radix. Same as {@link Short#parseShort(String)},
	 * but allows -0xffff to 0xffff range.
	 */
	public static short parseShort(String s) {
		return parseShort(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into an int using decimal radix. Same as {@link Integer#parseInt(String)},
	 * but allows -0xffffffff to 0xffffffff range.
	 */
	public static int parseInt(String s) {
		return parseInt(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into a long using decimal radix. Same as {@link Long#parseLong(String)}, but
	 * allows -0xffffffffffffffff to 0xffffffffffffffff range.
	 */
	public static long parseLong(String s) {
		return parseLong(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into a byte using radix. Same as {@link Byte#parseByte(String, int)}, but
	 * allows 0 to 0xff range.
	 */
	public static byte parseUbyte(String s, int radix) {
		return new NumberParser(s).unsigned().radix(radix).noSign().parseByte();
	}

	/**
	 * Parses a string into a short using radix. Same as {@link Short#parseShort(String, int)}, but
	 * allows 0 to 0xffff range.
	 */
	public static short parseUshort(String s, int radix) {
		return new NumberParser(s).unsigned().radix(radix).noSign().parseShort();
	}

	/**
	 * Parses a string into an int using radix. Same as
	 * {@link Integer#parseUnsignedInt(String, int)}.
	 */
	public static int parseUint(String s, int radix) {
		return new NumberParser(s).unsigned().radix(radix).noSign().parseInt();
	}

	/**
	 * Parses a string into a long using radix. Same as {@link Long#parseUnsignedLong(String, int)}.
	 */
	public static long parseUlong(String s, int radix) {
		return new NumberParser(s).unsigned().radix(radix).noSign().parseLong();
	}

	/**
	 * Parses a string into a byte using decimal radix. Same as {@link Byte#parseByte(String)}, but
	 * allows 0 to 0xff range.
	 */
	public static byte parseUbyte(String s) {
		return parseUbyte(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into a short using decimal radix. Same as {@link Short#parseShort(String)},
	 * but allows 0 to 0xffff range.
	 */
	public static short parseUshort(String s) {
		return parseUshort(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into an int using decimal radix. Same as
	 * {@link Integer#parseUnsignedInt(String)}.
	 */
	public static int parseUint(String s) {
		return parseUint(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Parses a string into a long using decimal radix. Same as
	 * {@link Long#parseUnsignedLong(String)}.
	 */
	public static long parseUlong(String s) {
		return parseUlong(s, StringUtil.DECIMAL_RADIX);
	}

	/**
	 * Decodes a string into a byte. Same as {@link Byte#decode(String)}, but allows -0xff to 0xff
	 * range.
	 */
	public static byte decodeByte(String s) {
		return new NumberParser(s).signed().radix().noSign().parseByte();
	}

	/**
	 * Decodes a string into a short. Same as {@link Short#decode(String)}, but allows -0xffff to
	 * 0xffff range.
	 */
	public static short decodeShort(String s) {
		return new NumberParser(s).signed().radix().noSign().parseShort();
	}

	/**
	 * Decodes a string into an int. Same as {@link Integer#decode(String)}, but allows -0xffffffff
	 * to 0xffffffff range.
	 */
	public static int decodeInt(String s) {
		return new NumberParser(s).signed().radix().noSign().parseInt();
	}

	/**
	 * Decodes a string into a long. Same as {@link Long#decode(String)}, but allows
	 * -0xffffffffffffffff to 0xffffffffffffffff range.
	 */
	public static long decodeLong(String s) {
		return new NumberParser(s).signed().radix().noSign().parseLong();
	}

	/**
	 * Decodes a string into a byte. Same as {@link Byte#decode(String)}, but allows 0 to 0xff
	 * range.
	 */
	public static byte decodeUbyte(String s) {
		return new NumberParser(s).unsigned().radix().noSign().parseByte();
	}

	/**
	 * Decodes a string into a short. Same as {@link Short#decode(String)}, but allows 0 to 0xffff
	 * range.
	 */
	public static short decodeUshort(String s) {
		return new NumberParser(s).unsigned().radix().noSign().parseShort();
	}

	/**
	 * Decodes a string into an int. Same as {@link Integer#decode(String)}, but allows 0 to
	 * 0xffffffff range.
	 */
	public static int decodeUint(String s) {
		return new NumberParser(s).unsigned().radix().noSign().parseInt();
	}

	/**
	 * Decodes a string into a long. Same as {@link Long#decode(String)}, but allows 0 to
	 * 0xffffffffffffffff range.
	 */
	public static long decodeUlong(String s) {
		return new NumberParser(s).unsigned().radix().noSign().parseLong();
	}

	private NumberParser(String s) {
		this.s = s;
	}

	/**
	 * Parse to check for positive, negative, or no sign at current position.
	 */
	private NumberParser signed() {
		if (i >= s.length()) return this;
		char c = s.charAt(i);
		if (c == '+' || c == '-') i++;
		if (c == '-') positive = false;
		return this;
	}

	/**
	 * Parse to make sure positive sign or no sign at current position.
	 */
	private NumberParser unsigned() {
		if (i >= s.length()) return this;
		char c = s.charAt(i);
		if (c == '-') throw formatException("Number cannot be negative: \"%s\"", s);
		if (c == '+') i++;
		return this;
	}

	/**
	 * Parse radix at current position.
	 */
	private NumberParser radix() {
		if (i >= s.length()) return this;
		for (var entry : RADIX_MAP.entrySet()) {
			if (!s.startsWith(entry.getKey(), i)) continue;
			if (i + entry.getKey().length() >= s.length()) continue;
			i += entry.getKey().length();
			radix = entry.getValue();
			break;
		}
		return this;
	}

	/**
	 * Set given radix.
	 */
	private NumberParser radix(int radix) {
		this.radix = radix;
		validateRange(radix, Character.MIN_RADIX, Character.MAX_RADIX, "Radix");
		return this;
	}

	/**
	 * Verify that +/- sign is not at the current position, before calling paseXxx method.
	 */
	private NumberParser noSign() {
		if (i >= s.length()) return this;
		char c = s.charAt(i);
		if (c == '+' || c == '-')
			throw formatException("Unexpected sign at index %d: \"%s\"", i, s);
		return this;
	}

	/**
	 * Parses remaining string.
	 */
	private byte parseByte() {
		int ivalue = Integer.parseUnsignedInt(s, i, s.length(), radix);
		if (ivalue < 0 || ivalue > MathUtil.MAX_UBYTE)
			throw formatException("Number out of byte range -0xff to 0xff: \"%s\"", s);
		byte value = (byte) ivalue;
		return (positive || value == Byte.MIN_VALUE) ? value : (byte) -value;
	}

	/**
	 * Parses remaining string.
	 */
	private short parseShort() {
		int ivalue = Integer.parseUnsignedInt(s, i, s.length(), radix);
		if (ivalue < 0 || ivalue > MathUtil.MAX_USHORT)
			throw formatException("Number out of short range -0xffff to 0xffff: \"%s\"", s);
		short value = (short) ivalue;
		return (positive || value == Short.MIN_VALUE) ? value : (short) -value;
	}

	/**
	 * Parses remaining string.
	 */
	private int parseInt() {
		int value = Integer.parseUnsignedInt(s, i, s.length(), radix);
		return (positive || value == Integer.MIN_VALUE) ? value : -value;
	}

	/**
	 * Parses remaining string.
	 */
	private long parseLong() {
		long value = Long.parseUnsignedLong(s, i, s.length(), radix);
		return (positive || value == Long.MIN_VALUE) ? value : -value;
	}

	private static NumberFormatException formatException(String format, Object... args) {
		return new NumberFormatException(String.format(format, args));
	}

}
