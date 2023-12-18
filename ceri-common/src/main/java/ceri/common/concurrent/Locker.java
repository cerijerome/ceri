package ceri.common.concurrent;

import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.ExceptionCloseable;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionLongSupplier;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.util.Holder;

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
	 * Lock, and return a resource that executes unlock on close. Executes given post-lock and
	 * pre-unlock logic, making sure the lock is unlocked if an exception occurs.
	 */
	public <E extends Exception> ExceptionCloseable<E> lock(ExceptionRunnable<E> postLock,
		ExceptionRunnable<E> preUnlock) throws E {
		return ConcurrentUtil.locker(lock, postLock, preUnlock);
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public <E extends Exception, T> T get(ExceptionSupplier<E, T> supplier) throws E {
		return ConcurrentUtil.lockedGet(lock, supplier);
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public <E extends Exception> int getAsInt(ExceptionIntSupplier<E> supplier) throws E {
		return ConcurrentUtil.lockedGetAsInt(lock, supplier);
	}

	/**
	 * Executes the operation within the lock and returns the result.
	 */
	public <E extends Exception> long getAsLong(ExceptionLongSupplier<E> supplier) throws E {
		return ConcurrentUtil.lockedGetAsLong(lock, supplier);
	}

	/**
	 * Executes the operation within the lock.
	 */
	public <E extends Exception> void run(ExceptionRunnable<E> runnable) throws E {
		ConcurrentUtil.lockedRun(lock, runnable);
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public <E extends Exception, T> Holder<T> tryGet(ExceptionSupplier<E, T> supplier) throws E {
		return ConcurrentUtil.tryLockedGet(lock, supplier);
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public <E extends Exception> OptionalInt tryGetAsInt(ExceptionIntSupplier<E> supplier)
		throws E {
		return ConcurrentUtil.tryLockedGetAsInt(lock, supplier);
	}

	/**
	 * Tries to execute the operation within the lock and return the result as a value holder. The
	 * holder is empty if the lock is not available.
	 */
	public <E extends Exception> OptionalLong tryGetAsLong(ExceptionLongSupplier<E> supplier)
		throws E {
		return ConcurrentUtil.tryLockedGetAsLong(lock, supplier);
	}

	/**
	 * Tries to execute the operation within the lock. Returns false if the lock is not available.
	 */
	public <E extends Exception> boolean tryRun(ExceptionRunnable<E> runnable) throws E {
		return ConcurrentUtil.tryLockedRun(lock, runnable);
	}

	/**
	 * Create a new condition from the lock.
	 */
	public Condition condition() {
		return lock.newCondition();
	}
}
