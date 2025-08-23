package ceri.common.text;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.function.Predicates;
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
	 * Joining patterns.
	 */
	public static class Join {
		public static final Joiner or = Joiner.of("(", "|", ")");
		public static final Joiner orNoCap = Joiner.of("(?:", "|", ")");
		
		private Join() {}
	}
	
	/**
	 * Split patterns.
	 */
	public static class Split {
		public static final Pattern line = Pattern.compile("(\\r\\n|\\n|\\r)");
		public static final Pattern comma = Pattern.compile("\\s*,\\s*");
		public static final Pattern space = Pattern.compile("\\s+");

		private Split() {}
		
		/**
		 * Split the string into an array.
		 */
		public static String[] array(Pattern pattern, CharSequence s) {
			if (pattern == null || Strings.isEmpty(s)) return ArrayUtil.Empty.strings;
			return pattern.split(s);
		}

		/**
		 * Split the string into a list.
		 */
		public static List<String> list(Pattern pattern, CharSequence s) {
			return Arrays.asList(split(pattern, s));
		}

		/**
		 * Split the string as a stream.
		 */
		public static Stream<RuntimeException, String> stream(Pattern pattern, CharSequence s) {
			if (pattern == null || Strings.isEmpty(s)) return Stream.empty();
			return Streams.from(pattern.splitAsStream(s));
		}
	}
	
	/**
	 * Regex filters.
	 */
	public static class Filter {
		private Filter() {}

		/**
		 * Returns true if the pattern is found.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> find(String format,
			Object... objs) {
			return find(compile(format, objs));
		}

		/**
		 * Returns true if the pattern is found.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> find(Pattern pattern) {
			return matching(pattern, Matcher::find);
		}

		/**
		 * Returns true if the pattern matches.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> match(String format,
			Object... objs) {
			return match(compile(format, objs));
		}

		/**
		 * Returns true if the pattern matches.
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> match(Pattern pattern) {
			return matching(pattern, Matcher::matches);
		}

		/**
		 * Applies the predicate to the matcher (before find or match is called).
		 */
		public static <E extends Exception> Excepts.Predicate<E, String> matching(Pattern pattern,
			Excepts.Predicate<? extends E, ? super Matcher> predicate) {
			if (pattern == null || predicate == null) return Predicates.no();
			return t -> t != null && predicate.test(pattern.matcher(t));
		}
	}

	/**
	 * Wrapper for matcher.
	 */
	public static class Match {
		public final Matcher matcher;

		private Match(Matcher matcher) {
			this.matcher = matcher;
		}

		/**
		 * Returns the matcher groups as a stream, or empty stream if no matches.
		 */
		public static Stream<RuntimeException, String> groups(Matcher m) {
			if (!hasMatch(m)) return Stream.empty();
			int count = m.groupCount();
			if (count <= 0) return Stream.empty();
			return Streams.slice(1, count).mapToObj(m::group);
		}

		public <E extends Exception> boolean accept(Excepts.Consumer<E, ? super Matcher> consumer)
			throws E {
			if (!matcher.hasMatch() || consumer == null) return false;
			consumer.accept(matcher);
			return true;
		}

		public <E extends Exception, T> T accept(Excepts.Function<E, ? super Matcher, T> function,
			T def) throws E {
			if (!matcher.hasMatch() || function == null) return def;
			return function.apply(matcher);
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
	 * Returns true if the matcher has a match.
	 */
	public static boolean hasMatch(Matcher m) {
		return m != null && m.hasMatch();
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
	 * Split the string into an array.
	 */
	public static String[] split(Pattern p, CharSequence s) {
		if (p == null || Strings.isEmpty(s)) return ArrayUtil.Empty.strings;
		return p.split(s);
	}

	/**
	 * Split the string into a list.
	 */
	public static List<String> splitList(Pattern p, CharSequence s) {
		return Arrays.asList(split(p, s));
	}

	/**
	 * Split the string as a stream.
	 */
	public static Stream<RuntimeException, String> splitStream(Pattern p, CharSequence s) {
		if (p == null || Strings.isEmpty(s)) return Stream.empty();
		return Streams.from(p.splitAsStream(s));
	}

	/**
	 * Returns the matcher after find().
	 */
	public static Matcher find(Matcher m) {
		if (m != null) m.find();
		return m;
	}

	/**
	 * Calls the consumer on successful find().
	 */
	public static <E extends Exception> boolean find(Matcher m,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		if (!hasMatch(find(m))) return false;
		consumer.accept(m);
		return true;
	}

	/**
	 * Calls the consumer on successful matches().
	 */
	public static <E extends Exception> boolean find(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(find(pattern.matcher(text)), consumer);
	}

	/**
	 * Returns the matcher after matches().
	 */
	public static Matcher match(Matcher m) {
		if (m != null) m.matches();
		return m;
	}

	/**
	 * Calls the consumer on successful matches().
	 */
	public static <E extends Exception> boolean match(Pattern pattern, CharSequence text,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		return accept(match(pattern.matcher(text)), consumer);
	}

	/**
	 * Calls the consumer if the matcher has a match.
	 */
	public static <E extends Exception> boolean accept(Matcher m,
		Excepts.Consumer<E, ? super Matcher> consumer) throws E {
		if (!hasMatch(m)) return false;
		consumer.accept(m);
		return true;
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
	 * Returns the groups of the given matcher as a stream, or empty stream if no match.
	 */
	public static Stream<RuntimeException, String> groups(Matcher m) {
		if (!hasMatch(m)) return Stream.empty();
		int count = m.groupCount();
		if (count <= 0) return Stream.empty();
		return Streams.slice(1, count).mapToObj(m::group);
	}

}
