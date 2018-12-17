package ceri.common.text;

import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.StreamUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.util.BasicUtil;
import ceri.common.util.HAlign;
import ceri.common.util.PrimitiveUtil;

/**
 * String-based utilities. See also TextUtil for more word-based formatting utilities.
 */
public class StringUtil {
	private static final String INTEGRAL_FLOAT = ".0";
	private static final char UNPRINTABLE_CHAR = '.';
	private static final char BACKSLASH = '\\';
	private static final char BACKSPACE = '\b';
	private static final char ESCAPE = '\u001b';
	private static final char TAB = '\t';
	private static final char FF = '\f';
	private static final char CR = '\r';
	private static final char NL = '\n';
	private static final char NULL = '\0';
	private static final String ESCAPED_NULL = "\\0";
	private static final String ESCAPED_BACKSLASH = "\\\\";
	private static final String ESCAPED_BACKSPACE = "\\b";
	private static final String ESCAPED_ESCAPE = "\\e";
	private static final String ESCAPED_TAB = "\\t";
	private static final String ESCAPED_FF = "\\f";
	private static final String ESCAPED_CR = "\\r";
	private static final String ESCAPED_NL = "\\n";
	private static final String ESCAPED_OCTAL = "\\0";
	private static final String ESCAPED_HEX = "\\x";
	private static final String ESCAPED_UTF16 = "\\u";
	private static final Pattern ESCAPE_REGEX =
		Pattern.compile("\\\\\\\\|\\\\b|\\\\e|\\\\t|\\\\f|\\\\r|\\\\n|" +
			"\\\\0[0-3][0-7]{2}|\\\\0[0-7]{2}|\\\\0[0-7]|\\\\0|" +
			"\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}");
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	public static final int HEX_RADIX = 16;
	public static final int DECIMAL_RADIX = 10;
	public static final int OCTAL_RADIX = 8;
	public static final int BINARY_RADIX = 2;
	public static final Pattern NEWLINE_REGEX = Pattern.compile("(\\r\\n|\\n|\\r)");
	public static final Pattern COMMA_SPLIT_REGEX = Pattern.compile("\\s*,\\s*");
	public static final Pattern WHITE_SPACE_REGEX = Pattern.compile("\\s+");
	public static final int LONG_HEX_DIGITS = 16;
	public static final int INT_HEX_DIGITS = 8;
	public static final int SHORT_HEX_DIGITS = 4;
	public static final int BYTE_HEX_DIGITS = 2;
	public static final int LONG_BINARY_DIGITS = 64;
	public static final int INT_BINARY_DIGITS = 32;
	public static final int SHORT_BINARY_DIGITS = 16;
	public static final int BYTE_BINARY_DIGITS = 8;

	private StringUtil() {}

	public static DecimalFormat decimalFormat(int decimalPlaces) {
		StringBuilder b = new StringBuilder("0");
		if (decimalPlaces > 0) b.append(".");
		while (decimalPlaces-- > 0)
			b.append("#");
		DecimalFormat format = new DecimalFormat(b.toString());
		format.setRoundingMode(RoundingMode.HALF_UP);
		return format;
	}

	/**
	 * Extracts the first substring with matching open and close brackets. Extraction includes
	 * bracket chars.
	 * 
	 */
	public static String extractBrackets(String s, char open, char close) {
		if (s == null) return null;
		int count = 0;
		int start = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == open) {
				if (count == 0) start = i;
				count++;
			} else if (c == close) {
				count--;
				if (count == 0) return s.substring(start, i + 1);
			}
		}
		return null;
	}

	/**
	 * Creates a string from code points.
	 */
	public static String toString(int... codePoints) {
		return StreamUtil.toString(IntStream.of(codePoints));
	}

	/**
	 * Escapes non-visible characters within the given string.
	 */
	public static String escape(String s) {
		return replaceUnprintable(s, StringUtil::escapeChar);
	}

	/**
	 * Escapes a non-printable char.
	 */
	static String escapeChar(char c) {
		switch (c) {
		case BACKSLASH:
			return ESCAPED_BACKSLASH;
		case BACKSPACE:
			return ESCAPED_BACKSPACE;
		case ESCAPE:
			return ESCAPED_ESCAPE;
		case FF:
			return ESCAPED_FF;
		case NL:
			return ESCAPED_NL;
		case CR:
			return ESCAPED_CR;
		case TAB:
			return ESCAPED_TAB;
		case NULL:
			return ESCAPED_NULL;
		default:
			return ESCAPED_UTF16 + toHex(c, SHORT_HEX_DIGITS);
		}
	}

	/**
	 * Encodes escaped characters within the given string.
	 */
	public static String unEscape(String s) {
		return replaceAll(s, ESCAPE_REGEX, m -> String.valueOf(unEscapeChar(m.group())));
	}

	/**
	 * Encodes an escaped character string.
	 */
	static char unEscapeChar(String escapedChar) {
		if (escapedChar == null) return NULL;
		switch (escapedChar) {
		case ESCAPED_BACKSLASH:
			return BACKSLASH;
		case ESCAPED_BACKSPACE:
			return BACKSPACE;
		case ESCAPED_ESCAPE:
			return ESCAPE;
		case ESCAPED_FF:
			return FF;
		case ESCAPED_NL:
			return NL;
		case ESCAPED_CR:
			return CR;
		case ESCAPED_TAB:
			return TAB;
		case ESCAPED_NULL:
			return NULL;
		}
		Character c = escaped(escapedChar, ESCAPED_OCTAL, OCTAL_RADIX);
		if (c == null) c = escaped(escapedChar, ESCAPED_HEX, HEX_RADIX);
		if (c == null) c = escaped(escapedChar, ESCAPED_UTF16, HEX_RADIX);
		if (c == null) return NULL;
		return c.charValue();
	}

	private static Character escaped(String escapedChar, String prefix, int radix) {
		if (!escapedChar.startsWith(prefix)) return null;
		return (char) Integer.parseInt(escapedChar.substring(prefix.length()), radix);
	}

	public static String repeat(String s, int n) {
		if (s == null) return null;
		if (n == 0 || s.isEmpty()) return "";
		if (n == 1) return s;
		return IntStream.range(0, n).mapToObj(i -> s).collect(Collectors.joining());
	}

	/**
	 * Calls String.replaceAll with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(String s, String pattern, String replacement) {
		return s.replaceAll(pattern, Matcher.quoteReplacement(replacement));
	}

	/**
	 * Replace all instances of the pattern using the replacer function.
	 */
	public static String replaceAll(String s, String pattern,
		Function<MatchResult, String> replacer) {
		return replaceAll(s, Pattern.compile(pattern), replacer);
	}

	/**
	 * Replace all instances of the pattern using the replacer function.
	 */
	public static String replaceAll(String s, Pattern p, Function<MatchResult, String> replacer) {
		StringBuffer output = new StringBuffer();
		Matcher m = p.matcher(s);
		while (m.find()) {
			String replacement = replacer.apply(m);
			m.appendReplacement(output, "");
			output.append(replacement);
		}
		m.appendTail(output);
		return output.toString();
	}

	/**
	 * Splits a string at given indexes.
	 */
	public static List<String> split(String s, Collection<Integer> indexes) {
		return split(s, PrimitiveUtil.toIntArray(indexes));
	}

	/**
	 * Splits a string at given indexes.
	 */
	public static List<String> split(String s, int... indexes) {
		List<String> list = new ArrayList<>(indexes.length);
		int last = 0;
		for (int i : indexes) {
			if (i >= s.length()) i = s.length();
			list.add(s.substring(last, i));
			last = i;
		}
		list.add(s.substring(last));
		return list;
	}

	/**
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(float f) {
		String s = Float.toString(f);
		if (s.endsWith(INTEGRAL_FLOAT)) s = s.substring(0, s.length() - INTEGRAL_FLOAT.length());
		return s;
	}

	/**
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(double d) {
		String s = Double.toString(d);
		if (s.endsWith(INTEGRAL_FLOAT)) s = s.substring(0, s.length() - INTEGRAL_FLOAT.length());
		return s;
	}

	/**
	 * Replace multiple whitespaces with a single space, then trim.
	 */
	public static String compact(String s) {
		if (s == null) return null;
		return WHITE_SPACE_REGEX.matcher(s).replaceAll(" ").trim();
	}

	/**
	 * Convert a byte array into a list of hex strings.
	 */
	public static String toHexArray(byte[] data) {
		if (data.length == 0) return "[]";
		return IntStream.range(0, data.length).mapToObj(i -> toHex(data[i]))
			.collect(Collectors.joining(", 0x", "[0x", "]"));
	}

	/**
	 * Converts a byte array into a single hex string.
	 */
	public static String toHex(byte[] data) {
		if (data.length == 0) return "";
		return IntStream.range(0, data.length).mapToObj(i -> toHex(data[i]))
			.collect(Collectors.joining());
	}

	/**
	 * Convenience method to convert a long to a 16-digit hex string.
	 */
	public static String toHex(long l) {
		return toHex(l, LONG_HEX_DIGITS);
	}

	/**
	 * Convenience method to convert an int to a 8-digit hex string.
	 */
	public static String toHex(int i) {
		return toHex(i, INT_HEX_DIGITS);
	}

	/**
	 * Convenience method to convert a short to a 4-digit hex string.
	 */
	public static String toHex(short s) {
		return toHex(s, SHORT_HEX_DIGITS);
	}

	/**
	 * Convenience method to convert a byte to a 2-digit hex string.
	 */
	public static String toHex(byte b) {
		return toHex(b, BYTE_HEX_DIGITS);
	}

	/**
	 * Converts a number to radix-based string with exactly the numbers of specified digits. For
	 * numbers larger than the digits specified, the most significant digits are dropped.
	 */
	public static String toHex(long l, int digits) {
		return toUnsigned(l, HEX_RADIX, digits);
	}

	/**
	 * Convenience method to convert a byte to an 8-digit binary string.
	 */
	public static String toBinary(byte b) {
		return toBinary(b, BYTE_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert a short to a 16-digit binary string.
	 */
	public static String toBinary(short s) {
		return toBinary(s, SHORT_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert an int to a 32-digit binary string.
	 */
	public static String toBinary(int i) {
		return toBinary(i, INT_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert a long to a 64-digit binary string.
	 */
	public static String toBinary(long l) {
		return toBinary(l, LONG_BINARY_DIGITS);
	}

	/**
	 * Converts a number to radix-based string with exactly the numbers of specified digits. For
	 * numbers larger than the digits specified, the most significant digits are dropped.
	 */
	public static String toBinary(long l, int digits) {
		return toUnsigned(l, BINARY_RADIX, digits);
	}

	/**
	 * Converts a number to radix-based string with exactly the numbers of specified digits. For
	 * numbers larger than the digits specified, the most significant digits are dropped.
	 */
	public static String toUnsigned(long l, int radix, int digits) {
		String s = Long.toUnsignedString(l, radix);
		s = pad(s, digits, "0", HAlign.right);
		int len = s.length();
		return s.substring(len - digits, len);
	}

	/**
	 * Uses URLEncoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String urlEncode(String s) {
		return URLEncoder.encode(s, UTF8);
	}

	/**
	 * Uses URLDecoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String urlDecode(String s) {
		return URLDecoder.decode(s, UTF8);
	}

	/**
	 * Splits a string by commas and trims each entry. Trailing empty strings are dropped as with
	 * the regular split method.
	 */
	public static List<String> commaSplit(String s) {
		return split(s, COMMA_SPLIT_REGEX);
	}

	/**
	 * Splits a string by whitespace and trims each entry. Trailing empty strings are dropped as
	 * with the regular split method.
	 */
	public static List<String> whiteSpaceSplit(String s) {
		return split(s, WHITE_SPACE_REGEX);
	}

	/**
	 * Splits a string by pattern and trims each entry. Trailing empty strings are dropped as with
	 * the regular split method.
	 */
	public static List<String> split(String s, Pattern pattern) {
		if (BasicUtil.isEmpty(s)) return Collections.emptyList();
		return StreamUtil.toList(Stream.of(pattern.split(s)).map(String::trim));
	}

	/**
	 * Checks if a char is printable
	 */
	public static boolean isPrintable(char c) {
		if (Character.isISOControl(c) || c == KeyEvent.CHAR_UNDEFINED) return false;
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return block != Character.UnicodeBlock.SPECIALS;
	}

	/**
	 * Replaces unprintable chars with '.'.
	 */
	public static String printable(String s) {
		return replaceUnprintable(s, UNPRINTABLE_CHAR);
	}

	/**
	 * Replaces unprintable chars with given char.
	 */
	public static String replaceUnprintable(String s, char replace) {
		if (s == null) return null;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			b.append(isPrintable(c) ? c : replace);
		}
		return b.toString();
	}

	/**
	 * Replaces unprintable chars using replacer function.
	 */
	public static String replaceUnprintable(String s, Function<Character, String> replacer) {
		if (s == null) return null;
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (isPrintable(c)) b.append(c);
			else b.append(replacer.apply(c));
		}
		return b.toString();
	}

	/**
	 * Counts the number of instances of the given char within the string.
	 */
	public static int count(String str, char ch) {
		if (str.isEmpty()) return 0;
		int count = 0;
		int i = 0;
		int len = str.length();
		while (i < len) {
			i = str.indexOf(ch, i);
			if (i == -1) break;
			count++;
			i++;
		}
		return count;
	}

	/**
	 * Counts the number of instances of the given substring within the string.
	 */
	public static int count(String str, String sub) {
		if (sub.isEmpty() || str.isEmpty()) return 0;
		int count = 0;
		int i = 0;
		int len = str.length();
		int subLen = sub.length();
		while (i < len) {
			i = str.indexOf(sub, i);
			if (i == -1) break;
			count++;
			i += subLen;
		}
		return count;
	}

	/**
	 * Creates a formatted string for iterable items: item1[separator]item2[separator]...
	 */
	public static String toString(String separator, Iterable<?> iterable) {
		return toString("", "", separator, iterable);
	}

	/**
	 * Creates a formatted string for iterable items: [pre]item1[separator]item2[separator]...[post]
	 */
	public static String toString(String pre, String post, String separator, Iterable<?> iterable) {
		StringBuilder b = new StringBuilder(pre);
		boolean first = true;
		for (Object obj : iterable) {
			if (!first) b.append(separator);
			b.append(obj);
			first = false;
		}
		return b.append(post).toString();
	}

	/**
	 * Convenience method to prevent callers needing to cast to Object[].
	 */
	public static String toString(String pre, String post, String separator, String... objects) {
		return toString(pre, post, separator, (Object[]) objects);
	}

	/**
	 * Creates a formatted string for an array of items:
	 * [pre]item1[separator]item2[separator]...[post]
	 */
	public static String toString(String pre, String post, String separator, Object... objects) {
		StringBuilder b = new StringBuilder(pre);
		boolean first = true;
		for (Object obj : objects) {
			if (!first) b.append(separator);
			b.append(obj);
			first = false;
		}
		return b.append(post).toString();
	}

	public static String toLowerCase(String s) {
		if (s == null) return null;
		return s.toLowerCase();
	}

	public static String toUpperCase(String s) {
		if (s == null) return null;
		return s.toUpperCase();
	}

	public static boolean startsWithIgnoreCase(String lhs, String rhs) {
		if (lhs == rhs) return true;
		if (lhs == null || rhs == null) return false;
		if (lhs.length() < rhs.length()) return false;
		return lhs.substring(0, rhs.length()).equalsIgnoreCase(rhs);
	}

	/**
	 * Pads a number with leading zeros.
	 */
	public static String pad(long value, int minLength) {
		if (value == Long.MIN_VALUE) {
			String str = String.valueOf(value).substring(1);
			return "-" + pad(str, minLength - 1, "0", HAlign.right);
		}
		if (value < 0) return "-" + pad(-value, minLength - 1);
		return pad(String.valueOf(value), minLength, "0", HAlign.right);
	}

	/**
	 * Pads a string with leading spaces.
	 */
	public static String pad(String str, int minLength) {
		return pad(str, minLength, " ", HAlign.right);
	}

	/**
	 * Pads a string with leading or trailing characters.
	 */
	public static String pad(String str, int minLength, HAlign align) {
		return pad(str, minLength, " ", align);
	}

	/**
	 * Pads a string with leading or trailing characters.
	 */
	public static String pad(String str, int minLength, String pad, HAlign align) {
		if (str == null) str = "";
		if (pad == null) pad = "";
		if (str.length() >= minLength) return str;
		int pads = pads(pad.length(), minLength - str.length());
		int left = leftCount(pads, align);
		if (pads == 0) return str;
		StringBuilder b = new StringBuilder(minLength);
		for (int i = 0; i < left; i++)
			b.append(pad);
		b.append(str);
		for (int i = 0; i < pads - left; i++)
			b.append(pad);
		return b.toString();
	}

	private static int pads(int padLen, int len) {
		return padLen > 0 ? len / padLen : 0;
	}

	private static int leftCount(int count, HAlign align) {
		if (align == HAlign.right) return count;
		if (align == HAlign.center) return count / 2;
		return 0;
	}

	/**
	 * Trims a string if not null.
	 */
	public static String trim(String s) {
		if (s == null) return s;
		return s.trim();
	}

	/**
	 * Returns substring, or "" if null or index out of bounds. Use start < 0 for length relative to
	 * end.
	 */
	public static String safeSubstring(String s, int start) {
		return safeSubstring(s, start, -1);
	}

	/**
	 * Returns substring, or "" if null or index out of bounds. Use end = -1 for no end limit. Use
	 * start < 0 for length relative to end.
	 */
	public static String safeSubstring(String s, int start, int end) {
		if (s == null) return "";
		int len = s.length();
		if (start >= len) return "";
		if (end > len || end == -1) end = len;
		if (start < 0) start = end + start;
		if (start < 0) start = 0;
		return s.substring(start, end);
	}

	/**
	 * 
	 */
	public static <E extends Exception> String print(ExceptionConsumer<E, PrintStream> consumer)
		throws E {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = asPrintStream(b)) {
			consumer.accept(out);
		}
		return b.toString();
	}

	/**
	 * Wrap a PrintStream around a string builder. PrintStream will flush automatically.
	 */
	public static PrintStream asPrintStream(final StringBuilder s) {
		return new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				s.append((char) b);
			}

			@Override
			public void write(byte[] b, int off, int len) {
				s.append(new String(b, off, len));
			}
		}, true);
	}

	/**
	 * Prefixes each line of the string with given prefix.
	 */
	public static String prefixLines(String prefix, String s) {
		if (prefix.isEmpty()) return s;
		return prefix +
			NEWLINE_REGEX.matcher(s).replaceAll("$1" + Matcher.quoteReplacement(prefix));
	}

}
