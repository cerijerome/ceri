package ceri.common.text;

import static ceri.common.stream.StreamUtil.toList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import ceri.common.collection.CollectionUtil;
import ceri.common.collection.IteratorUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions.Function;
import ceri.common.function.Functions.ObjIntFunction;
import ceri.common.function.Functions.Predicate;
import ceri.common.property.Parser;

/**
 * General utilities for regular expressions.
 */
public class RegexUtil {
	public static final Pattern ALL = Pattern.compile(".*");
	private static final Pattern GROUP_NAME_REGEX = Pattern.compile("\\(\\?\\<([^>]+)\\>");

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

	private RegexUtil() {}

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
		if (lhs == null || rhs == null) return false;
		if (!Objects.equals(lhs.pattern(), rhs.pattern())) return false;
		if (lhs.flags() != rhs.flags()) return false;
		return true;
	}

	/**
	 * A predicate that returns true if the pattern is found.
	 */
	public static Predicate<String> finder(String format, Object... objs) {
		return finder(compile(format, objs));
	}

	/**
	 * A predicate that returns true if the pattern is found.
	 */
	public static Predicate<String> finder(Pattern p) {
		return s -> s != null && p.matcher(s).find();
	}

	/**
	 * A predicate that returns true if the pattern matches.
	 */
	public static Predicate<String> matcher(String format, Object... objs) {
		return matcher(compile(format, objs));
	}

	/**
	 * A predicate that returns true if the pattern matches.
	 */
	public static Predicate<String> matcher(Pattern p) {
		return s -> s != null && p.matcher(s).matches();
	}

	/**
	 * A predicate that returns true if the pattern is not found.
	 */
	public static Predicate<String> nonFinder(String format, Object... objs) {
		return nonFinder(compile(format, objs));
	}

	public static Predicate<String> nonFinder(Pattern p) {
		return s -> s != null && !p.matcher(s).find();
	}

	public static Predicate<String> nonMatcher(String format, Object... objs) {
		return nonMatcher(compile(format, objs));
	}

	public static Predicate<String> nonMatcher(Pattern p) {
		return s -> s != null && !p.matcher(s).matches();
	}

	/**
	 * Returns a function that converts a string into a matching group or null. Null strings return
	 * null.
	 */
	public static Function<String, String> groupMatcher(int group, String format, Object... objs) {
		return groupMatcher(group, compile(format, objs));
	}

	/**
	 * Returns a function that converts a string into a matching group or null. Null strings return
	 * null.
	 */
	public static Function<String, String> groupMatcher(int group, Pattern pattern) {
		return s -> group(matched(pattern, s), group);
	}

	/**
	 * Returns a function that converts a string into a matching group or null. Null strings return
	 * null.
	 */
	public static Function<String, String> groupFinder(int group, String format, Object... objs) {
		return groupFinder(group, compile(format, objs));
	}

	/**
	 * Returns a function that converts a string into a matching group or null. Null strings return
	 * null.
	 */
	public static Function<String, String> groupFinder(int group, Pattern pattern) {
		return s -> group(found(pattern, s), group);
	}

	/**
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(StringUtil.format(format, objs));
	}

	/**
	 * Compiles a pattern with a group OR of string values of given objects.
	 */
	public static Pattern compileOr(Object... objs) {
		return Pattern.compile(
			Stream.of(objs).map(String::valueOf).collect(Collectors.joining("|", "(", ")")));
	}

	/**
	 * Creates a pattern to search for text, ignoring case
	 */
	public static Pattern ignoreCase(String text) {
		return compile("(?i)\\Q%s\\E", text);
	}

	/**
	 * Allows for-each loop over match results.
	 */
	public static Iterable<MatchResult> forEach(Pattern pattern, String s) {
		return IteratorUtil.iterable(pattern.matcher(s).results().iterator());
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(Pattern pattern, String s, String replacement) {
		return replaceAllQuoted(pattern, s, (_, _) -> replacement);
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(Pattern pattern, String s,
		Function<MatchResult, String> replacer) {
		return replaceAll(pattern, s, (m, _) -> quote(replacer.apply(m)));
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(Pattern pattern, String s,
		ObjIntFunction<MatchResult, String> replacer) {
		return replaceAll(pattern, s, (m, i) -> quote(replacer.apply(m, i)));
	}

	private static String quote(String s) {
		return s == null ? null : Matcher.quoteReplacement(s);
	}

	/**
	 * Same as Matcher.replaceAll, but the replacer function can return null to skip replacement.
	 */
	public static String replaceAll(Pattern p, String s, Function<MatchResult, String> replacer) {
		return replaceAll(p, s, (m, _) -> replacer.apply(m));
	}

	/**
	 * Same as Matcher.replaceAll, but the replacer function can return null to skip replacement.
	 * Match index is passed to the function.
	 */
	public static String replaceAll(Pattern p, String s,
		ObjIntFunction<MatchResult, String> replacer) {
		Matcher m = p.matcher(s);
		StringBuilder b = new StringBuilder();
		int start = 0; // start position of next append
		int i = 0;
		while (m.find()) {
			String replacement = replacer.apply(m, i++);
			if (replacement == null) continue;
			// Append from last append to m.start, then append replacement
			m.appendReplacement(b, replacement); // handles special \ and $
			start = m.end();
		}
		if (start == 0 && b.length() == 0) return s;
		return m.appendTail(b).toString(); // handles special \ and $
	}

	/**
	 * Replaces text that does not match the pattern.
	 */
	public static String replaceExcept(Pattern p, String s, String replacement) {
		return replaceExcept(p, s, (_, _) -> replacement);
	}

	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement.
	 */
	public static String replaceExcept(Pattern p, String s,
		Function<NonMatchResult, String> replacer) {
		return replaceExcept(p, s, (t, _) -> replacer.apply(t));
	}

	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement.
	 * Replacement index is passed to the function.
	 */
	public static String replaceExcept(Pattern p, String s,
		ObjIntFunction<NonMatchResult, String> replacer) {
		NonMatcher m = NonMatcher.of(p, s);
		StringBuilder b = new StringBuilder();
		int start = 0; // start position of next append
		int i = 0;
		while (m.find()) {
			String replacement = replacer.apply(m, i++);
			if (replacement == null) continue;
			// Append from last append to m.start, then append replacement
			m.appendReplacement(b, replacement);
			start = m.end();
		}
		if (start == 0 && b.length() == 0) return s;
		return m.appendTail(b).toString();
	}

	/**
	 * Splits a string by splitting before each instance of the pattern.
	 */
	public static List<String> splitBefore(Pattern pattern, String s) {
		Matcher m = pattern.matcher(s);
		List<String> list = new ArrayList<>();
		int last = 0;
		while (m.find()) {
			if (m.start() == 0) continue;
			list.add(s.substring(last, m.start()));
			last = m.start();
		}
		list.add(s.substring(last));
		return list;
	}

	/**
	 * Splits a string by splitting after each instance of the pattern.
	 */
	public static List<String> splitAfter(Pattern pattern, String s) {
		Matcher m = pattern.matcher(s);
		List<String> list = new ArrayList<>();
		int last = 0;
		while (m.find()) {
			if (m.end() == s.length()) break;
			list.add(s.substring(last, m.end()));
			last = m.end();
		}
		list.add(s.substring(last));
		return list;
	}

	/**
	 * Returns named group or null. Matcher match should have been attempted.
	 */
	public static String namedGroup(Matcher m, String name) {
		try {
			return m.group(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Returns named group or null. Matcher match should have been attempted.
	 */
	public static Map<String, String> namedGroups(Matcher m) {
		List<String> names = groupNames(m);
		if (names.isEmpty()) return Collections.emptyMap();
		return CollectionUtil.toMap(s -> s, name -> namedGroup(m, name), names);
	}

	/**
	 * Returns group names from the pattern.
	 */
	public static List<String> groupNames(Matcher m) {
		if (m == null) return Collections.emptyList();
		return groupNames(m.pattern());
	}

	/**
	 * Returns group names from the pattern.
	 */
	public static List<String> groupNames(Pattern pattern) {
		if (pattern == null) return Collections.emptyList();
		return findAll(GROUP_NAME_REGEX, pattern.pattern());
	}

	/**
	 * Returns the groups of the given matcher as a list.
	 */
	public static List<String> groups(Pattern regex, String s) {
		Matcher m = found(regex, s);
		if (m == null) return Collections.emptyList();
		return groups(m);
	}

	/**
	 * Returns the groups of the given matcher as a list. Matcher match should have been attempted.
	 */
	public static List<String> groups(Matcher m) {
		int count = m.groupCount();
		if (count <= 0) return Collections.emptyList();
		return toList(IntStream.range(1, count + 1).mapToObj(m::group));
	}

	/**
	 * Finds the first match and returns the first group if it exists, otherwise the entire matched
	 * pattern.
	 */
	public static String find(Pattern regex, String s) {
		return groupOrAll(found(regex, s), 1);
	}

	/**
	 * Matches the regex and returns the first group if it exists, otherwise the entire matched
	 * pattern.
	 */
	public static String match(Pattern regex, String s) {
		return groupOrAll(matched(regex, s), 1);
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	private static String groupOrAll(Matcher m, int group) {
		String s = group(m, group);
		if (s != null) return s;
		return group(m, 0);
	}

	/**
	 * Return matcher if pattern found, otherwise null.
	 */
	public static Matcher found(Pattern regex, String s) {
		if (s == null) return null;
		Matcher m = regex.matcher(s);
		if (!m.find()) return null;
		return m;
	}

	/**
	 * Return matcher if pattern matched, otherwise null.
	 */
	public static Matcher matched(Pattern regex, String s) {
		if (s == null) return null;
		Matcher m = regex.matcher(s);
		if (!m.matches()) return null;
		return m;
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static String group(Matcher m, int group) {
		if (m == null) return null;
		if (m.groupCount() < group) return null;
		return m.group(group);
	}

	/**
	 * Calls the consumer with the group only if non-null. Matcher match should have been attempted.
	 */
	public static <E extends Exception> void acceptGroup(Matcher m, int group,
		Excepts.Consumer<E, String> consumer) throws E {
		var s = group(m, group);
		if (s != null) consumer.accept(s);
	}

	/**
	 * Calls the function with the group only if non-null. Returns null is the group is null.
	 * Matcher match should have been attempted.
	 */
	public static <E extends Exception, T> T applyGroup(Matcher m, int group,
		Excepts.Function<E, String, T> function) throws E {
		var s = group(m, group);
		return s == null ? null : function.apply(s);
	}

	/**
	 * Returns a parser for the group or null. Matcher match should have been attempted before
	 * calling.
	 */
	public static Parser.String parse(Matcher m, int group) {
		return Parser.string(group(m, group));
	}

	/**
	 * Returns a parser for the first found group or null.
	 */
	public static Parser.String parseFind(Pattern regex, String s) {
		return Parser.string(find(regex, s));
	}

	/**
	 * Returns a parser for the group or null. Matcher match should have been attempted before
	 * calling.
	 */
	public static Parser.Strings parseFindAll(Pattern regex, String s) {
		return Parser.strings(findAll(regex, s));
	}

	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Boolean findBoolean(Pattern regex, String s) {
	// return Boolean.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Byte findByte(Pattern regex, String s) {
	// return Byte.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Short findShort(Pattern regex, String s) {
	// return Short.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Integer findInt(Pattern regex, String s) {
	// return Integer.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Long findLong(Pattern regex, String s) {
	// return Long.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Float findFloat(Pattern regex, String s) {
	// return Float.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds the first matching regex and returns the first group.
	// */
	// public static Double findDouble(Pattern regex, String s) {
	// return Double.valueOf(find(regex, s));
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<String> findAll(Pattern regex, String s) {
	// return findAll(regex, s, t -> t);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Boolean> findAllBooleans(Pattern regex, String s) {
	// return findAll(regex, s, Boolean::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Byte> findAllBytes(Pattern regex, String s) {
	// return findAll(regex, s, Byte::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Short> findAllShorts(Pattern regex, String s) {
	// return findAll(regex, s, Short::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Integer> findAllInts(Pattern regex, String s) {
	// return findAll(regex, s, Integer::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Long> findAllLongs(Pattern regex, String s) {
	// return findAll(regex, s, Long::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Float> findAllFloats(Pattern regex, String s) {
	// return findAll(regex, s, Float::valueOf);
	// }
	//
	// /**
	// * Finds matching regex and returns the first group for each match.
	// */
	// public static List<Double> findAllDoubles(Pattern regex, String s) {
	// return findAll(regex, s, Double::valueOf);
	// }
	//
	// private static <T> List<T> findAll(Pattern regex, String s, Function<String, T> fn) {
	// List<T> values = new ArrayList<>();
	// Matcher m = regex.matcher(s);
	// while (m.find())
	// values.add(fn.apply(m.group(1)));
	// return values;
	// }

	/**
	 * Finds the first match regex and collects the first group if it exists, otherwise the entire
	 * matched pattern. Repeats until no results, collecting the groups as a list.
	 */
	public static List<String> findAll(Pattern regex, String s) {
		List<String> values = new ArrayList<>();
		Matcher m = regex.matcher(s);
		while (m.find())
			values.add(m.group(1));
		return values;
	}

}
