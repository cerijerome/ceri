package ceri.jna.util;

import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.sun.jna.Memory;
import ceri.common.concurrent.Concurrent;
import ceri.common.function.Functions;
import ceri.common.util.Validate;

/**
 * A class that provides resizable thread-local buffers.
 */
public class ThreadBuffers implements Functions.Closeable {
	public static final int SIZE_DEF = 1024;
	private final WeakHashMap<Thread, Memory> buffers = new WeakHashMap<>();
	private final Lock lock;
	private volatile long size = SIZE_DEF;

	public static ThreadBuffers of() {
		return of(SIZE_DEF);
	}

	public static ThreadBuffers of(long size) {
		return of(new ReentrantLock(), size);
	}

	public static ThreadBuffers of(Lock lock, long size) {
		return new ThreadBuffers(lock, size);
	}

	private ThreadBuffers(Lock lock, long size) {
		this.lock = lock;
		this.size = size;
	}

	/**
	 * Get buffer size.
	 */
	public long size() {
		return size;
	}

	/**
	 * Set future buffer sizes.
	 */
	public void size(long size) {
		Validate.min(size, 1);
		this.size = size;
	}

	/**
	 * Get the current buffer.
	 */
	@SuppressWarnings("resource")
	public Memory get() {
		var size = this.size;
		return Concurrent.lockedGet(lock, () -> buffers.compute(Thread.currentThread(),
			(_, m) -> (m != null && m.size() == size && m.valid()) ? m : new Memory(size)));
	}

	/**
	 * Remove the buffer for the current thread.
	 */
	public void remove() {
		@SuppressWarnings("resource")
		var m = Concurrent.lockedGet(lock, () -> buffers.remove(Thread.currentThread()));
		JnaUtil.close(m);
	}

	/**
	 * Removes all buffers, and relies on gc to free memory.
	 */
	@Override
	public void close() {
		Concurrent.lockedRun(lock, buffers::clear);
	}
}
