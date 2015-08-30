package ceri.common.concurrent;

import java.util.concurrent.TimeoutException;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class BooleanCondition {
	private final Object value = new Object();
	private final ValueCondition<Object> condition;

	public BooleanCondition() {
		condition = ValueCondition.create();
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
