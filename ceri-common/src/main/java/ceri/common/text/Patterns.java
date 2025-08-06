package ceri.common.text;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.stream.Stream;
import ceri.common.stream.Streams;

/**
 * Support for regex patterns.
 */
public class Patterns {
	private static final Joiner OR_JOINER = Joiner.of("(", "|", ")");
	private static final Pattern GROUP_NAME_REGEX = Pattern.compile("\\(\\?\\<([^>]+)\\>");
	public static final Pattern ALL = Pattern.compile(".*");

	private Patterns() {}

	/**
	 * Common patterns.
	 */
	public static class Common {
		/** Unsigned binary integer with 0b prefix (needs custom parser). */
		public static final String BIN_UINT = "0[bB][01]+";
		/** Signed binary integer with 0b prefix (needs custom parser). */
		public static final String BIN_INT = "[+-]?0[bB][01]+";
		/** Unsigned octal integer with 0 prefix. */
		public static final String OCT_UINT = "0[0-7]+";
		/** Signed Octal integer with 0 prefix. */
		public static final String OCT_INT = "[+-]?0[0-7]+";
		/** Unsigned decimal integer. */
		public static final String DEC_UINT = "(?:0|[1-9]\\d*)";
		/** Signed decimal integer. */
		public static final String DEC_INT = "[+-]?" + DEC_UINT;
		/** Single hexadecimal digit. */
		public static final String HEX_DIGIT = "[a-fA-F0-9]";
		/** Unsigned hexadecimal integer with prefix. */
		public static final String HEX_UINT = "(?:0x|0X|#)[a-fA-F0-9]+";
		/** Signed hexadecimal integer with prefix. */
		public static final String HEX_INT = "[+-]?" + HEX_UINT;
		/** Unsigned binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String UINT_NUMBER = or(DEC_UINT, HEX_UINT, OCT_UINT, BIN_UINT);
		/** Signed binary, octal, decimal, or hexadecimal integer (use decodes below). */
		public static final String INT_NUMBER = "[+-]?" + UINT_NUMBER;
		/** Unsigned decimal number; integer or floating point. */
		public static final String UDEC_NUMBER = "(?:0|[1-9]\\d*|\\d*\\.\\d+)";
		/** Signed decimal number; integer or floating point. */
		public static final String DEC_NUMBER = "[+-]?" + UDEC_NUMBER;
		/** Single ASCII letter. */
		public static final String ALPHABET = "[a-zA-Z]";
		/** Single ASCII letter or number. */
		public static final String ALPHANUM = "[a-zA-Z0-9]";
		/** Java identifier name. */
		public static final String JAVA_NAME = "[\\p{L}$_][\\p{L}0-9$_]*";

		private Common() {}

		/**
		 * Combines patterns as an OR non-capturing group.
		 */
		public static String or(String... patterns) {
			return "(?:" + String.join("|", patterns) + ")";
		}

		/**
		 * Use to decode UINT_NUMBER and INT_NUMBER.
		 */
		public static int decodeInt(String s) {
			return isBinaryPrefix(s) ? Integer.parseInt(s.substring(2), 2) : Integer.decode(s);
		}

		/**
		 * Use to decode UINT_NUMBER and INT_NUMBER.
		 */
		public static long decodeLong(String s) {
			return isBinaryPrefix(s) ? Long.parseLong(s.substring(2), 2) : Long.decode(s);
		}

		private static boolean isBinaryPrefix(String s) {
			return s.length() > 2 && s.charAt(0) == '0'
				&& (s.charAt(1) == 'b' || s.charAt(1) == 'B');
		}
	}

	/**
	 * Pattern does not override hashCode(); this method generates a hash code for a Pattern
	 * instance.
	 */
	public static int hashCode(Pattern pattern) {
		if (pattern == null) return Objects.hash();
		return Objects.hash(pattern.pattern(), pattern.flags());
	}

	/**
	 * Pattern does not override equals(); this method checks if patterns are equal.
	 */
	public static boolean equals(Pattern lhs, Pattern rhs) {
		if (lhs == rhs) return true;
		return lhs != null && rhs != null && Objects.equals(lhs.pattern(), rhs.pattern())
			&& lhs.flags() == rhs.flags();
	}

	/**
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(Strings.format(format, objs));
	}

	/**
	 * Compiles a pattern with a group OR of string values of given objects.
	 */
	public static Pattern compileOr(Object... objs) {
		return Pattern.compile(OR_JOINER.joinAll(objs));
	}

	/**
	 * Creates a pattern to search for quoted text, ignoring case.
	 */
	public static Pattern ignoreCase(String text) {
		return compile("(?i)\\Q" + text + "\\E");
	}

	/**
	 * Calls find and returns the matcher.
	 */
	public static Matcher find(Matcher m) {
		if (m != null) m.find();
		return m;
	}

	/**
	 * Calls matches and returns the matcher.
	 */
	public static Matcher match(Matcher m) {
		if (m != null) m.matches();
		return m;
	}

	/**
	 * Returns found groups as a stream.
	 */
	public static Stream<RuntimeException, String> findGroups(Pattern p, CharSequence s) {
		return groups(find(p.matcher(s)));
	}

	/**
	 * Returns matched groups as a stream.
	 */
	public static Stream<RuntimeException, String> matchGroups(Pattern p, CharSequence s) {
		return groups(match(p.matcher(s)));
	}

	/**
	 * Returns the groups of the given matcher as a stream. Find or match should have been attempted
	 * before calling.
	 */
	public static Stream<RuntimeException, String> groups(Matcher m) {
		if (m == null || !m.hasMatch()) return Stream.empty();
		int count = m.groupCount();
		if (count <= 0) return Stream.empty();
		return Streams.range(1, count).mapToObj(m::group);
	}

}
