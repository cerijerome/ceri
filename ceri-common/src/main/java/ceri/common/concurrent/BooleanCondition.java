package ceri.common.concurrent;

import java.util.concurrent.TimeoutException;
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

	public void signal() {
		condition.signal(value);
	}

	public void await() throws InterruptedException {
		condition.await();
	}

	public void await(long timeoutMs) throws InterruptedException, TimeoutException {
		if (condition.await(timeoutMs) == null) throw new TimeoutException();
	}

	public boolean isSet() {
		return condition.value() != null;
	}

}
