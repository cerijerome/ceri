package ceri.common.test;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ceri.common.collect.Enums;
import ceri.common.text.Regex;

/**
 * The test class naming conventions used. Use to identify tests, and convert between target and
 * test forms for class name, files and paths.
 */
public enum TestStyle {
	none("", ""),
	test("Test", "test"),
	behavior("Behavior", "should");

	// List of main class suffixes that are most likely to use test style
	private static final List<String> testGuessSuffixes = List.of("Util");
	private static final Pattern REGEX =
		Regex.compile("^(.*?)(%s|%s|)(\\.java|\\.class|)$", test.suffix, behavior.suffix);
	private static final Map<String, TestStyle> lookup = Enums.map(t -> t.suffix, TestStyle.class);
	private static final int TARGET_INDEX = 1;
	private static final int STYLE_INDEX = 2;
	private static final int FILE_TYPE_INDEX = 3;
	public final String suffix;
	public final String methodPrefix;

	/**
	 * Guess from class - can be target or test class.
	 */
	public static TestStyle guessFrom(Class<?> cls) {
		if (cls == null) return none;
		return guessFrom(cls.getSimpleName());
	}

	/**
	 * Guess from class - can be target or test class.
	 */
	public static TestStyle guessFrom(String name) {
		var m = Regex.match(REGEX, name);
		if (!m.hasMatch()) return none;
		var style = fromSuffix(m.group(STYLE_INDEX));
		if (!style.isNone()) return style;
		var target = m.group(TARGET_INDEX);
		if (target.isEmpty()) return none;
		return testGuessSuffixes.stream().anyMatch(s -> target.endsWith(s)) ? test : behavior;
	}

	/**
	 * Returns the test target. Simple/full class names, filenames, and paths are permitted. Returns
	 * given string if it does not match a test style.
	 */
	public static String target(String test) {
		return none.test(test);
	}

	/**
	 * Determines if the given test matches a test style. Simple/full class names, filenames, and
	 * paths are permitted.
	 */
	public static boolean hasStyle(String test) {
		return !from(test).isNone();
	}

	/**
	 * Returns the test style. Simple/full class names, filenames, and paths are permitted. Returns
	 * null if the given string does not match a test style.
	 */
	public static TestStyle from(String test) {
		var m = Regex.match(REGEX, test);
		if (!m.hasMatch()) return none;
		return fromSuffix(m.group(STYLE_INDEX));
	}

	/**
	 * Lookup style from suffix.
	 */
	public static TestStyle fromSuffix(String suffix) {
		return lookup.getOrDefault(suffix, none);
	}

	private TestStyle(String suffix, String methodPrefix) {
		this.suffix = suffix;
		this.methodPrefix = methodPrefix;
	}

	/**
	 * Check if not a test style.
	 */
	public boolean isNone() {
		return this == none;
	}

	/**
	 * Converts the target to test, by adding the style suffix. Simple/full class names, filenames,
	 * and paths are permitted.
	 */
	public String test(String target) {
		var m = Regex.match(REGEX, target);
		if (!m.hasMatch()) return target;
		return m.group(TARGET_INDEX) + suffix + m.group(FILE_TYPE_INDEX);
	}
}
