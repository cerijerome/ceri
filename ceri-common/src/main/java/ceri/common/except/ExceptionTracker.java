package ceri.common.except;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks thrown exceptions to determine if they are repeated based on type and message. Useful for
 * reducing log noise.
 */
public class ExceptionTracker {
	private static final int MAX_DEF = 100;
	public final int max;
	private final Map<Key, Throwable> map = new HashMap<>();

	record Key(Class<?> cls, String message) {}

	public static ExceptionTracker of() {
		return of(MAX_DEF);
	}

	public static ExceptionTracker of(int max) {
		return new ExceptionTracker(max);
	}

	private ExceptionTracker(int max) {
		this.max = max;
	}

	/**
	 * Returns true if exception is new, based on exact type and message and the maximum number of
	 * exceptions received has not been exceeded. Typically a caller will log the exception if this
	 * method returns true.
	 */
	public boolean add(Throwable t) {
		if (t == null || map.size() >= max) return false;
		Key key = new Key(t.getClass(), t.getMessage());
		return map.putIfAbsent(key, t) == null;
	}

	/**
	 * Clear tracked exceptions.
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Returns true if no tracked exceptions.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns the number of tracked exceptions.
	 */
	public int size() {
		return map.size();
	}
}
