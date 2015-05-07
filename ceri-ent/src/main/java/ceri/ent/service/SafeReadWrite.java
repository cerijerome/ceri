package ceri.ent.service;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.concurrent.ExceptionSupplier;

public class SafeReadWrite {
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	public <E extends Exception, T> T read(ExceptionSupplier<E, T> supplier) throws E {
		return ConcurrentUtil.executeGet(lock.readLock(), supplier);
	}

	public <E extends Exception> void write(ExceptionRunnable<E> runnable) throws E {
		ConcurrentUtil.execute(lock.writeLock(), runnable);
	}

	public <E extends Exception, T> T writeWithReturn(ExceptionSupplier<E, T> supplier) throws E {
		return ConcurrentUtil.executeGet(lock.writeLock(), supplier);
	}

}
