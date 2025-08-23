package ceri.common.text;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Predicates;
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
		public static final Pattern line = Pattern.compile("(\\r\\n|\\n|\\r)");
		public static final Pattern comma = Pattern.compile("\\s*,\\s*");
		public static final Pattern space = Pattern.compile("\\s+");

		private Regex() {}
	}

	/**
	 * String-based filters.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Applies comparator to non-null string representation.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, T>
			of(Excepts.Predicate<E, ? super String> predicate) {
			if (predicate == null) return Predicates.yes();
			return t -> t != null && predicate.test(String.valueOf(t));
		}

		/**
		 * Returns true if the non-null arg string equals the string, with optional case match.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> eq(boolean matchCase,
			String s) {
			if (s == null) return Predicates.isNull();
			return of(t -> Strings.equals(matchCase, t, s));
		}

		/**
		 * Returns true for strings that contain the given substring.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> contains(String s) {
			return contains(true, s);
		}

		/**
		 * Returns true for strings that contain the given substring, with optional case match.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String>
			contains(boolean matchCase, String s) {
			if (s == null) return Predicates.isNull();
			if (s.isEmpty()) return Predicates.yes();
			return t -> t != null && Strings.contains(matchCase, t, s);
		}
	}

	/**
	 * Splits and trims strings.
	 */
	public static class Split {
		/**
		 * Split the string into an array of trimmed values.
		 */
		public static String[] array(Pattern pattern, CharSequence s) {
			var split = Patterns.split(pattern, s);
			for (int i = 0; i < split.length; i++)
				split[i] = Strings.trim(split[i]);
			return split;
		}

		/**
		 * Split the string into a list of trimmed values.
		 */
		public static List<String> list(Pattern pattern, CharSequence s) {
			return stream(pattern, s).toList();
		}

		/**
		 * Split the string into a stream of trimmed values.
		 */
		public static Stream<RuntimeException, String> stream(Pattern pattern, CharSequence s) {
			return Patterns.splitStream(pattern, s).map(Strings::trim);
		}
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
	 * Trims the string; returns empty string if null.
	 */
	public static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	/**
	 * Converts to lower case.
	 */
	public static String lower(String s) {
		return s == null ? "" : s.toLowerCase();
	}

	/**
	 * Converts to lower case.
	 */
	public static String upper(String s) {
		return s == null ? "" : s.toUpperCase();
	}

	/**
	 * Returns the formatted string, or unformatted if no args.
	 */
	public static String format(String format, Object... objs) {
		if (format == null) return "";
		if (objs == null || objs.length == 0) return format;
		return String.format(format, objs);
	}

	/**
	 * Returns true if the string contains the given string, ignoring case.
	 */
	public static boolean equals(boolean matchCase, String s, String other) {
		if (s == other) return true;
		if (s == null || other == null) return false;
		return matchCase ? s.equals(other) : s.equalsIgnoreCase(other);
	}

	/**
	 * Returns true if the string contains the given string.
	 */
	public static boolean contains(String s, String other) {
		return contains(true, s, other);
	}

	/**
	 * Returns true if the string contains the given string, optionally ignoring case.
	 */
	public static boolean contains(boolean matchCase, String s, String other) {
		if (s == null || other == null) return false;
		if (s == other) return true;
		return s.regionMatches(!matchCase, 0, other, 0, s.length());
	}

	/**
	 * Returns true if the index marks a change to upper-case, or between letter and symbol.
	 */
	public static boolean isNameBoundary(CharSequence s, int i) {
		if (s == null) return false;
		if (i <= 0 || i >= s.length()) return true;
		return Chars.isNameBoundary(s.charAt(i - 1), s.charAt(i));
	}
}
