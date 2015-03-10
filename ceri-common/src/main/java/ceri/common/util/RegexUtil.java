package ceri.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.factory.Factories;
import ceri.common.factory.Factory;
import ceri.common.factory.StringFactories;

public class RegexUtil {
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
	 * Finds the first matching regex and returns the first group.
	 */
	public static final String find(Pattern regex, String s) {
		Matcher m = regex.matcher(s);
		if (!m.find()) return null;
		return m.group(1);
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
		while (m.find()) values.add(m.group(1));
		return values;
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
