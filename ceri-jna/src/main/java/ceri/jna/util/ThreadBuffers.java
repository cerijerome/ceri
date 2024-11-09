package ceri.jna.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.sun.jna.Memory;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.RuntimeCloseable;

/**
 * A class that provides resizable thread-local buffers.
 */
public class ThreadBuffers implements RuntimeCloseable {
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
		validateMin(size, 1);
		this.size = size;
	}

	/**
	 * Get the current buffer.
	 */
	@SuppressWarnings("resource")
	public Memory get() {
		var size = this.size;
		return ConcurrentUtil.lockedGet(lock, () -> buffers.compute(Thread.currentThread(),
			(t, m) -> (m != null && m.size() == size && m.valid()) ? m : new Memory(size)));
	}

	/**
	 * Remove the buffer for the current thread.
	 */
	public void remove() {
		@SuppressWarnings("resource")
		var m = ConcurrentUtil.lockedGet(lock, () -> buffers.remove(Thread.currentThread()));
		JnaUtil.close(m);
	}

	/**
	 * Removes all buffers, and relies on gc to free memory.
	 */
	@Override
	public void close() {
		ConcurrentUtil.lockedRun(lock, buffers::clear);
	}
}
