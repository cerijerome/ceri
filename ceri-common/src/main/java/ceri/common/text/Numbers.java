package ceri.common.text;

import java.util.Set;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.math.Radix;
import ceri.common.util.Basics;

/**
 * Utility methods for parsing types, with defaults if invalid.
 */
public class Numbers {
	private static final Set<String> TRUE = Set.of("true", "yes", "1");
	private static final Set<String> FALSE = Set.of("false", "no", "0");

	private Numbers() {}

	private interface RadixParser<T> {
		T parse(int radix, CharSequence s);
	}
	
	/**
	 * Supports decoding numbers, where the radix is determined.
	 */
	public static class Decode {
		public static final Functions.Function<CharSequence, Byte> BYTE = s -> toByte(s, null);
		public static final Functions.Function<CharSequence, Byte> UBYTE = s -> toUbyte(s, null);
		public static final Functions.Function<CharSequence, Short> SHORT = s -> toShort(s, null);
		public static final Functions.Function<CharSequence, Short> USHORT = s -> toUshort(s, null);
		public static final Functions.Function<CharSequence, Integer> INT = s -> toInt(s, null);
		public static final Functions.Function<CharSequence, Integer> UINT = s -> toUint(s, null);
		public static final Functions.Function<CharSequence, Long> LONG = s -> toLong(s, null);
		public static final Functions.Function<CharSequence, Long> ULONG = s -> toUlong(s, null);

		private Decode() {}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toByte(CharSequence s) {
			return new Parser(s).signed().radix().noSign().parseByte();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toByte(CharSequence s, Byte def) {
			return parse(s, Decode::toByte, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toUbyte(CharSequence s) {
			return new Parser(s).unsigned().radix().noSign().parseByte();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toUbyte(CharSequence s, Byte def) {
			return parse(s, Decode::toUbyte, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toShort(CharSequence s) {
			return new Parser(s).signed().radix().noSign().parseShort();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toShort(CharSequence s, Short def) {
			return parse(s, Decode::toShort, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toUshort(CharSequence s) {
			return new Parser(s).unsigned().radix().noSign().parseShort();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toUshort(CharSequence s, Short def) {
			return parse(s, Decode::toUshort, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toInt(CharSequence s) {
			return new Parser(s).signed().radix().noSign().parseInt();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toInt(CharSequence s, Integer def) {
			return parse(s, Decode::toInt, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toUint(CharSequence s) {
			return new Parser(s).unsigned().radix().noSign().parseInt();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toUint(CharSequence s, Integer def) {
			return parse(s, Decode::toUint, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toLong(CharSequence s) {
			return new Parser(s).signed().radix().noSign().parseLong();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toLong(CharSequence s, Long def) {
			return parse(s, Decode::toLong, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toUlong(CharSequence s) {
			return new Parser(s).unsigned().radix().noSign().parseLong();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toUlong(CharSequence s, Long def) {
			return parse(s, Decode::toUlong, def);
		}
	}

	/**
	 * Supports parsing numbers, where the radix is known or not needed.
	 */
	public static class Parse {
		public static final Functions.Function<CharSequence, Boolean> BOOL = s -> toBool(s, null);
		public static final Functions.Function<CharSequence, Byte> BYTE = s -> toByte(s, null);
		public static final Functions.Function<CharSequence, Byte> UBYTE = s -> toUbyte(s, null);
		public static final Functions.Function<CharSequence, Short> SHORT = s -> toShort(s, null);
		public static final Functions.Function<CharSequence, Short> USHORT = s -> toUshort(s, null);
		public static final Functions.Function<CharSequence, Integer> INT = s -> toInt(s, null);
		public static final Functions.Function<CharSequence, Integer> UINT = s -> toUint(s, null);
		public static final Functions.Function<CharSequence, Long> LONG = s -> toLong(s, null);
		public static final Functions.Function<CharSequence, Long> ULONG = s -> toUlong(s, null);
		public static final Functions.Function<CharSequence, Float> FLOAT = s -> toFloat(s, null);
		public static final Functions.Function<CharSequence, Double> DOUBLE =
			s -> toDouble(s, null);

		private Parse() {}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static boolean toBool(CharSequence s) {
			var bool = parseBool(s);
			if (bool != null) return bool.booleanValue();
			throw Numbers.nfe("Invalid bool: ", s);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Boolean toBool(CharSequence s, Boolean def) {
			return Basics.def(parseBool(s), def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toByte(CharSequence s) {
			return toByte(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toByte(int radix, CharSequence s) {
			return new Parser(s).signed().radix(radix).noSign().parseByte();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toByte(CharSequence s, Byte def) {
			return toByte(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toByte(int radix, CharSequence s, Byte def) {
			return parse(radix, s, Parse::toByte, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toUbyte(CharSequence s) {
			return toUbyte(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static byte toUbyte(int radix, CharSequence s) {
			return new Parser(s).unsigned().radix(radix).noSign().parseByte();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toUbyte(CharSequence s, Byte def) {
			return toUbyte(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Byte toUbyte(int radix, CharSequence s, Byte def) {
			return parse(radix, s, Parse::toUbyte, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toShort(CharSequence s) {
			return toShort(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toShort(int radix, CharSequence s) {
			return new Parser(s).signed().radix(radix).noSign().parseShort();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toShort(CharSequence s, Short def) {
			return toShort(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toShort(int radix, CharSequence s, Short def) {
			return parse(radix, s, Parse::toShort, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toUshort(CharSequence s) {
			return toUshort(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static short toUshort(int radix, CharSequence s) {
			return new Parser(s).unsigned().radix(radix).noSign().parseShort();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toUshort(CharSequence s, Short def) {
			return toUshort(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Short toUshort(int radix, CharSequence s, Short def) {
			return parse(radix, s, Parse::toUshort, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toInt(CharSequence s) {
			return toInt(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toInt(int radix, CharSequence s) {
			return new Parser(s).signed().radix(radix).noSign().parseInt();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toInt(CharSequence s, Integer def) {
			return toInt(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toInt(int radix, CharSequence s, Integer def) {
			return parse(radix, s, Parse::toInt, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toUint(CharSequence s) {
			return toUint(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static int toUint(int radix, CharSequence s) {
			return new Parser(s).unsigned().radix(radix).noSign().parseInt();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toUint(CharSequence s, Integer def) {
			return toUint(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Integer toUint(int radix, CharSequence s, Integer def) {
			return parse(radix, s, Parse::toUint, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toLong(CharSequence s) {
			return toLong(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toLong(int radix, CharSequence s) {
			return new Parser(s).signed().radix(radix).noSign().parseLong();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toLong(CharSequence s, Long def) {
			return toLong(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toLong(int radix, CharSequence s, Long def) {
			return parse(radix, s, Parse::toLong, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toUlong(CharSequence s) {
			return toUlong(Radix.DEC.n, s);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static long toUlong(int radix, CharSequence s) {
			return new Parser(s).unsigned().radix(radix).noSign().parseLong();
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toUlong(CharSequence s, Long def) {
			return toUlong(Radix.DEC.n, s, def);
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Long toUlong(int radix, CharSequence s, Long def) {
			return parse(radix, s, Parse::toUlong, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static Float toFloat(CharSequence s) {
			return Float.parseFloat(Strings.safe(s));
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Float toFloat(CharSequence s, Float def) {
			return parse(s, Parse::toFloat, def);
		}

		/**
		 * Parses char sequence; throws exception if invalid.
		 */
		public static Double toDouble(CharSequence s) {
			return Double.parseDouble(Strings.safe(s));
		}

		/**
		 * Parses char sequence; returns default if invalid.
		 */
		public static Double toDouble(CharSequence s, Double def) {
			return parse(s, Parse::toDouble, def);
		}
	}

	private static class Parser {
		private boolean positive = true;
		private int radix = 10;
		private int i = 0;
		private final CharSequence s;

		private Parser(CharSequence s) {
			this.s = Chars.safe(s);
		}

		/**
		 * Parses to check for positive, negative, or no sign at current position.
		 */
		public Parser signed() {
			if (i >= s.length()) return this;
			char c = s.charAt(i);
			if (c == '+' || c == '-') i++;
			if (c == '-') positive = false;
			return this;
		}

		/**
		 * Parses to make sure positive sign or no sign at current position.
		 */
		public Parser unsigned() {
			if (i >= s.length()) return this;
			char c = s.charAt(i);
			if (c == '-') throw nfe("Number cannot be negative: \"%s\"", s);
			if (c == '+') i++;
			return this;
		}

		/**
		 * Parses radix at current position.
		 */
		public Parser radix() {
			var prefix = Radix.Prefix.find(s, i);
			if (prefix.radix().isValid()) {
				i += prefix.prefix().length();
				radix = prefix.radix().n;
			}
			return this;
		}

		/**
		 * Sets the given radix.
		 */
		public Parser radix(int radix) {
			this.radix = Radix.validate(radix);
			return this;
		}

		/**
		 * Verifies that +/- sign is not at the current position, before calling parseXxx method.
		 */
		public Parser noSign() {
			if (i >= s.length()) return this;
			char c = s.charAt(i);
			if (c == '+' || c == '-') throw nfe("Unexpected sign at index %d: \"%s\"", i, s);
			return this;
		}

		/**
		 * Parses the remaining string.
		 */
		public byte parseByte() {
			int ivalue = Integer.parseUnsignedInt(s, i, s.length(), radix);
			if (ivalue < 0 || ivalue > Maths.MAX_UBYTE)
				throw nfe("Number out of byte range -0xff to 0xff: \"%s\"", s);
			byte value = (byte) ivalue;
			return (positive || value == Byte.MIN_VALUE) ? value : (byte) -value;
		}

		/**
		 * Parses the remaining string.
		 */
		public short parseShort() {
			int ivalue = Integer.parseUnsignedInt(s, i, s.length(), radix);
			if (ivalue < 0 || ivalue > Maths.MAX_USHORT)
				throw nfe("Number out of short range -0xffff to 0xffff: \"%s\"", s);
			short value = (short) ivalue;
			return (positive || value == Short.MIN_VALUE) ? value : (short) -value;
		}

		/**
		 * Parses the remaining string.
		 */
		public int parseInt() {
			int value = Integer.parseUnsignedInt(s, i, s.length(), radix);
			return (positive || value == Integer.MIN_VALUE) ? value : -value;
		}

		/**
		 * Parses the remaining string.
		 */
		public long parseLong() {
			long value = Long.parseUnsignedLong(s, i, s.length(), radix);
			return (positive || value == Long.MIN_VALUE) ? value : -value;
		}
	}

	private static Boolean parseBool(CharSequence value) {
		if (Strings.isEmpty(value)) return null;
		var s = Chars.lower(value).toString();
		if (TRUE.contains(s)) return Boolean.TRUE;
		if (FALSE.contains(s)) return Boolean.FALSE;
		return null;
	}

	private static <T> T parse(CharSequence s, Functions.Function<CharSequence, T> parser, T def) {
		try {
			return Strings.isEmpty(s) ? def : parser.apply(s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static <T> T parse(int radix, CharSequence s, RadixParser<T> radixParser, T def) {
		try {
			return Strings.isEmpty(s) ? def : radixParser.parse(radix, s);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	private static NumberFormatException nfe(String format, Object... args) {
		return new NumberFormatException(String.format(format, args));
	}
}
