package ceri.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks thrown exceptions to determine if they are repeated based on type and message. Useful for
 * reducing log noise.
 */
public class ExceptionTracker {
	private static final int MAX_DEF = 100;
	public final int max;
	private final Map<Key, Throwable> map = new HashMap<>();

	static class Key {
		final String message;
		final Class<?> cls;

		Key(Class<?> cls, String message) {
			this.cls = cls;
			this.message = message;
		}

		@Override
		public int hashCode() {
			return Objects.hash(cls, message);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Key)) return false;
			Key other = (Key) obj;
			if (!Objects.equals(cls, other.cls)) return false;
			if (!Objects.equals(message, other.message)) return false;
			return true;
		}
	}

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
