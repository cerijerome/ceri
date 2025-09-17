package ceri.common.text;

import java.util.Set;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.math.Radix;
import ceri.common.util.Basics;

/**
 * Methods for parsing strings to numeric types.
 */
public class Parse {
	private static final Set<String> TRUE = Set.of("true", "yes", "1");
	private static final Set<String> FALSE = Set.of("false", "no", "0");
	public static final Function<Boolean> BOOL = s -> parseBool(s, null);
	public static final Function<Byte> BYTE = s -> parseByte(s, null);
	public static final Function<Byte> UBYTE = s -> parseUbyte(s, null);
	public static final Function<Short> SHORT = s -> parseShort(s, null);
	public static final Function<Short> USHORT = s -> parseUshort(s, null);
	public static final Function<Integer> INT = s -> parseInt(s, null);
	public static final Function<Integer> UINT = s -> parseUint(s, null);
	public static final Function<Long> LONG = s -> parseLong(s, null);
	public static final Function<Long> ULONG = s -> parseUlong(s, null);
	public static final Function<Float> FLOAT = s -> parseFloat(s, null);
	public static final Function<Double> DOUBLE = s -> parseDouble(s, null);

	private Parse() {}

	/**
	 * Parses char sequence to type, returning null if invalid.
	 */
	public interface Function<T> extends Functions.Function<CharSequence, T> {}
	
	/**
	 * Interface to avoid reversing args in ObjIntFunction.
	 */
	private interface RadixParser<T> {
		T parse(int radix, CharSequence s);
	}

	/**
	 * Sequential parsing of int types.
	 */
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

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static byte decodeByte(CharSequence s) {
		return new Parser(s).signed().radix().noSign().parseByte();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Byte decodeByte(CharSequence s, Byte def) {
		return parse(s, Parse::decodeByte, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static byte decodeUbyte(CharSequence s) {
		return new Parser(s).unsigned().radix().noSign().parseByte();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Byte decodeUbyte(CharSequence s, Byte def) {
		return parse(s, Parse::decodeUbyte, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static short decodeShort(CharSequence s) {
		return new Parser(s).signed().radix().noSign().parseShort();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Short decodeShort(CharSequence s, Short def) {
		return parse(s, Parse::decodeShort, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static short decodeUshort(CharSequence s) {
		return new Parser(s).unsigned().radix().noSign().parseShort();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Short decodeUshort(CharSequence s, Short def) {
		return parse(s, Parse::decodeUshort, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static int decodeInt(CharSequence s) {
		return new Parser(s).signed().radix().noSign().parseInt();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Integer decodeInt(CharSequence s, Integer def) {
		return parse(s, Parse::decodeInt, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static int decodeUint(CharSequence s) {
		return new Parser(s).unsigned().radix().noSign().parseInt();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Integer decodeUint(CharSequence s, Integer def) {
		return parse(s, Parse::decodeUint, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static long decodeLong(CharSequence s) {
		return new Parser(s).signed().radix().noSign().parseLong();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Long decodeLong(CharSequence s, Long def) {
		return parse(s, Parse::decodeLong, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static long decodeUlong(CharSequence s) {
		return new Parser(s).unsigned().radix().noSign().parseLong();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Long decodeUlong(CharSequence s, Long def) {
		return parse(s, Parse::decodeUlong, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static boolean parseBool(CharSequence s) {
		var bool = bool(s);
		if (bool != null) return bool.booleanValue();
		throw Parse.nfe("Invalid bool: ", s);
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Boolean parseBool(CharSequence s, Boolean def) {
		return Basics.def(bool(s), def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static byte parseByte(CharSequence s) {
		return parseByte(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static byte parseByte(int radix, CharSequence s) {
		return new Parser(s).signed().radix(radix).noSign().parseByte();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Byte parseByte(CharSequence s, Byte def) {
		return parseByte(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Byte parseByte(int radix, CharSequence s, Byte def) {
		return parse(radix, s, Parse::parseByte, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static byte parseUbyte(CharSequence s) {
		return parseUbyte(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static byte parseUbyte(int radix, CharSequence s) {
		return new Parser(s).unsigned().radix(radix).noSign().parseByte();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Byte parseUbyte(CharSequence s, Byte def) {
		return parseUbyte(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Byte parseUbyte(int radix, CharSequence s, Byte def) {
		return parse(radix, s, Parse::parseUbyte, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static short parseShort(CharSequence s) {
		return parseShort(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static short parseShort(int radix, CharSequence s) {
		return new Parser(s).signed().radix(radix).noSign().parseShort();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Short parseShort(CharSequence s, Short def) {
		return parseShort(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Short parseShort(int radix, CharSequence s, Short def) {
		return parse(radix, s, Parse::parseShort, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static short parseUshort(CharSequence s) {
		return parseUshort(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static short parseUshort(int radix, CharSequence s) {
		return new Parser(s).unsigned().radix(radix).noSign().parseShort();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Short parseUshort(CharSequence s, Short def) {
		return parseUshort(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Short parseUshort(int radix, CharSequence s, Short def) {
		return parse(radix, s, Parse::parseUshort, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static int parseInt(CharSequence s) {
		return parseInt(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static int parseInt(int radix, CharSequence s) {
		return new Parser(s).signed().radix(radix).noSign().parseInt();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Integer parseInt(CharSequence s, Integer def) {
		return parseInt(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Integer parseInt(int radix, CharSequence s, Integer def) {
		return parse(radix, s, Parse::parseInt, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static int parseUint(CharSequence s) {
		return parseUint(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static int parseUint(int radix, CharSequence s) {
		return new Parser(s).unsigned().radix(radix).noSign().parseInt();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Integer parseUint(CharSequence s, Integer def) {
		return parseUint(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Integer parseUint(int radix, CharSequence s, Integer def) {
		return parse(radix, s, Parse::parseUint, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static long parseLong(CharSequence s) {
		return parseLong(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static long parseLong(int radix, CharSequence s) {
		return new Parser(s).signed().radix(radix).noSign().parseLong();
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Long parseLong(CharSequence s, Long def) {
		return parseLong(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Long parseLong(int radix, CharSequence s, Long def) {
		return parse(radix, s, Parse::parseLong, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static long parseUlong(CharSequence s) {
		return parseUlong(Radix.DEC.n, s);
	}

	/**
	 * Parses char sequence; throws exception if invalid. Only positive values are valid.
	 */
	public static long parseUlong(int radix, CharSequence s) {
		return new Parser(s).unsigned().radix(radix).noSign().parseLong();
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Long parseUlong(CharSequence s, Long def) {
		return parseUlong(Radix.DEC.n, s, def);
	}

	/**
	 * Parses char sequence; returns default if invalid. Only positive values are valid.
	 */
	public static Long parseUlong(int radix, CharSequence s, Long def) {
		return parse(radix, s, Parse::parseUlong, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static Float parseFloat(CharSequence s) {
		return Float.parseFloat(Strings.safe(s));
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Float parseFloat(CharSequence s, Float def) {
		return parse(s, Parse::parseFloat, def);
	}

	/**
	 * Parses char sequence; throws exception if invalid.
	 */
	public static Double parseDouble(CharSequence s) {
		return Double.parseDouble(Strings.safe(s));
	}

	/**
	 * Parses char sequence; returns default if invalid.
	 */
	public static Double parseDouble(CharSequence s, Double def) {
		return parse(s, Parse::parseDouble, def);
	}

	private static Boolean bool(CharSequence value) {
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
