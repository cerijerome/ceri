package ceri.common.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;

/**
 * Lazily-instantiated constant, that only computes the value once.
 */
public class Constant<T> {
	private final Lock lock;
	private volatile T value;

	/**
	 * Gets the value, instantiating on first call using the given supplier.
	 */
	public static interface Function<E extends Exception, T> {
		T get(ExceptionSupplier<E, T> supplier) throws E;
	}

	/**
	 * Provides a lazily-instantiated constant that requires a supplier.
	 */
	public static <E extends Exception, T> Function<E, T> of() {
		return of(new ReentrantLock());
	}

	/**
	 * Provides a lazily-instantiated constant that requires a supplier.
	 */
	public static <E extends Exception, T> Function<E, T> of(Lock lock) {
		Objects.requireNonNull(lock);
		return function(lock);
	}

	/**
	 * Provides a lazily-instantiated constant that requires a supplier. Does not use a lock, so the
	 * supplier may be called more than once.
	 */
	public static <E extends Exception, T> Function<E, T> unsafe() {
		return function(null);
	}

	private static <E extends Exception, T> Function<E, T> function(Lock lock) {
		var cc = new Constant<T>(lock);
		return supplier -> cc.get(supplier);
	}

	/**
	 * Provides a lazily instantiated constant using given supplier.
	 */
	public static <E extends Exception, T> ExceptionSupplier<E, T>
		of(ExceptionSupplier<E, T> supplier) {
		return of(new ReentrantLock(), supplier);
	}

	/**
	 * Provides a lazily instantiated constant using given supplier.
	 */
	public static <E extends Exception, T> ExceptionSupplier<E, T> of(Lock lock,
		ExceptionSupplier<E, T> supplier) {
		Objects.requireNonNull(lock);
		return supplier(lock, supplier);
	}

	/**
	 * Provides a lazily-instantiated constant using the given supplier. Does not use a lock, so the
	 * supplier may be called more than once.
	 */
	public static <E extends Exception, T> ExceptionSupplier<E, T>
		unsafe(ExceptionSupplier<E, T> supplier) {
		return supplier(null, supplier);
	}

	private static <E extends Exception, T> ExceptionSupplier<E, T> supplier(Lock lock,
		ExceptionSupplier<E, T> supplier) {
		Objects.requireNonNull(supplier);
		var cc = new Constant<T>(lock);
		return () -> cc.get(supplier);
	}

	private Constant(Lock lock) {
		this.lock = lock;
	}

	private <E extends Exception> T get(ExceptionSupplier<E, T> supplier) throws E {
		if (value == null) try (var x = locker()) {
			if (value == null) value = supplier.get();
		}
		return value;
	}

	private RuntimeCloseable locker() {
		if (lock != null) return ConcurrentUtil.locker(lock);
		return RuntimeCloseable.NULL;
	}
}
