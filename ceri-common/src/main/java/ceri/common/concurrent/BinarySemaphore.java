package ceri.common.concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import ceri.common.function.Excepts.RuntimeCloseable;

/**
 * A closable binary semaphore. Can be used to protect a long-running code block. Acquisition is
 * interruptible; may perform better than Lock.lockInterruptibly.
 */
public class BinarySemaphore implements AutoCloseable {
	private final Semaphore semaphore;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public static BinarySemaphore of() {
		return of(false);
	}

	public static BinarySemaphore of(boolean fair) {
		return new BinarySemaphore(fair);
	}

	private BinarySemaphore(boolean fair) {
		this.semaphore = new Semaphore(1, fair);
	}

	/**
	 * Acquires the permit for try-with-resource.
	 */
	public RuntimeCloseable acquirer() {
		acquire();
		return this::release;
	}

	/**
	 * Returns an approximate number of threads waiting for acquisition.
	 */
	public int waitingThreads() {
		return semaphore.getQueueLength();
	}

	/**
	 * Wait to acquire the permit. Throws RuntimeInterruptedException if interrupted,
	 * IllegalStateException if the semaphore is closed before acquiring the permit.
	 */
	public void acquire() {
		try {
			if (closed()) throw closedException();
			semaphore.acquire();
			if (!closed()) return;
			semaphore.release();
			throw closedException();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

	/**
	 * Releases the permit; does nothing if permit has not been acquired.
	 */
	public void release() {
		semaphore.release();
	}

	/**
	 * Returns true if the semaphore is not closed, and a permit is available.
	 */
	public boolean available() {
		return !closed() && semaphore.availablePermits() > 0;
	}

	public boolean closed() {
		return closed.get();
	}

	@Override
	public void close() {
		if (closed.getAndSet(true)) return;
		semaphore.release();
	}

	private RuntimeException closedException() {
		return new IllegalStateException("Semaphore is closed");
	}

}
