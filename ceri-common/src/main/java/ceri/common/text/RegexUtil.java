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
import ceri.common.util.BasicUtil;
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

	public static Predicate<String> finder(Pattern p) {
		return s -> s == null ? false : p.matcher(s).find();
	}
	
	public static Predicate<String> matcher(Pattern p) {
		return s -> s == null ? false : p.matcher(s).matches();
	}
	
	/**
	 * Compiles a pattern from string format.
	 */
	public static Pattern compile(String format, Object... objs) {
		return Pattern.compile(String.format(format, objs));
	}

	/**
	 * Allows for-each loop over match results.
	 */
	public static Iterable<MatchResult> forEach(Pattern pattern, String s) {
		return BasicUtil.forEach(pattern.matcher(s).results().iterator());
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
		Matcher m = regex.matcher(s);
		if (!m.find()) return Collections.emptyList();
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
