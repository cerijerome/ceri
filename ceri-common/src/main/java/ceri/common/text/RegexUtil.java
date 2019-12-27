package ceri.common.text;

import static ceri.common.collection.StreamUtil.toList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.collection.CollectionUtil;
import ceri.common.factory.Factories;
import ceri.common.factory.Factory;
import ceri.common.factory.StringFactories;
import ceri.common.function.ObjIntFunction;
import ceri.common.util.BasicUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.PrimitiveUtil;

public class RegexUtil {
	public static final Pattern ALL = Pattern.compile(".*");
	private static final Pattern GROUP_NAME_REGEX = Pattern.compile("\\(\\?\\<([^>]+)\\>");
	private static final Factory<List<Boolean>, Iterable<String>> BOOLEAN_LIST_FACTORY =
		Factories.list(StringFactories.TO_BOOLEAN);
	private static final Factory<List<Byte>, Iterable<String>> BYTE_LIST_FACTORY =
		Factories.list(StringFactories.TO_BYTE);
	private static final Factory<List<Short>, Iterable<String>> SHORT_LIST_FACTORY =
		Factories.list(StringFactories.TO_SHORT);
	private static final Factory<List<Integer>, Iterable<String>> INT_LIST_FACTORY =
		Factories.list(StringFactories.TO_INTEGER);
	private static final Factory<List<Long>, Iterable<String>> LONG_LIST_FACTORY =
		Factories.list(StringFactories.TO_LONG);
	private static final Factory<List<Float>, Iterable<String>> FLOAT_LIST_FACTORY =
		Factories.list(StringFactories.TO_FLOAT);
	private static final Factory<List<Double>, Iterable<String>> DOUBLE_LIST_FACTORY =
		Factories.list(StringFactories.TO_DOUBLE);

	private RegexUtil() {}

	public static int hashCode(Pattern pattern) {
		if (pattern == null) return HashCoder.hash((Object) null);
		return HashCoder.hash(pattern.pattern(), pattern.flags());
	}

	public static boolean equals(Pattern lhs, Pattern rhs) {
		if (lhs == rhs) return true;
		if (lhs == null || rhs == null) return false;
		if (!lhs.pattern().equals(rhs.pattern())) return false;
		return lhs.flags() == rhs.flags();
	}

	public static Predicate<String> finder(String format, Object... objs) {
		return finder(compile(format, objs));
	}

	public static Predicate<String> finder(Pattern p) {
		return s -> s != null && p.matcher(s).find();
	}

	public static Predicate<String> matcher(String format, Object... objs) {
		return matcher(compile(format, objs));
	}

	public static Predicate<String> matcher(Pattern p) {
		return s -> s != null && p.matcher(s).matches();
	}

	/**
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(StringUtil.format(format, objs));
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
		return BasicUtil.forEach(pattern.matcher(s).results().iterator());
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(String pattern, String s, String replacement) {
		return replaceAllQuoted(Pattern.compile(pattern), s, replacement);
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(String pattern, String s,
		Function<MatchResult, String> replacer) {
		return replaceAllQuoted(Pattern.compile(pattern), s, replacer);
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(String pattern, String s,
		ObjIntFunction<MatchResult, String> replacer) {
		return replaceAllQuoted(Pattern.compile(pattern), s, replacer);
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(Pattern pattern, String s, String replacement) {
		return replaceAllQuoted(pattern, s, (m, i) -> replacement);
	}

	/**
	 * Replaces pattern matches with well behaved \ and $ in the replacement string.
	 */
	public static String replaceAllQuoted(Pattern pattern, String s,
		Function<MatchResult, String> replacer) {
		return replaceAll(pattern, s, (m, i) -> quote(replacer.apply(m)));
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
	 * Replace all instances of the pattern using the replacer function.
	 */
	public static String replaceAll(String pattern, String s,
		Function<MatchResult, String> replacer) {
		return replaceAll(Pattern.compile(pattern), s, replacer);
	}

	/**
	 * Replace all instances of the pattern using the replacer function with index.
	 */
	public static String replaceAll(String pattern, String s,
		ObjIntFunction<MatchResult, String> replacer) {
		return replaceAll(Pattern.compile(pattern), s, replacer);
	}

	/**
	 * Same as Matcher.replaceAll, but the replacer function can return null to skip replacement.
	 */
	public static String replaceAll(Pattern p, String s, Function<MatchResult, String> replacer) {
		return replaceAll(p, s, (m, i) -> replacer.apply(m));
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
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 */
	public static String replaceExcept(String pattern, String s, String replacement) {
		return replaceExcept(Pattern.compile(pattern), s, replacement);
	}
	
	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 */
	public static String replaceExcept(String pattern, String s, Function<String, String> replacer) {
		return replaceExcept(Pattern.compile(pattern), s, replacer);
	}
	
	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 * Replacement index is passed to the function.
	 */
	public static String replaceExcept(String pattern, String s,
		ObjIntFunction<String, String> replacer) {
		return replaceExcept(Pattern.compile(pattern), s, replacer);
	}
	
	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 */
	public static String replaceExcept(Pattern p, String s, String replacement) {
		return replaceExcept(p, s, (t, i) -> replacement);
	}
	
	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 */
	public static String replaceExcept(Pattern p, String s, Function<String, String> replacer) {
		return replaceExcept(p, s, (t, i) -> replacer.apply(t));
	}
	
	/**
	 * Replaces text that does not match the pattern. Replacer can return null to skip replacement. 
	 * Replacement index is passed to the function.
	 */
	public static String replaceExcept(Pattern p, String s,
		ObjIntFunction<String, String> replacer) {
		Matcher m = p.matcher(s);
		StringBuilder b = new StringBuilder();
		int start = 0; // start position of next append
		int end = 0; // end position to next append
		int i = 0;
		while (end < s.length()) {
			boolean found = m.find(); // If not found, match end of string
			int mStart = found ? m.start() : s.length();
			int mEnd = found ? m.end() : mStart;
			String except = s.substring(end, mStart);
			String replacement = except.isEmpty() ? null : replacer.apply(except, i++);
			if (replacement == null) end = mEnd;
			else {
				b.append(s.substring(start, end)).append(replacement);
				start = mStart;
				end = mEnd;
			}
		}
		if (start == 0 && b.length() == 0) return s;
		return b.append(s.substring(start, end)).toString();
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
	 * Splits a string by splitting before each instance of the pattern.
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
		return CollectionUtil.toMap(Function.identity(), name -> namedGroup(m, name), names);
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
	 * Finds the first matching regex and returns the first group if it exists, otherwise the entire
	 * matched pattern.
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
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Boolean booleanGroup(Matcher m, int group) {
		return PrimitiveUtil.booleanValue(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Byte byteGroup(Matcher m, int group) {
		return PrimitiveUtil.byteDecode(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Short shortGroup(Matcher m, int group) {
		return PrimitiveUtil.shortDecode(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Integer intGroup(Matcher m, int group) {
		return PrimitiveUtil.intDecode(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Long longGroup(Matcher m, int group) {
		return PrimitiveUtil.longDecode(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Float floatGroup(Matcher m, int group) {
		return PrimitiveUtil.floatValue(group(m, group));
	}

	/**
	 * Returns the group or null. Matcher match should have been attempted.
	 */
	public static Double doubleGroup(Matcher m, int group) {
		return PrimitiveUtil.doubleValue(group(m, group));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Boolean findBoolean(Pattern regex, String s) {
		return StringFactories.TO_BOOLEAN.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Byte findByte(Pattern regex, String s) {
		return StringFactories.TO_BYTE.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Short findShort(Pattern regex, String s) {
		return StringFactories.TO_SHORT.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Integer findInt(Pattern regex, String s) {
		return StringFactories.TO_INTEGER.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Long findLong(Pattern regex, String s) {
		return StringFactories.TO_LONG.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Float findFloat(Pattern regex, String s) {
		return StringFactories.TO_FLOAT.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static Double findDouble(Pattern regex, String s) {
		return StringFactories.TO_DOUBLE.create(find(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<String> findAll(Pattern regex, String s) {
		List<String> values = new ArrayList<>();
		Matcher m = regex.matcher(s);
		while (m.find())
			values.add(m.group(1));
		return values;
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Boolean> findAllBooleans(Pattern regex, String s) {
		return BOOLEAN_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Byte> findAllBytes(Pattern regex, String s) {
		return BYTE_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Short> findAllShorts(Pattern regex, String s) {
		return SHORT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Integer> findAllInts(Pattern regex, String s) {
		return INT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Long> findAllLongs(Pattern regex, String s) {
		return LONG_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Float> findAllFloats(Pattern regex, String s) {
		return FLOAT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static List<Double> findAllDoubles(Pattern regex, String s) {
		return DOUBLE_LIST_FACTORY.create(findAll(regex, s));
	}

}
