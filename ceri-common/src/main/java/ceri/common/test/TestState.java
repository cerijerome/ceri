package ceri.common.test;

/**
 * This is a holder for a state value. Useful when testing behavior across threads. For example, set
 * a value on one thread and wait for it on another.
 */
public class TestState<T> {
	private static final int DEFAULT_WAIT_MS = 1000;
	private T value = null;

	/**
	 * Constructor with null initial state value.
	 */
	public TestState() {}

	/**
	 * Constructor that sets initial state value.
	 */
	public TestState(T value) {
		set(value);
	}

	/**
	 * Sets state value in thread-safe manner.
	 */
	public synchronized T set(T value) {
		T old = value;
		this.value = value;
		notifyAll();
		return old;
	}

	/**
	 * Reads state value in thread-safe manner.
	 */
	public synchronized T get() {
		return value;
	}

	/**
	 * Wait for state value to equal given value, up to given 1 second. Null is an allowed value.
	 */
	public synchronized T waitFor(T value) {
		return waitFor(value, DEFAULT_WAIT_MS);
	}

	/**
	 * Wait for state value to equal given value, up to given number of milliseconds. Null is an
	 * allowed value.
	 */
	public synchronized T waitFor(T value, long ms) {
		try {
			long t = System.currentTimeMillis() + ms;
			while (!equalsValue(value) && (ms == 0 || System.currentTimeMillis() < t)) {
				if (ms == 0) wait(0);
				else wait(t - System.currentTimeMillis());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return get();
	}

	private boolean equalsValue(T value) {
		if (this.value == value) return true;
		if (this.value == null) return false;
		return this.value.equals(value);
	}

}
