package ceri.common.log;

import java.util.regex.Pattern;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	static final Pattern SPACE_REGEX = Pattern.compile("\\s+");

	private LogUtil() {}

	/**
	 * Interface used with toString to lazily generate a string.
	 */
	public static interface ToLazyString {
		String toLazyString() throws Exception;
	}

	/**
	 * Returns an object whose toString() executes the given object's toString() method.
	 * Convenient for use with lambda notation.
	 */
	public static Object toString(final ToLazyString toLazyString) {
		return new Object() {
			@Override
			public String toString() {
				try {
					return toLazyString.toLazyString();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	/**
	 * Returns an object whose toString() returns the hex string of the given integer value.
	 */
	public static Object toHex(final int value) {
		return new Object() {
			@Override
			public String toString() {
				return Integer.toHexString(value);
			}
		};
	}

	/**
	 * Returns an object with a compact toString(), replacing multiple whitespace with a single
	 * space.
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
