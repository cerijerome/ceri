package ceri.common.util;

import java.awt.event.KeyEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String-based utilities. See also TextUtil for more word-based formatting utilities.
 */
public class StringUtil {
	public static final String UTF8 = "UTF8";
	public static final Pattern NEWLINE_REGEX = Pattern.compile("(\\r\\n|\\n|\\r)");
	public static final Pattern COMMA_SPLIT_REGEX = Pattern.compile("\\s*,\\s*");

	private StringUtil() {}

	/**
	 * Uses URLEncoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Uses URLDecoder with UTF8 encoding. Throws IllegalArgumentException for encoding issues.
	 */
	public static String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Splits a string by commas and trims each entry. Trailing empty strings are dropped as with
	 * the regular split method.
	 */
	public static List<String> commaSplit(String s) {
		if (BasicUtil.isEmpty(s)) return Collections.emptyList();
		String[] ss = COMMA_SPLIT_REGEX.split(s);
		List<String> list = new ArrayList<>();
		for (String str : ss)
			list.add(str.trim());
		return list;
	}

	/**
	 * Checks if a char is printable
	 */
	public static boolean printable(char c) {
		if (Character.isISOControl(c) || c == KeyEvent.CHAR_UNDEFINED) return false;
		Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
		return block != null && block != Character.UnicodeBlock.SPECIALS;
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

	/**
	 * Pads a number with leading zeros.
	 */
	public static String pad(long value, int minLength) {
		if (value == Long.MIN_VALUE) {
			String str = String.valueOf(value).substring(1);
			return "-" + pad(str, minLength - 1, "0", Align.RIGHT);
		}
		if (value < 0) return "-" + pad(-value, minLength - 1);
		return pad(String.valueOf(value), minLength, "0", Align.RIGHT);
	}

	/**
	 * Pads a string with leading spaces.
	 */
	public static String pad(String str, int minLength) {
		return pad(str, minLength, " ", Align.RIGHT);
	}

	/**
	 * Alignment for padding.
	 */
	public static enum Align {
		LEFT,
		RIGHT
	}

	/**
	 * Pads a string with leading or trailing characters.
	 */
	public static String pad(String str, int minLength, String pad, Align align) {
		if (str == null) str = "";
		if (pad == null) pad = "";
		int len = str.length();
		if (len >= minLength) return str;

		int padLen = pad.length();
		int padCount = padLen > 0 ? (minLength - len) / padLen : 0;
		if (padCount == 0) return str;

		StringBuilder b = new StringBuilder(minLength);
		if (align == Align.LEFT) {
			b.append(str);
			for (int i = 0; i < padCount; i++)
				b.append(pad);
		} else {
			for (int i = 0; i < padCount; i++)
				b.append(pad);
			b.append(str);
		}
		return b.toString();
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
	 * Wrap a PrintStream around a string builder. PrintStream will not flush automatically.
	 */
	public static PrintStream asPrintStream(final StringBuilder s) {
		return new PrintStream(new OutputStream() {
			@Override
			public void write(int b) {
				s.append((char) b);
			}

			@Override
			public void write(byte[] b) {
				s.append(new String(b));
			}

			@Override
			public void write(byte[] b, int off, int len) {
				s.append(new String(b, off, len));
			}
		});
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
