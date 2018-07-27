package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class BooleanCondition {
	private final Object value = new Object();
	private final ValueCondition<Object> condition;

	public static BooleanCondition create() {
		return create(new ReentrantLock());
	}

	public static BooleanCondition create(Lock lock) {
		return new BooleanCondition(ValueCondition.create(lock));
	}

	private BooleanCondition(ValueCondition<Object> condition) {
		this.condition = condition;
	}

	public void clear() {
		condition.clear();
	}

	public void signal() {
		condition.signal(value);
	}

	public void await() throws InterruptedException {
		condition.await();
	}

	public boolean await(long timeoutMs) throws InterruptedException {
		return isSet(condition.await(timeoutMs));
	}

	public boolean awaitPeek() throws InterruptedException {
		return isSet(condition.awaitPeek());
	}

	public boolean awaitPeek(long timeoutMs) throws InterruptedException {
		return isSet(condition.awaitPeek(timeoutMs));
	}

	public boolean isSet() {
		return condition.value() != null;
	}

	private boolean isSet(Object value) {
		return value != null;
	}
	
}
