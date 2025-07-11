package ceri.common.text;

import static ceri.common.text.RegexUtil.replaceAllQuoted;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions.IntBiPredicate;
import ceri.common.math.MathUtil;
import ceri.common.stream.StreamUtil;
import ceri.common.util.Align;

/**
 * String-based utilities. See also TextUtil for more word-based formatting utilities.
 */
public class StringUtil {
	private static final String INTEGRAL_FLOAT = ".0";
	private static final char UNPRINTABLE_CHAR = '.';
	public static final char BACKSLASH = '\\';
	public static final char NULL = '\0';
	public static final char BS = '\b';
	public static final char TAB = '\t';
	public static final char NL = '\n';
	public static final char FF = '\f';
	public static final char CR = '\r';
	public static final char ESC = '\u001b';
	public static final char DEL = '\u007f';
	public static final String NULL_STRING = "null";
	public static final String EOL = System.lineSeparator();
	private static final String ESCAPED_NULL = "\\0";
	private static final String ESCAPED_BACKSLASH = "\\\\";
	private static final String ESCAPED_BS = "\\b";
	private static final String ESCAPED_ESC = "\\e";
	private static final String ESCAPED_TAB = "\\t";
	private static final String ESCAPED_FF = "\\f";
	private static final String ESCAPED_CR = "\\r";
	private static final String ESCAPED_NL = "\\n";
	private static final String ESCAPED_OCTAL = "\\0";
	private static final String ESCAPED_HEX = "\\x";
	private static final String ESCAPED_UTF16 = "\\u";
	private static final Pattern ESCAPE_REGEX =
		Pattern.compile("\\\\\\\\|\\\\b|\\\\e|\\\\t|\\\\f|\\\\r|\\\\n|"
			+ "\\\\0[0-3][0-7]{2}|\\\\0[0-7]{2}|\\\\0[0-7]|\\\\0|"
			+ "\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}");
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
	public static final int HEX_BINARY_DIGITS = 4;
	public static final Excepts.Predicate<RuntimeException, String> NOT_EMPTY = s -> !empty(s);
	public static final Excepts.Predicate<RuntimeException, String> NOT_BLANK = s -> !blank(s);

	private StringUtil() {}

	/**
	 * Returns the char at index, or default if out of range or char sequence is null.
	 */
	public static Character charAt(CharSequence s, int index, Character def) {
		if (s == null || index < 0 || index >= s.length()) return def;
		return s.charAt(index);
	}

	/**
	 * Reverses a string.
	 */
	public static String reverse(String s) {
		if (s == null || s.isEmpty()) return s;
		return new StringBuilder(s).reverse().toString();
	}

	/**
	 * Optimized String.format.
	 */
	public static String format(String format, Object... objs) {
		if (format == null) return "";
		if (objs.length == 0) return format;
		return String.format(format, objs);
	}

	/**
	 * Appends formatted text to string builder.
	 */
	public static StringBuilder format(StringBuilder sb, String format, Object... objs) {
		if (objs.length == 0) return sb.append(format);
		try (Formatter f = new Formatter(sb)) {
			f.format(format, objs);
			return sb;
		}
	}

	/**
	 * Creates a decimal formatter with given number of decimal places.
	 */
	public static DecimalFormat decimalFormat(int decimalPlaces) {
		StringBuilder b = new StringBuilder("0");
		if (decimalPlaces > 0) repeat(b.append("."), "#", decimalPlaces);
		DecimalFormat format = new DecimalFormat(b.toString());
		format.setRoundingMode(RoundingMode.HALF_UP);
		return format;
	}

	/**
	 * Extracts the first substring with matching open and close brackets. Extraction includes
	 * bracket chars.
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
	 * Returns the object as a string, or null if the object is null.
	 */
	public static String string(Object obj) {
		return obj == null ? null : obj.toString();
	}

	/**
	 * Creates a string from code points.
	 */
	public static String toString(int... codePoints) {
		return StreamUtil.toString(IntStream.of(codePoints));
	}

	/**
	 * Creates a string from vararg chars.
	 */
	public static String toString(char... chars) {
		return String.valueOf(chars);
	}

	/**
	 * Escapes non-visible characters within the given string.
	 */
	public static String escape(String s) {
		return replaceUnprintable(s, StringUtil::escapeChar);
	}

	/**
	 * Escapes non-visible character.
	 */
	public static String escape(char c) {
		return isPrintable(c) ? String.valueOf(c) : escapeChar(c);
	}

	/**
	 * Escapes a non-printable char.
	 */
	static String escapeChar(char c) {
		return switch (c) {
			case BACKSLASH -> ESCAPED_BACKSLASH;
			case BS -> ESCAPED_BS;
			case ESC -> ESCAPED_ESC;
			case FF -> ESCAPED_FF;
			case NL -> ESCAPED_NL;
			case CR -> ESCAPED_CR;
			case TAB -> ESCAPED_TAB;
			case NULL -> ESCAPED_NULL;
			default -> ESCAPED_UTF16 + toHex(c, SHORT_HEX_DIGITS);
		};
	}

	/**
	 * Encodes escaped characters within the given string.
	 */
	public static String unEscape(String s) {
		return replaceAllQuoted(ESCAPE_REGEX, s, m -> String.valueOf(unEscapeChar(m.group())));
	}

	/**
	 * Encodes an escaped character string.
	 */
	static char unEscapeChar(String escapedChar) {
		if (escapedChar == null) return NULL;
		return switch (escapedChar) {
			case ESCAPED_BACKSLASH -> BACKSLASH;
			case ESCAPED_BS -> BS;
			case ESCAPED_ESC -> ESC;
			case ESCAPED_FF -> FF;
			case ESCAPED_NL -> NL;
			case ESCAPED_CR -> CR;
			case ESCAPED_TAB -> TAB;
			case ESCAPED_NULL -> NULL;
			default -> {
				Character c = escaped(escapedChar, ESCAPED_OCTAL, OCTAL_RADIX);
				if (c == null) c = escaped(escapedChar, ESCAPED_HEX, HEX_RADIX);
				if (c == null) c = escaped(escapedChar, ESCAPED_UTF16, HEX_RADIX);
				yield c == null ? NULL : c;
			}
		};
	}

	/**
	 * Returns char repeated n times.
	 */
	public static String repeat(char c, int n) {
		if (n == 0) return "";
		char[] cs = new char[n];
		Arrays.fill(cs, c);
		return new String(cs);
	}

	/**
	 * Returns string repeated n times.
	 */
	public static String repeat(String s, int n) {
		if (s == null) return null;
		return s.repeat(n);
	}

	/**
	 * Adds string to builder n times.
	 */
	public static StringBuilder repeat(StringBuilder b, char c, int n) {
		if (b != null) while (n-- > 0)
			b.append(c);
		return b;
	}

	/**
	 * Adds string to builder n times.
	 */
	public static StringBuilder repeat(StringBuilder b, CharSequence s, int n) {
		if (b != null && s != null && s.length() > 0) while (n-- > 0)
			b.append(s);
		return b;
	}

	/**
	 * Splits a string at given indexes.
	 */
	public static List<String> split(String s, Collection<Integer> indexes) {
		return split(s, ArrayUtil.ints(indexes));
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
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(double d, int precision) {
		return compact(MathUtil.simpleRound(precision, d));
	}

	/**
	 * Replace multiple whitespaces with a single space, then trim.
	 */
	public static String compact(String s) {
		if (s == null) return null;
		return WHITE_SPACE_REGEX.matcher(s).replaceAll(" ").trim();
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
	 * Convenience method to convert a byte to an 8-digit binary string. A separator is added each
	 * count digits, aligned to the right.
	 */
	public static String toBinary(byte b, String separator, int... counts) {
		return toBinary(b, BYTE_BINARY_DIGITS, separator, counts);
	}

	/**
	 * Convenience method to convert a short to a 16-digit binary string.
	 */
	public static String toBinary(short s) {
		return toBinary(s, SHORT_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert a byte to an 8-digit binary string. A separator is added each
	 * count digits, aligned to the right.
	 */
	public static String toBinary(short s, String separator, int... counts) {
		return toBinary(s, SHORT_BINARY_DIGITS, separator, counts);
	}

	/**
	 * Convenience method to convert an int to a 32-digit binary string.
	 */
	public static String toBinary(int i) {
		return toBinary(i, INT_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert a byte to an 8-digit binary string. A separator is added each
	 * count digits, aligned to the right.
	 */
	public static String toBinary(int i, String separator, int... counts) {
		return toBinary(i, INT_BINARY_DIGITS, separator, counts);
	}

	/**
	 * Convenience method to convert a long to a 64-digit binary string.
	 */
	public static String toBinary(long l) {
		return toBinary(l, LONG_BINARY_DIGITS);
	}

	/**
	 * Convenience method to convert a byte to an 8-digit binary string. A separator is added each
	 * count digits, aligned to the right.
	 */
	public static String toBinary(long l, String separator, int... counts) {
		return toBinary(l, LONG_BINARY_DIGITS, separator, counts);
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
	 * numbers larger than the digits specified, the most significant digits are dropped. A
	 * separator is added each count digits, aligned to the right.
	 */
	public static String toBinary(long l, int digits, String separator, int... counts) {
		return separate(toUnsigned(l, BINARY_RADIX, digits), separator, Align.H.right, counts);
	}

	/**
	 * Converts a number to radix-based string with exactly the numbers of specified digits. For
	 * numbers larger than the digits specified, the most significant digits are dropped.
	 */
	public static String toUnsigned(long l, int radix, int digits) {
		String s = Long.toUnsignedString(l, radix);
		s = pad(s, digits, "0", Align.H.right);
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
	public static List<String> split(CharSequence s, Pattern pattern) {
		if (s == null || s.length() == 0) return Collections.emptyList();
		return StreamUtil.toList(Stream.of(pattern.split(s)).map(String::trim));
	}

	/**
	 * Replace tabs with spaces, keeping column alignment.
	 */
	public static String spacesToTabs(String s, int tabSize) {
		if (tabSize <= 0 || s == null || s.isEmpty()) return s;
		s = tabsToSpaces(s, tabSize); // replace any tabs first
		int pos = 0;
		StringBuilder b = null;
		for (int i = tabSize; i <= s.length(); i += tabSize) {
			int j = i;
			while (j > i - tabSize && s.charAt(j - 1) == ' ')
				j--;
			if (j == i) continue;
			if (b == null) b = new StringBuilder();
			while (pos < j)
				b.append(s.charAt(pos++));
			b.append(TAB);
			pos = i;
		}
		if (b != null) while (pos < s.length())
			b.append(s.charAt(pos++));
		return b == null ? s : b.toString();
	}

	/**
	 * Replace tabs with spaces, keeping column alignment.
	 */
	public static String tabsToSpaces(String s, int tabSize) {
		if (tabSize <= 0 || s == null || s.isEmpty()) return s;
		int pos = 0;
		StringBuilder b = null;
		while (pos < s.length()) {
			int i = s.indexOf(TAB, pos);
			if (i < 0) break;
			if (b == null) b = new StringBuilder();
			while (pos < i)
				b.append(s.charAt(pos++));
			repeat(b, ' ', tabSpaces(b.length(), tabSize));
			pos++;
		}
		if (b != null) while (pos < s.length())
			b.append(s.charAt(pos++));
		return b == null ? s : b.toString();
	}

	/**
	 * Checks if a char is printable
	 */
	public static boolean isPrintable(char c) {
		if (Character.isISOControl(c) || c == KeyEvent.CHAR_UNDEFINED) return false;
		return Character.UnicodeBlock.of(c) != Character.UnicodeBlock.SPECIALS;
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
	 * Converts to lower case if not null.
	 */
	public static String toLowerCase(CharSequence s) {
		if (s == null) return null;
		return s.toString().toLowerCase();
	}

	/**
	 * Converts to upper case if not null.
	 */
	public static String toUpperCase(CharSequence s) {
		if (s == null) return null;
		return s.toString().toUpperCase();
	}

	/**
	 * Extends CharSequence subSequence functionality to match substring.
	 */
	public static CharSequence subSequence(CharSequence s, int start) {
		if (s == null) return null;
		return s.subSequence(start, s.length());
	}

	/**
	 * Functionality of String applied expanded to CharSequences.
	 */
	public static String substring(CharSequence s, int start) {
		if (s == null) return null;
		return substring(s, start, s.length());
	}

	/**
	 * Functionality of String applied expanded to CharSequences.
	 */
	public static String substring(CharSequence s, int start, int end) {
		if (s == null) return null;
		return s.subSequence(start, end).toString();
	}

	/**
	 * Functionality of String applied expanded to StringBuilder.
	 */
	public static boolean startsWith(StringBuilder b, String s) {
		return startsWith(b, 0, s);
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean startsWith(StringBuilder b, int offset, String s) {
		if (s == null) return false;
		return regionMatches(b, offset, s, 0, s.length());
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean startsWithIgnoreCase(String s0, String s1) {
		return startsWithIgnoreCase(s0, 0, s1);
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean startsWithIgnoreCase(String s0, int offset, String s1) {
		if (s0 == null || s1 == null) return false;
		return s0.regionMatches(true, offset, s1, 0, s1.length());
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean startsWithIgnoreCase(StringBuilder b, String s) {
		return startsWithIgnoreCase(b, 0, s);
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean startsWithIgnoreCase(StringBuilder b, int offset, String s) {
		if (s == null) return false;
		return regionMatches(b, true, offset, s, 0, s.length());
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean equalsIgnoreCase(StringBuilder b, String s) {
		if (b == null) return s == null;
		if (s == null) return false;
		return regionMatches(b, true, 0, s, 0, s.length());
	}

	/**
	 * Returns true if the string range is the full string.
	 */
	public static boolean full(String s, int start, int end) {
		return s != null && start == 0 && end == s.length();
	}

	/**
	 * Returns true if the string contains the sub-string at the given index.
	 */
	public static boolean matchAt(String s, int index, String sub) {
		if (s == null || sub == null) return false;
		return s.regionMatches(index, sub, 0, sub.length());
	}

	/**
	 * Returns true if the string contains the given string, ignoring case.
	 */
	public static boolean containsIgnoreCase(String s, String other) {
		if (s == null || other == null) return false;
		if (s == other) return true;
		return s.regionMatches(true, 0, other, 0, s.length());
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean regionMatches(StringBuilder b, int offset, String s, int sOffset,
		int len) {
		return regionMatches(b, false, offset, s, sOffset, len);
	}

	/**
	 * Functionality of String applied to StringBuilder.
	 */
	public static boolean regionMatches(StringBuilder b, boolean ignoreCase, int offset, String s,
		int sOffset, int len) {
		if (b == null || s == null) return false;
		if (!ArrayUtil.isValidSlice(b.length(), offset, len)) return false;
		if (!ArrayUtil.isValidSlice(s.length(), sOffset, len)) return false;
		return b.substring(offset, offset + len).regionMatches(ignoreCase, 0, s, sOffset, len);
	}

	/**
	 * Pads a number with leading zeros.
	 */
	public static String pad(long value, int minLength) {
		if (value == Long.MIN_VALUE) {
			String str = String.valueOf(value).substring(1);
			return "-" + pad(str, minLength - 1, "0", Align.H.right);
		}
		if (value < 0) return "-" + pad(-value, minLength - 1);
		return pad(String.valueOf(value), minLength, "0", Align.H.right);
	}

	/**
	 * Pads a string with leading spaces.
	 */
	public static String pad(CharSequence str, int minLength) {
		return pad(str, minLength, " ", Align.H.right);
	}

	/**
	 * Pads a string with leading or trailing characters.
	 */
	public static String pad(CharSequence str, int minLength, Align.H align) {
		return pad(str, minLength, " ", align);
	}

	/**
	 * Pads a string with leading or trailing characters.
	 */
	public static String pad(CharSequence str, int minLength, CharSequence pad, Align.H align) {
		if (str == null) str = "";
		if (pad == null) pad = "";
		if (str.length() >= minLength) return str.toString();
		int pads = pads(pad.length(), minLength - str.length());
		int left = leftCount(pads, align);
		if (pads == 0) return str.toString();
		StringBuilder b = new StringBuilder();
		repeat(b, pad, Math.max(0, left)).append(str);
		repeat(b, pad, Math.max(0, pads - left));
		return b.toString();
	}

	/**
	 * Separate a string into sections, aligned to left or right. Multiple counts can be used for
	 * variable separation widths.
	 */
	public static String separate(CharSequence str, CharSequence separator, Align.H align,
		int... counts) {
		int len = str.length();
		if (len == 0 || counts.length == 0 || separator.length() == 0) return str.toString();
		if (align == null) align = Align.H.left;
		if (align == Align.H.center)
			throw new IllegalArgumentException("Alignment not supported: " + align);
		int[] sections = sections(len, align, counts);
		StringBuilder b = new StringBuilder();
		for (int i = 1; i < sections.length; i++) {
			b.append(str.subSequence(sections[i - 1], sections[i]));
			if (sections[i] < len) b.append(separator);
		}
		return b.toString();
	}

	/**
	 * Clears the StringBuilder.
	 */
	public static StringBuilder clear(StringBuilder b) {
		b.setLength(0);
		return b;
	}

	/**
	 * Gets the current string then clears the StringBuilder.
	 */
	public static String flush(StringBuilder b) {
		String s = b.toString();
		clear(b);
		return s;
	}

	/**
	 * Returns the length, or 0 if null.
	 */
	public static int len(CharSequence str) {
		return str == null ? 0 : str.length();
	}

	/**
	 * Returns the minimum length of the strings, 0 if none, and 0 for any null strings.
	 */
	public static int minLen(CharSequence... strings) {
		return minLen(Arrays.asList(strings));
	}

	/**
	 * Returns the minimum length of the strings, 0 if none, and 0 for any null strings.
	 */
	public static int minLen(Iterable<? extends CharSequence> strings) {
		return len(strings, (l, m) -> l < m);
	}

	/**
	 * Returns the maximum length of the strings, 0 if none, and 0 for any null strings.
	 */
	public static int maxLen(CharSequence... strings) {
		return maxLen(Arrays.asList(strings));
	}

	/**
	 * Returns the maximum length of the strings, 0 if none, and 0 for any null strings.
	 */
	public static int maxLen(Iterable<? extends CharSequence> strings) {
		return len(strings, (l, m) -> l > m);
	}

	/**
	 * Checks if the given string is null or empty. Can be used as a predicate.
	 */
	public static boolean empty(CharSequence str) {
		return str == null || str.isEmpty();
	}

	/**
	 * Checks if the given string is non-null and not empty. Can be used as a predicate.
	 */
	public static boolean nonEmpty(CharSequence str) {
		return !empty(str);
	}

	/**
	 * Checks if the given string is null or empty or contains only whitespace. Can be used as a
	 * predicate.
	 */
	public static boolean blank(String str) {
		return str == null || str.isBlank();
	}

	/**
	 * Checks if the given string is non-null or not empty and does not only contain whitespace. Can
	 * be used as a predicate.
	 */
	public static boolean nonBlank(String str) {
		return !blank(str);
	}

	/**
	 * Trims a string if not null.
	 */
	public static String trim(String s) {
		return s == null ? null : s.trim();
	}

	/**
	 * Returns substring, or "" if null or index out of bounds. Use start < 0 for length relative to
	 * the end.
	 */
	public static String safeSubstring(CharSequence s, int start) {
		return safeSubstring(s, start, -1);
	}

	/**
	 * Returns substring, or "" if null or index out of bounds. Use end = -1 for no end limit. Use
	 * start < 0 for length relative to end.
	 */
	public static String safeSubstring(CharSequence s, int start, int end) {
		if (s == null || start >= s.length()) return "";
		if (end > s.length() || end == -1) end = s.length();
		if (start < 0) start = Math.max(0, end + start);
		return substring(s, start, end);
	}

	/**
	 * Captures the output of a print stream consumer.
	 */
	public static <E extends Exception> String print(Excepts.Consumer<E, PrintStream> consumer)
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
	 * Splits a string into lines without trimming.
	 */
	public static List<String> lines(CharSequence s) {
		if (s.length() == 0) return List.of();
		return Arrays.asList(NEWLINE_REGEX.split(s));
	}

	/**
	 * Prefixes each line of the string with given prefix.
	 */
	public static String prefixLines(CharSequence prefix, CharSequence s) {
		if (prefix.length() == 0) return s.toString();
		return prefix + NEWLINE_REGEX.matcher(s) //
			.replaceAll("$1" + Matcher.quoteReplacement(prefix.toString()));
	}

	/**
	 * Finds the length of the common prefix between the given strings.
	 */
	public static int commonPrefixLen(CharSequence... strings) {
		return commonPrefixLen(Arrays.asList(strings));
	}

	/**
	 * Finds the length of the common prefix between the given strings.
	 */
	public static int commonPrefixLen(Collection<? extends CharSequence> strings) {
		int min = minLen(strings);
		for (int i = 0; i < min; i++) {
			int c = -1;
			for (var s : strings)
				if (c == -1) c = s.charAt(i);
				else if (c != s.charAt(i)) return i;
		}
		return min;
	}

	/**
	 * Returns the common prefix between the given strings.
	 */
	public static CharSequence commonPrefix(CharSequence... strings) {
		return commonPrefix(Arrays.asList(strings));
	}

	/**
	 * Returns the common prefix between the given strings.
	 */
	public static CharSequence commonPrefix(Collection<? extends CharSequence> strings) {
		int len = commonPrefixLen(strings);
		if (len == 0) return "";
		return strings.iterator().next().subSequence(0, len);
	}

	/**
	 * Returns true if the index marks a change to upper-case, or between letter and symbol.
	 */
	public static boolean nameBoundary(CharSequence s, int i) {
		if (s == null) return false;
		if (i <= 0 || i >= s.length()) return true;
		char l = s.charAt(i - 1);
		char r = s.charAt(i);
		if (Character.isLetter(l) != Character.isLetter(r)) return true;
		if (Character.isDigit(l) != Character.isDigit(r)) return true;
		if (Character.isLowerCase(l) && Character.isUpperCase(r)) return true;
		return false;
	}

	// support methods

	private static Character escaped(String escapedChar, String prefix, int radix) {
		if (!escapedChar.startsWith(prefix)) return null;
		return (char) Integer.parseInt(escapedChar.substring(prefix.length()), radix);
	}

	private static int pads(int padLen, int len) {
		return padLen > 0 ? len / padLen : 0;
	}

	private static int leftCount(int count, Align.H align) {
		if (align == Align.H.right) return count;
		if (align == Align.H.center) return count / 2;
		return 0;
	}

	private static int tabSpaces(int pos, int tabSize) {
		return ((pos + tabSize) / tabSize) * tabSize - pos;
	}

	private static int[] sections(int len, Align.H align, int... counts) {
		List<Integer> list = new ArrayList<>();
		list.add(0);
		for (int pos = 0, i = 0; pos < len;) {
			int count = counts[Math.min(counts.length - 1, i++)];
			pos = count <= 0 ? len : Math.min(len, pos + count);
			list.add(pos);
		}
		int[] sections = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			sections[i] =
				align == Align.H.left ? list.get(i) : len - list.get(sections.length - i - 1);
		return sections;
	}

	private static int len(Iterable<? extends CharSequence> strings, IntBiPredicate predicate) {
		if (strings == null) return 0;
		int value = -1;
		for (var s : strings) {
			int len = len(s);
			if (value == -1 || predicate.test(len, value)) value = len;
		}
		return Math.max(value, 0);
	}
}