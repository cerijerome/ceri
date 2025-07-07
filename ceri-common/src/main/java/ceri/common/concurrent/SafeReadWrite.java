package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import ceri.common.function.Excepts;

public class SafeReadWrite {
	public final ReadWriteLock lock;

	public static SafeReadWrite of() {
		return of(false);
	}

	public static SafeReadWrite of(boolean fair) {
		return new SafeReadWrite(new ReentrantReadWriteLock(fair));
	}

	private SafeReadWrite(ReadWriteLock lock) {
		this.lock = lock;
	}

	public Lock conditionLock() {
		return lock.writeLock();
	}

	public <E extends Exception, T> T read(Excepts.Supplier<E, T> supplier) throws E {
		return ConcurrentUtil.lockedGet(lock.readLock(), supplier);
	}

	public <E extends Exception> void readNoReturn(Excepts.Runnable<E> runnable) throws E {
		ConcurrentUtil.lockedRun(lock.readLock(), runnable);
	}

	public <E extends Exception> void write(Excepts.Runnable<E> runnable) throws E {
		ConcurrentUtil.lockedRun(lock.writeLock(), runnable);
	}

	public <E extends Exception, T> T writeWithReturn(Excepts.Supplier<E, T> supplier) throws E {
		return ConcurrentUtil.lockedGet(lock.writeLock(), supplier);
	}

}
