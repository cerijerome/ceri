package ceri.common.test;

import java.util.Objects;
import ceri.common.util.Timer;

/**
 * This is a holder for a state value. Useful when testing behavior across threads. For example, set
 * a value on one thread and wait for it on another.
 */
public class TestState<T> {
	private static final int DEFAULT_WAIT_MS = 1000;
	private T value;

	/**
	 * Constructor with null initial state value.
	 */
	public TestState() {
		this(null);
	}

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
		T old = this.value;
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
			waitForValue(value, ms);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return get();
	}

	private void waitForValue(T value, long ms) throws InterruptedException {
		Timer timer = (ms == 0 ? Timer.INFINITE : Timer.millis(ms)).start();
		while (!Objects.equals(this.value, value)) {
			Timer.Snapshot snapshot = timer.snapshot();
			if (snapshot.expired()) break;
			if (snapshot.infinite()) wait(0);
			else snapshot.applyRemaining(this::wait);
		}
	}

}
