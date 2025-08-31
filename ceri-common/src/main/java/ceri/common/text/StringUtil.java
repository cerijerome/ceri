package ceri.common.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.util.Align;

/**
 * String-based utilities. See also TextUtil for more word-based formatting utilities.
 */
public class StringUtil {
	private static final char UNPRINTABLE_CHAR = '.';
	public static final Pattern NEWLINE_REGEX = Pattern.compile("(\\r\\n|\\n|\\r)");
	public static final Pattern COMMA_SPLIT_REGEX = Pattern.compile("\\s*,\\s*");
	public static final Pattern WHITE_SPACE_REGEX = Pattern.compile("\\s+");

	private StringUtil() {}

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
			b.append(Chars.isPrintable(c) ? c : replace);
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
			if (Chars.isPrintable(c)) b.append(c);
			else b.append(replacer.apply(c));
		}
		return b.toString();
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
		var b = new StringBuilder();
		StringBuilders.repeat(b, pad, Math.max(0, left)).append(str);
		StringBuilders.repeat(b, pad, Math.max(0, pads - left));
		return b.toString();
	}

	// support methods

	private static int pads(int padLen, int len) {
		return padLen > 0 ? len / padLen : 0;
	}

	private static int leftCount(int count, Align.H align) {
		if (align == Align.H.right) return count;
		if (align == Align.H.center) return count / 2;
		return 0;
	}
}