package ceri.common.text;

import ceri.common.function.Functions;
import ceri.common.math.Radix;

/**
 * Utility methods for parsing types, with defaults if invalid.
 */
public class ParseUtil {
	// Convenience filters for Parser.String
	public static final Functions.Function<String, Boolean> BOOL = ParseUtil::parseBool;
	public static final Functions.Function<String, Byte> BYTE = ParseUtil::parseByte;
	public static final Functions.Function<String, Byte> DBYTE = ParseUtil::decodeByte;
	public static final Functions.Function<String, Short> SHORT = ParseUtil::parseShort;
	public static final Functions.Function<String, Short> DSHORT = ParseUtil::decodeShort;
	public static final Functions.Function<String, Integer> INT = ParseUtil::parseInt;
	public static final Functions.Function<String, Integer> DINT = ParseUtil::decodeInt;
	public static final Functions.Function<String, Long> LONG = ParseUtil::parseLong;
	public static final Functions.Function<String, Long> DLONG = ParseUtil::decodeLong;
	public static final Functions.Function<String, Float> FLOAT = ParseUtil::parseFloat;
	public static final Functions.Function<String, Double> DOUBLE = ParseUtil::parseDouble;

	private ParseUtil() {}

	/**
	 * Parses boolean, or returns null if null or empty.
	 */
	public static Boolean parseBool(String value) {
		return parseBool(value, null);
	}

	/**
	 * Parses boolean, or returns default if null or empty.
	 */
	public static Boolean parseBool(String value, Boolean def) {
		if (Strings.isEmpty(value)) return def;
		return Boolean.parseBoolean(value);
	}

	/**
	 * Parses byte, or returns null if null, empty or invalid.
	 */
	public static Byte parseByte(String value) {
		return parseByte(value, null);
	}

	/**
	 * Parses byte, or returns default if null, empty or invalid.
	 */
	public static Byte parseByte(String value, Byte def) {
		return parseByte(value, def, Radix.DEC.n);
	}

	/**
	 * Parses byte with radix, or returns default if null, empty or invalid.
	 */
	public static Byte parseByte(String value, Byte def, int radix) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.parseByte(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Parses short, or returns null if null, empty or invalid.
	 */
	public static Short parseShort(String value) {
		return parseShort(value, null);
	}

	/**
	 * Parses short, or returns default if null, empty or invalid.
	 */
	public static Short parseShort(String value, Short def) {
		return parseShort(value, def, Radix.DEC.n);
	}

	/**
	 * Parses short with radix, or returns default if null, empty or invalid.
	 */
	public static Short parseShort(String value, Short def, int radix) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.parseShort(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Parses int, or returns null if null, empty or invalid.
	 */
	public static Integer parseInt(String value) {
		return parseInt(value, null);
	}

	/**
	 * Parses int, or returns default if null, empty or invalid.
	 */
	public static Integer parseInt(String value, Integer def) {
		return parseInt(value, def, Radix.DEC.n);
	}

	/**
	 * Parses int with radix, or returns default if null, empty or invalid.
	 */
	public static Integer parseInt(String value, Integer def, int radix) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.parseInt(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Parses long, or returns null if null, empty or invalid.
	 */
	public static Long parseLong(String value) {
		return parseLong(value, null);
	}

	/**
	 * Parses long, or returns default if null, empty or invalid.
	 */
	public static Long parseLong(String value, Long def) {
		return parseLong(value, def, Radix.DEC.n);
	}

	/**
	 * Parses long with radix, or returns default if null, empty or invalid.
	 */
	public static Long parseLong(String value, Long def, int radix) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.parseLong(value, radix);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Parses float, or returns null if null, empty or invalid.
	 */
	public static Float parseFloat(String value) {
		return parseFloat(value, null);
	}

	/**
	 * Parses float, or returns default if null, empty or invalid.
	 */
	public static Float parseFloat(String value, Float def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Parses double, or returns null if null, empty or invalid.
	 */
	public static Double parseDouble(String value) {
		return parseDouble(value, null);
	}

	/**
	 * Parses double, or returns default if null, empty or invalid.
	 */
	public static Double parseDouble(String value, Double def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Decodes byte, or returns null if null, empty or invalid.
	 */
	public static Byte decodeByte(String value) {
		return decodeByte(value, null);
	}

	/**
	 * Decodes byte, or returns default if null, empty or invalid.
	 */
	public static Byte decodeByte(String value, Byte def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.decodeByte(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Decodes short, or returns null if null, empty or invalid.
	 */
	public static Short decodeShort(String value) {
		return decodeShort(value, null);
	}

	/**
	 * Decodes short, or returns default if null, empty or invalid.
	 */
	public static Short decodeShort(String value, Short def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.decodeShort(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Decodes int, or returns null if null, empty or invalid.
	 */
	public static Integer decodeInt(String value) {
		return decodeInt(value, null);
	}

	/**
	 * Decodes int, or returns default if null, empty or invalid.
	 */
	public static Integer decodeInt(String value, Integer def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.decodeInt(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Decodes int, or returns null if null, empty or invalid.
	 */
	public static Long decodeLong(String value) {
		return decodeLong(value, null);
	}

	/**
	 * Decodes long, or returns default if null, empty or invalid.
	 */
	public static Long decodeLong(String value, Long def) {
		if (Strings.isEmpty(value)) return def;
		try {
			return NumberParser.decodeLong(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}
}
