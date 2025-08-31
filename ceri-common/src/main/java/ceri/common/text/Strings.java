package ceri.common.text;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
import ceri.common.math.MathUtil;
import ceri.common.stream.IntStream;
import ceri.common.stream.Streams;
import ceri.common.util.BasicUtil;

/**
 * String type support.
 */
public class Strings {
	public static final String NULL = "null";
	private static final String INTEGRAL_FLOAT = ".0";
	private static final char UNPRINTABLE_CHAR = '.';
	public static final String EOL = System.lineSeparator();

	private Strings() {}

	/**
	 * String-based filters.
	 */
	public static class Filter {
		public static final Functions.Predicate<String> nonEmpty = Strings::nonEmpty;
		public static final Functions.Predicate<String> nonBlank = Strings::nonBlank;

		private Filter() {}

		/**
		 * Returns true if not null and not empty.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> nonEmpty() {
			return BasicUtil.unchecked(nonEmpty);
		}

		/**
		 * Returns true if not null and has a non-whitespace code point.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> nonBlank() {
			return BasicUtil.unchecked(nonBlank);
		}

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
		public static <E extends Exception> Excepts.Predicate<E, String> contains(boolean matchCase,
			String s) {
			if (s == null) return Predicates.isNull();
			if (s.isEmpty()) return Predicates.yes();
			return t -> t != null && Strings.contains(matchCase, t, s);
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
	 * Returns the formatted string, or unformatted if no args.
	 */
	public static String format(String format, Object... objs) {
		if (format == null) return "";
		if (ArrayUtil.isEmpty(objs)) return format;
		return String.format(format, objs);
	}

	/**
	 * Creates a decimal formatter with given number of decimal places.
	 */
	public static DecimalFormat decimalFormat(int decimalPlaces) {
		var b = new StringBuilder("0");
		if (decimalPlaces > 0) StringBuilders.repeat(b.append("."), "#", decimalPlaces);
		var format = new DecimalFormat(b.toString());
		format.setRoundingMode(RoundingMode.HALF_UP);
		return format;
	}

	/**
	 * Returns char repeated n times.
	 */
	public static String repeat(char c, int n) {
		if (n <= 0) return "";
		char[] cs = new char[n];
		Arrays.fill(cs, c);
		return new String(cs);
	}

	/**
	 * Returns string repeated n times.
	 */
	public static String repeat(String s, int n) {
		if (isEmpty(s)) return "";
		return s.repeat(n);
	}

	/**
	 * Returns true if the char sequence is null or empty.
	 */
	public static boolean isEmpty(CharSequence s) {
		return s == null || s.isEmpty();
	}

	/**
	 * Returns true if the char sequence is non-null and not empty.
	 */
	public static boolean nonEmpty(CharSequence s) {
		return !isEmpty(s);
	}

	/**
	 * Returns true if the string is null, empty, or contains only whitespace codepoints.
	 */
	public static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	/**
	 * Returns true if the string is non-null, and has a non-whitespace codepoint.
	 */
	public static boolean nonBlank(String s) {
		return !isBlank(s);
	}

	/**
	 * Returns the length, or 0 if null.
	 */
	public static int length(CharSequence s) {
		return s == null ? 0 : s.length();
	}

	/**
	 * Returns true if the string contains the given string, optionally ignoring case.
	 */
	public static boolean equals(boolean matchCase, String s, String other) {
		if (s == other) return true;
		if (s == null || other == null) return false;
		return matchCase ? s.equals(other) : s.equalsIgnoreCase(other);
	}

	/**
	 * Returns true if the sanitized char ranges match, optionally ignoring case.
	 */
	public static boolean equals(boolean matchCase, String s, int offset, String other) {
		return equals(matchCase, s, offset, other, 0, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the sanitized char ranges match, optionally ignoring case.
	 */
	public static boolean equals(boolean matchCase, String s, int offset, String other, int ooffset,
		int length) {
		if (s == null || other == null) return s == other;
		offset = MathUtil.limit(offset, 0, length(s));
		ooffset = MathUtil.limit(ooffset, 0, length(other));
		length = MathUtil.limit(length, 0, Math.max(length(s) - offset, length(other) - ooffset));
		if (length == 0) return true;
		return s.regionMatches(!matchCase, offset, other, ooffset, length);
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
	 * Reverses a string.
	 */
	public static String reverse(String s) {
		if (isEmpty(s)) return "";
		return new StringBuilder(s).reverse().toString();
	}

	/**
	 * Replace multiple whitespaces with a single space, then trim.
	 */
	public static String compact(String s) {
		if (s == null) return "";
		return Patterns.findAllAccept(Patterns.Split.SPACE.pattern, s, (b, _) -> b.append(' '))
			.trim();
	}

	/**
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(float f) {
		return compactFp(Float.toString(f));
	}

	/**
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(double d) {
		return compactFp(Double.toString(d));
	}

	/**
	 * Compact floating point representation - trailing .0 is removed if present.
	 */
	public static String compact(double d, int precision) {
		return compact(MathUtil.simpleRound(precision, d));
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(String s, int offset) {
		return sub(s, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(String s, int offset, int length) {
		if (isEmpty(s)) return "";
		return ArrayUtil.applySlice(s.length(), offset, length, (o, l) -> s.substring(o, o + l));
	}

	/**
	 * Captures the output of a print stream consumer.
	 */
	public static <E extends Exception> String printed(Excepts.Consumer<E, PrintStream> consumer)
		throws E {
		return printed(consumer, null);
	}

	/**
	 * Captures the output of a print stream consumer.
	 */
	public static <E extends Exception> String printed(Excepts.Consumer<E, PrintStream> consumer,
		Charset charset) throws E {
		var b = new StringBuilder();
		try (var out = StringBuilders.printStream(b, charset)) {
			consumer.accept(out);
		}
		return b.toString();
	}

	// support

	private static String compactFp(String s) {
		if (!s.endsWith(INTEGRAL_FLOAT)) return s;
		return s.substring(0, s.length() - INTEGRAL_FLOAT.length());
	}
}
