package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class BooleanCondition {
	private final Object value = new Object();
	private final ValueCondition<Object> condition;

	public static BooleanCondition of() {
		return of(new ReentrantLock());
	}

	public static BooleanCondition of(Lock lock) {
		return new BooleanCondition(ValueCondition.of(lock));
	}

	private BooleanCondition(ValueCondition<Object> condition) {
		this.condition = condition;
	}

	/**
	 * Clears the conditions without signaling waiting threads.
	 */
	public void clear() {
		condition.clear();
	}

	/**
	 * Signals waiting threads. Returns true if the signal state changed to true.
	 */
	public boolean signal() {
		return !isSet(condition.signal(value));
	}

	/**
	 * Waits indefinitely for a signal, and clears the signal.
	 */
	public void await() throws InterruptedException {
		condition.await();
	}

	/**
	 * Waits for a signal or timer to expire, and returns true if signaled. Clears the signal.
	 */
	public boolean await(long timeoutMs) throws InterruptedException {
		return isSet(condition.awaitTimeout(timeoutMs));
	}

	/**
	 * Waits indefinitely for a signal, and returns true if signaled. Does not clear the signal.
	 */
	public boolean awaitPeek() throws InterruptedException {
		return isSet(condition.awaitPeek());
	}

	/**
	 * Waits for a signal or timer to expire, and returns true if signaled. Does not clear the
	 * signal.
	 */
	public boolean awaitPeek(long timeoutMs) throws InterruptedException {
		return isSet(condition.awaitPeek(timeoutMs));
	}

	/**
	 * Returns true if signaled.
	 */
	public boolean isSet() {
		return condition.value() != null;
	}

	private boolean isSet(Object value) {
		return value != null;
	}

}
