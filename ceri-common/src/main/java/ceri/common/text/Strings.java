package ceri.common.text;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.PrimitiveIterator;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.stream.IntStream;
import ceri.common.stream.Streams;
import ceri.common.util.Hasher;

/**
 * String type support.
 */
public class Strings {
	public static final String NULL = "null";
	private static final String INTEGRAL_FLOAT = ".0";
	public static final String EOL = System.lineSeparator();

	private Strings() {}

	/**
	 * String-based filters.
	 */
	public static class Filter {
		public static final Functions.Predicate<CharSequence> NON_EMPTY = Strings::nonEmpty;
		public static final Functions.Predicate<String> NON_BLANK = Strings::nonBlank;

		private Filter() {}

		/**
		 * Returns true if not null and not empty.
		 */
		public static <E extends Exception> Excepts.Predicate<E, CharSequence> nonEmpty() {
			return Reflect.unchecked(NON_EMPTY);
		}

		/**
		 * Returns true if not null and has a non-whitespace code point.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> nonBlank() {
			return Reflect.unchecked(NON_BLANK);
		}

		/**
		 * Applies comparator to non-null string representation.
		 */
		public static <E extends Exception, T> Excepts.Predicate<E, T>
			of(Excepts.Predicate<E, ? super String> predicate) {
			if (predicate == null) return Filters.yes();
			return t -> t != null && predicate.test(String.valueOf(t));
		}

		/**
		 * Returns true if the non-null arg string equals the string, with optional case match.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> eq(boolean matchCase,
			String s) {
			if (s == null) return Filters.isNull();
			return of(t -> Strings.equals(matchCase, t, s));
		}

		/**
		 * Returns true for strings that contain the given substring.
		 */
		public static <E extends Exception> Excepts.Predicate<E, CharSequence>
			contains(CharSequence s) {
			if (s == null) return Filters.isNull();
			return t -> t != null && Strings.contains(t, s);
		}

		/**
		 * Returns true for strings that contain the given substring, with optional case match.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> contains(boolean matchCase,
			String s) {
			if (s == null) return Filters.isNull();
			return t -> t != null && Strings.contains(matchCase, t, s);
		}
	}

	/**
	 * Returns object string, or empty string if null.
	 */
	public static String safe(Object obj) {
		return obj == null ? "" : obj.toString();
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
	 * Trims the string; returns empty string if null.
	 */
	public static String trim(CharSequence s) {
		if (isEmpty(s)) return "";
		int start = 0, end = s.length();
		while (start < end && s.charAt(start) <= ' ')
			start++;
		while (end > start && s.charAt(end - 1) <= ' ')
			end--;
		if (start == end) return "";
		if (start == 0 && end == s.length()) return s.toString();
		return s.subSequence(start, end).toString();
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
	 * Returns char repeated n times.
	 */
	public static String repeat(char c, int n) {
		if (n <= 0) return "";
		char[] cs = new char[n];
		Arrays.fill(cs, c);
		return new String(cs);
	}

	/**
	 * Returns char sequence repeated n times.
	 */
	public static String repeat(CharSequence s, int n) {
		if (isEmpty(s) || n <= 0) return "";
		if (n == 1) return s.toString();
		return StringBuilders.repeat(new StringBuilder(s.length() * n), s, n).toString();
	}

	/**
	 * Reverses a string.
	 */
	public static String reverse(CharSequence s) {
		if (isEmpty(s)) return "";
		return new StringBuilder(s).reverse().toString();
	}

	/**
	 * Replace consecutive whitespace with a single space, then trim.
	 */
	public static String compact(CharSequence s) {
		if (s == null) return "";
		return Regex.appendAll(Regex.UNICODE_SPACE, s, (b, _) -> b.append(' ')).trim();
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
		return compact(Maths.simpleRound(precision, d));
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(CharSequence s, int offset) {
		return sub(s, offset, Integer.MAX_VALUE);
	}

	/**
	 * Returns a bounded substring.
	 */
	public static String sub(CharSequence s, int offset, int length) {
		if (isEmpty(s)) return "";
		return ArrayUtil.applySlice(s.length(), offset, length,
			(o, l) -> l == 0 ? "" : s.subSequence(o, o + l).toString());
	}

	/**
	 * Pads a string with leading or trailing char sequences. A negative length is left-justified, a
	 * positive length is right-justified.
	 */
	public static String pad(CharSequence s, int length, CharSequence pad) {
		return pad(s, Math.abs(length), pad, length < 0 ? 0.0 : 1.0);
	}

	/**
	 * Pads a string with leading and/or trailing char sequences; the ratio determines how many pads
	 * are before or after the char sequence.
	 */
	public static String pad(CharSequence s, int length, CharSequence pad, double ratio) {
		s = Chars.safe(s);
		if (isEmpty(pad)) return s.toString();
		int pads = (length - s.length()) / pad.length();
		if (pads <= 0) return s.toString();
		int left = (int) Math.round(Maths.limit(ratio, 0.0, 1.0) * pads);
		var b = StringBuilders.repeat(new StringBuilder(), pad, left).append(s);
		return StringBuilders.repeat(b, pad, pads - left).toString();
	}

	/**
	 * Returns the hash code of the char sequence.
	 */
	public static int hash(CharSequence s) {
		if (length(s) == 0) return 0;
		var hasher = Hasher.of();
		for (int i = 0; i < s.length(); i++)
			hasher.hash(s.charAt(i));
		return hasher.code();
	}

	/**
	 * Returns true if the char sequences are equal.
	 */
	public static boolean equals(CharSequence s, CharSequence other) {
		return equals(s, 0, other, 0);
	}

	/**
	 * Returns true if the bounded char ranges are equal.
	 */
	public static boolean equals(CharSequence s, int soffset, CharSequence other, int ooffset) {
		return equals(s, soffset, other, ooffset, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the bounded char ranges are equal.
	 */
	public static boolean equals(CharSequence s, int soffset, CharSequence other, int ooffset,
		int length) {
		if (s == null || other == null) return s == other;
		return ArrayUtil.applyBiSlice(length(s), soffset, length, length(other), ooffset, length,
			(so, sl, oo, ol) -> sl == ol && regionMatches(s, so, other, oo, sl));
	}

	/**
	 * Returns true if the strings are equal, with optional case matching.
	 */
	public static boolean equals(boolean matchCase, String s, String other) {
		if (s == other) return true;
		if (s == null || other == null) return false;
		return matchCase ? s.equals(other) : s.equalsIgnoreCase(other);
	}

	/**
	 * Returns true if the bounded strings are equal, with optional case matching.
	 */
	public static boolean equals(boolean matchCase, String s, int offset, String other,
		int ooffset) {
		return equals(matchCase, s, offset, other, ooffset, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the bounded strings are equal, with optional case matching.
	 */
	public static boolean equals(boolean matchCase, String s, int soffset, String other,
		int ooffset, int length) {
		if (s == null || other == null) return s == other;
		return ArrayUtil.applyBiSlice(length(s), soffset, length, length(other), ooffset, length,
			(so, sl, oo, ol) -> sl == ol && s.regionMatches(!matchCase, so, other, oo, sl));
	}

	/**
	 * Returns true if the bounded char sequences are equal at the offset.
	 */
	public static boolean equalsAt(CharSequence s, int soffset, CharSequence other) {
		return equalsAt(s, soffset, other, 0);
	}

	/**
	 * Returns true if the bounded char sequences are equal at the offset.
	 */
	public static boolean equalsAt(CharSequence s, int soffset, CharSequence other, int ooffset) {
		if (s == null || other == null) return s == other;
		return ArrayUtil.applyBiSlice(length(s), soffset, 0, length(other), ooffset,
			Integer.MAX_VALUE, (so, _, oo, ol) -> regionMatches(s, so, other, oo, ol));
	}

	/**
	 * Returns true if the bounded strings are equal at the offset, with optional case matching.
	 */
	public static boolean equalsAt(boolean matchCase, String s, int soffset, String other) {
		return equalsAt(matchCase, s, soffset, other, 0);
	}

	/**
	 * Returns true if the bounded strings are equal at the offset, with optional case matching.
	 */
	public static boolean equalsAt(boolean matchCase, String s, int soffset, String other,
		int ooffset) {
		if (s == null || other == null) return s == other;
		return ArrayUtil.applyBiSlice(length(s), soffset, 0, length(other), ooffset,
			Integer.MAX_VALUE, (so, _, oo, ol) -> s.regionMatches(!matchCase, so, other, oo, ol));
	}

	/**
	 * Returns true if the char sequence starts with the given char sequence.
	 */
	public static boolean startsWith(CharSequence s, CharSequence other) {
		return equalsAt(s, 0, other);
	}

	/**
	 * Returns true if the string starts with the given string, with optional case matching.
	 */
	public static boolean startsWith(boolean matchCase, String s, String other) {
		return equalsAt(matchCase, s, 0, other);
	}

	/**
	 * Returns true if the char sequence ends with the given char sequence.
	 */
	public static boolean endsWith(CharSequence s, CharSequence other) {
		return equalsAt(s, length(s) - length(other), other);
	}

	/**
	 * Returns true if the string ends with the given string, with optional case matching.
	 */
	public static boolean endsWith(boolean matchCase, String s, String other) {
		return equalsAt(matchCase, s, length(s) - length(other), other);
	}

	/**
	 * Returns true if the char sequence contains the given char sequence.
	 */
	public static boolean contains(CharSequence s, CharSequence other) {
		return contains(s, 0, other, 0);
	}

	/**
	 * Returns true if the bounded char sequence contains the given bounded char sequence.
	 */
	public static boolean contains(CharSequence s, int soffset, CharSequence other, int ooffset) {
		return contains(s, soffset, Integer.MAX_VALUE, other, ooffset, Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the bounded char sequence contains the given bounded char sequence.
	 */
	public static boolean contains(CharSequence s, int soffset, int slength, CharSequence other,
		int ooffset, int olength) {
		if (s == null || other == null) return false;
		return ArrayUtil.applyBiSlice(length(s), soffset, slength, length(other), ooffset, olength,
			(so, sl, oo, ol) -> {
				for (int i = 0; i <= sl - ol; i++)
					if (regionMatches(s, so + i, other, oo, ol)) return true;
				return false;
			});
	}

	/**
	 * Returns true if the string contains the given string, with optional case matching.
	 */
	public static boolean contains(boolean matchCase, String s, String other) {
		return contains(matchCase, s, 0, other, 0);
	}

	/**
	 * Returns true if the bounded string contains the given bounded string, optionally ignoring
	 * case.
	 */
	public static boolean contains(boolean matchCase, String s, int soffset, String other,
		int ooffset) {
		return contains(matchCase, s, soffset, Integer.MAX_VALUE, other, ooffset,
			Integer.MAX_VALUE);
	}

	/**
	 * Returns true if the bounded string contains the given bounded string, optionally ignoring
	 * case.
	 */
	public static boolean contains(boolean matchCase, String s, int soffset, int slength,
		String other, int ooffset, int olength) {
		if (s == null || other == null) return false;
		return ArrayUtil.applyBiSlice(length(s), soffset, slength, length(other), ooffset, olength,
			(so, sl, oo, ol) -> {
				for (int i = 0; i <= sl - ol; i++)
					if (s.regionMatches(!matchCase, so + i, other, oo, ol)) return true;
				return false;
			});
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
	 * Replaces unprintable chars with '.'.
	 */
	public static String printable(CharSequence s) {
		return printable(s, '.');
	}

	/**
	 * Replaces unprintable chars with given char.
	 */
	public static String printable(CharSequence s, char replacement) {
		return replaceChars(s, c -> Chars.isPrintable(c) ? c : replacement);
	}

	/**
	 * Replaces chars using replacer function. Return < 0 for no change.
	 */
	public static <E extends Exception> String replaceChars(CharSequence s,
		Chars.Operator<E> charReplacer) throws E {
		if (isEmpty(s)) return "";
		if (charReplacer == null) return s.toString();
		var b = StringBuilders.State.of(s);
		for (int i = 0; i < s.length(); i++)
			b.append(i, charReplacer.applyAsChar(s.charAt(i)));
		return b.toString();
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
			return b.toString();
		}
	}

	// support

	private static boolean regionMatches(CharSequence s, int soffset, CharSequence other,
		int ooffset, int length) {
		if (s == other && soffset == ooffset) return true;
		for (int i = 0; i < length; i++)
			if (!Chars.equals(s, soffset + i, other, ooffset + i)) return false;
		return true;
	}

	private static String compactFp(String s) {
		if (!s.endsWith(INTEGRAL_FLOAT)) return s;
		return s.substring(0, s.length() - INTEGRAL_FLOAT.length());
	}
}
