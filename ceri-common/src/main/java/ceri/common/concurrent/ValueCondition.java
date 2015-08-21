package ceri.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class ValueCondition<T> {
	private final Lock lock;
	private final Condition condition;
	private T value = null;

	public ValueCondition() {
		this(new ReentrantLock());
	}

	public ValueCondition(Lock lock) {
		this.lock = lock;
		condition = lock.newCondition();
	}

	public void signal(T value) {
		ConcurrentUtil.execute(lock, () -> {
			this.value = value;
			condition.signal();
		});
	}

	public T await() throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			while (value == null)
				condition.await();
			T returnValue = value;
			value = null;
			return returnValue;
		});
	}

	public T await(long timeoutMs) throws InterruptedException {
		return ConcurrentUtil.executeGet(lock, () -> {
			if (value == null)
				condition.await(timeoutMs, TimeUnit.MILLISECONDS);
			T returnValue = value;
			value = null;
			return returnValue;
		});
	}

	public T value() {
		return ConcurrentUtil.executeGet(lock, () -> value);
	}

}
