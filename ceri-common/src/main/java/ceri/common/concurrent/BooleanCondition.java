package ceri.common.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple condition to signal and wait for a boolean state change.
 */
public class BooleanCondition {
	private final Lock lock;
	private final Condition condition;
	private boolean state = false;
	
	public BooleanCondition() {
		this(new ReentrantLock());
	}
	
	public BooleanCondition(Lock lock) {
		this.lock = lock;
		condition = lock.newCondition();
	}
	
	public void signal() {
		lock.lock();
		try {
			state = true;
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public void await() throws InterruptedException {
		lock.lock();
		try {
			while (!state) condition.await();
			state = false;
		} finally {
			lock.unlock();
		}
	}
	
}
