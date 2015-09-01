package ceri.log.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Utility methods to assist with logging.
 */
public class LogUtil {
	static final Pattern SPACE_REGEX = Pattern.compile("\\s+");

	private LogUtil() {}

	/**
	 * Closes a closeable, and logs any exceptions as a warning.
	 */
	public static boolean close(Logger logger, Closeable closeable) {
		if (closeable == null) return false;
		try {
			closeable.close();
			return true;
		} catch (IOException | RuntimeException e) {
			logger.catching(Level.WARN, e);
			return false;
		}
	}

	/**
	 * Interface used with toString to lazily generate a string.
	 */
	public static interface ToLazyString {
		String toLazyString() throws Exception;
	}

	/**
	 * Returns an object whose toString() executes the given toLazyString() method.
	 * Used for logging lazy string instantiations.
	 */
	public static Object toString(final ToLazyString toLazyString) {
		return new Object() {
			@Override
			public String toString() {
				try {
					return toLazyString.toLazyString();
				} catch (RuntimeException e) {
					throw e;
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
		return toString(() -> Integer.toHexString(value));
	}

	/**
	 * Returns an object with a compact toString(), replacing multiple whitespace with a single
	 * space.
	 */
	public static Object compact(final Object obj) {
		return toString(() -> SPACE_REGEX.matcher(String.valueOf(obj)).replaceAll(" "));
	}

}
