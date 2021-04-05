package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.util.Enclosed;

/**
 * Encapsulate a lock, handling locking as a closeable resource.
 */
public class Locker {
	private final Enclosed<Lock> unlocker;
	public final Lock lock;

	public static Locker of(Lock lock) {
		return new Locker(lock);
	}

	public static Locker of() {
		return of(new ReentrantLock());
	}

	private Locker(Lock lock) {
		this.lock = lock;
		unlocker = Enclosed.of(lock, Lock::unlock);
	}

	/**
	 * Lock, and return a resource that executes unlock on close.
	 */
	public Enclosed<Lock> lock() {
		lock.lock();
		return unlocker;
	}
}
