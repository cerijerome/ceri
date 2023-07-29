package ceri.common.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.RuntimeCloseable;

/**
 * Encapsulate a lock, handling locking as a closeable resource.
 */
public class Locker {
	private final RuntimeCloseable unlocker;
	public final Lock lock;

	public static Locker of(Lock lock) {
		return new Locker(lock);
	}

	public static Locker of() {
		return of(new ReentrantLock());
	}

	private Locker(Lock lock) {
		this.lock = lock;
		unlocker = () -> lock.unlock();
	}

	/**
	 * Lock, and return a resource that executes unlock on close.
	 */
	public RuntimeCloseable lock() {
		lock.lock();
		return unlocker;
	}

	/**
	 * Create a new condition from the lock.
	 */
	public Condition condition() {
		return lock.newCondition();
	}

}
