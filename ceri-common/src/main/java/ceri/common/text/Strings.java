package ceri.common.text;

import java.util.Formatter;
import java.util.PrimitiveIterator;
import java.util.regex.Pattern;
import ceri.common.stream.IntStream;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;

/**
 * String type support.
 */
public class Strings {
	public static final String NULL = "null";
	private static final String INTEGRAL_FLOAT = ".0";
	private static final char UNPRINTABLE_CHAR = '.';
	public static final char BACKSLASH = '\\';
	public static final char NUL = '\0';
	public static final char BS = '\b';
	public static final char TAB = '\t';
	public static final char NL = '\n';
	public static final char FF = '\f';
	public static final char CR = '\r';
	public static final char ESC = '\u001b';
	public static final char DEL = '\u007f';
	public static final String EOL = System.lineSeparator();

	private Strings() {}

	private static class Escape {
		private static final Pattern REGEX = 
			Pattern.compile("\\\\\\\\|\\\\b|\\\\e|\\\\t|\\\\f|\\\\r|\\\\n|\\\\0[0-3][0-7]{2}"
				+ "|\\\\0[0-7]{2}|\\\\0[0-7]|\\\\0|\\\\x[0-9a-fA-F]{2}|\\\\u[0-9a-fA-F]{4}");
		private static final String NUL = "\\0";
		private static final String BACKSLASH = "\\\\";
		private static final String BS = "\\b";
		private static final String ESC = "\\e";
		private static final String TAB = "\\t";
		private static final String FF = "\\f";
		private static final String CR = "\\r";
		private static final String NL = "\\n";
		private static final String OCTAL = "\\0";
		private static final String HEX = "\\x";
		private static final String UTF16 = "\\u";

		private Escape() {}
	}

	public static class Regex {
		public static final Pattern NEWLINE = Pattern.compile("(\\r\\n|\\n|\\r)");

		private Regex() {}
		
	}

	/**
	 * Creates a string from vararg chars.
	 */
	public static String of(char... chars) {
		return String.valueOf(chars);
	}

	/**
	 * Creates a string from code points.
	 */
	public static String of(int... codePoints) {
		return of(Streams.ints(codePoints));
	}

	/**
	 * Creates a string from code points.
	 */
	public static <E extends Exception> String of(IntStream<E> codePoints) throws E {
		return StringBuilders.append(new StringBuilder(), codePoints).toString();
	}

	/**
	 * Creates a string from code points.
	 */
	public static String of(PrimitiveIterator.OfInt codePoints) {
		return StringBuilders.append(new StringBuilder(), codePoints).toString();
	}

	/**
	 * Checks if the given string is null or empty. Can be used as a predicate.
	 */
	public static boolean isEmpty(CharSequence s) {
		return s == null || s.isEmpty();
	}

	/**
	 * Checks if the given string is non-null and not empty. Can be used as a predicate.
	 */
	public static boolean nonEmpty(CharSequence s) {
		return !isEmpty(s);
	}

	/**
	 * Returns the length, or 0 if null.
	 */
	public static int length(CharSequence s) {
		return s == null ? 0 : s.length();
	}

	/**
	 * Formats the string; returns unformatted format if no objects are given.
	 */
	public static String format(String format, Object... objs) {
		if (format == null) return "";
		if (objs == null || objs.length == 0) return format;
		return String.format(format, objs);
	}

	/**
	 * Appends formatted text to string builder; appends unformatted format if no objects are given.
	 */
	public static StringBuilder format(StringBuilder sb, String format, Object... objs) {
		if (format == null) return sb;
		if (objs == null || objs.length == 0) return sb.append(format);
		try (var f = new Formatter(sb)) {
			f.format(format, objs);
			return sb;
		}
	}

	/**
	 * Returns true if the index marks a change to upper-case, or between letter and symbol.
	 */
	public static boolean isNameBoundary(CharSequence s, int i) {
		if (s == null) return false;
		if (i <= 0 || i >= s.length()) return true;
		char l = s.charAt(i - 1);
		char r = s.charAt(i);
		if (Character.isLetter(l) != Character.isLetter(r)) return true;
		if (Character.isDigit(l) != Character.isDigit(r)) return true;
		if (Character.isLowerCase(l) && Character.isUpperCase(r)) return true;
		return false;
	}

	/**
	 * Splits a string into lines without trimming.
	 */
	public static Stream<RuntimeException, String> lines(CharSequence s) {
		return Patterns.splitStream(Regex.NEWLINE, s);
	}

	/**
	 * Splits a string into lines without trimming.
	 */
	public static String[] lineArray(CharSequence s) {
		return Patterns.split(Regex.NEWLINE, s);
	}

}
