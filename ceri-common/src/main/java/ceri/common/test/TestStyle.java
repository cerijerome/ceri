package ceri.common.test;

import static ceri.common.collection.ImmutableUtil.enumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.text.RegexUtil;

/**
 * The test class naming conventions used. Use to identify tests, and convert between target and
 * test forms for class name, files and paths.
 */
public enum TestStyle {
	none(""),
	test("Test"),
	behavior("Behavior");

	private static final Pattern REGEX =
		RegexUtil.compile("^(.*?)(%s|%s|)(\\.java|\\.class|)$", test.suffix, behavior.suffix);
	private static final Map<String, TestStyle> lookup = enumMap(t -> t.suffix, TestStyle.class);
	private static final int TARGET_INDEX = 1;
	private static final int STYLE_INDEX = 2;
	private static final int FILE_TYPE_INDEX = 3;
	public final String suffix;

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
		Matcher m = RegexUtil.matched(REGEX, test);
		if (m == null) return none;
		return fromSuffix(m.group(STYLE_INDEX));
	}

	/**
	 * Lookup style from suffix.
	 */
	public static TestStyle fromSuffix(String suffix) {
		return lookup.getOrDefault(suffix, none);
	}

	private TestStyle(String suffix) {
		this.suffix = suffix;
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
		Matcher matcher = RegexUtil.matched(REGEX, target);
		if (matcher == null) return target;
		return matcher.group(TARGET_INDEX) + suffix + matcher.group(FILE_TYPE_INDEX);
	}

}
