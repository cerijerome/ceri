package ceri.common.concurrent;

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
		lock.lock();
		try {
			this.value = value;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public T await() throws InterruptedException {
		lock.lock();
		try {
			while (value == null) condition.await();
			T returnValue = value;
			value = null;
			return returnValue;
		} finally {
			lock.unlock();
		}
	}
	
	public T value() {
		lock.lock();
		try {
			return value;
		} finally {
			lock.unlock();
		}
	}
	
}
