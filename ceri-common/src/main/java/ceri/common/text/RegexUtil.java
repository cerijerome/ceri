package ceri.common.text;

import static ceri.common.collection.StreamUtil.toList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import ceri.common.factory.Factories;
import ceri.common.factory.Factory;
import ceri.common.factory.StringFactories;

public class RegexUtil {
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
	 * Returns named group or null.
	 */
	public static final String namedGroup(Matcher m, String name) {
		try {
			return m.group(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Returns the groups of the given matcher as a list.
	 */
	public static final List<String> groups(Pattern regex, String s) {
		Matcher m = regex.matcher(s);
		if (!m.find()) return Collections.emptyList();
		return groups(m);
	}

	/**
	 * Returns the groups of the given matcher as a list. A successful find() must be called on the
	 * given matcher before using this method.
	 */
	public static final List<String> groups(Matcher m) {
		int count = m.groupCount();
		if (count <= 0) return Collections.emptyList();
		return toList(IntStream.range(1, count + 1).mapToObj(m::group));
	}

	/**
	 * Finds the first matching regex and returns the first group if it exists, otherwise the entire
	 * matched pattern.
	 */
	public static final String find(Pattern regex, String s) {
		if (s == null) return null;
		Matcher m = regex.matcher(s);
		if (!m.find()) return null;
		int group = m.groupCount() > 0 ? 1 : 0;
		return m.group(group);
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Boolean findBoolean(Pattern regex, String s) {
		return StringFactories.TO_BOOLEAN.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Byte findByte(Pattern regex, String s) {
		return StringFactories.TO_BYTE.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Short findShort(Pattern regex, String s) {
		return StringFactories.TO_SHORT.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Integer findInt(Pattern regex, String s) {
		return StringFactories.TO_INTEGER.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Long findLong(Pattern regex, String s) {
		return StringFactories.TO_LONG.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Float findFloat(Pattern regex, String s) {
		return StringFactories.TO_FLOAT.create(find(regex, s));
	}

	/**
	 * Finds the first matching regex and returns the first group.
	 */
	public static final Double findDouble(Pattern regex, String s) {
		return StringFactories.TO_DOUBLE.create(find(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<String> findAll(Pattern regex, String s) {
		List<String> values = new ArrayList<>();
		Matcher m = regex.matcher(s);
		while (m.find())
			values.add(m.group(1));
		return values;
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Boolean> findAllBooleans(Pattern regex, String s) {
		return BOOLEAN_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Byte> findAllBytes(Pattern regex, String s) {
		return BYTE_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Short> findAllShorts(Pattern regex, String s) {
		return SHORT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Integer> findAllInts(Pattern regex, String s) {
		return INT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Long> findAllLongs(Pattern regex, String s) {
		return LONG_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Float> findAllFloats(Pattern regex, String s) {
		return FLOAT_LIST_FACTORY.create(findAll(regex, s));
	}

	/**
	 * Finds matching regex and returns the first group for each match.
	 */
	public static final List<Double> findAllDoubles(Pattern regex, String s) {
		return DOUBLE_LIST_FACTORY.create(findAll(regex, s));
	}

}
