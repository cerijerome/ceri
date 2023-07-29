package ceri.common.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.ExceptionSupplier;

/**
 * Simple class that only computes the value once.
 */
public class ComputedConstant<E extends Exception, T> {
	private final Lock lock = new ReentrantLock();
	private final ExceptionSupplier<E, T> supplier;
	private volatile T value;

	public static <E extends Exception, T> ComputedConstant<E, T>
		of(ExceptionSupplier<E, T> supplier) {
		return new ComputedConstant<>(supplier);
	}

	private ComputedConstant(ExceptionSupplier<E, T> supplier) {
		this.supplier = supplier;
	}

	public T get() throws E {
		if (value == null) try (var x = ConcurrentUtil.locker(lock)) {
			if (value == null) value = supplier.get();
		}
		return value;
	}
}
