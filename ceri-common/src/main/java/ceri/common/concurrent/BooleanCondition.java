package ceri.common.concurrent;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class BooleanCondition {
	private final Object value = new Object();
	private final ValueCondition<Object> condition;

	public BooleanCondition() {
		condition = new ValueCondition<>();
	}

	public void signal() {
		condition.signal(value);
	}

	public void await() throws InterruptedException {
		condition.await();
	}

	public void await(long timeoutMs) throws InterruptedException {
		condition.await(timeoutMs);
	}

	public boolean isSet() {
		return condition.value() != null;
	}

}
