package ceri.common.log;

import java.util.regex.Pattern;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	static final Pattern SPACE_REGEX = Pattern.compile("\\s+");

	private LogUtil() {}

	/**
	 * Returns an object with a compact toString(), replacing multiple
	 * whitespace with a single space.
	 */
	public static Object compact(final Object obj) {
		return new Object() {
			@Override
			public String toString() {
				return SPACE_REGEX.matcher(String.valueOf(obj)).replaceAll(" ");
			}
		};
	}

}