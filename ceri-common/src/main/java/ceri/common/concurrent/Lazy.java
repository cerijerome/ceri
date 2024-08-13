package ceri.common.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.util.BasicUtil;

/**
 * Utility for lazily instantiation. Instantiation may use a lock to ensure computation occurs once
 * only, or as an unsafe instance, which may instantiate more than once if called concurrently. Can
 * be created with a supplier, or with a supplier passed in on access.
 */
public class Lazy<T> {
	private final Lock lock;
	private volatile T value;

	/**
	 * Instantiates a value using a given supplier.
	 */
	public static interface Function<E extends Exception, T> {
		/**
		 * Gets the value, instantiating on first call using the given supplier.
		 */
		T get(ExceptionSupplier<E, T> supplier) throws E;

		/**
		 * Instantiates on first call using the given supplier.
		 */
		default void init(ExceptionSupplier<E, T> supplier) throws E {
			get(supplier);
		}

		/**
		 * Returns the current value without instantiating.
		 */
		T value();
	}

	/**
	 * Instantiates a value.
	 */
	public static interface Supplier<E extends Exception, T> {
		/**
		 * Gets the value, instantiating on first call.
		 */
		T get() throws E;

		/**
		 * Instantiates on first call.
		 */
		default void init() throws E {
			get();
		}

		/**
		 * Returns the current value without instantiating.
		 */
		T value();
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
		var cc = new Lazy<T>(lock);
		return new Function<>() {
			@Override
			public T get(ExceptionSupplier<E, T> supplier) throws E {
				return cc.get(supplier);
			}

			@Override
			public T value() {
				return cc.value;
			}
		};
	}

	/**
	 * Provides a lazily instantiated constant using given supplier.
	 */
	public static <E extends Exception, T> Supplier<E, T> of(ExceptionSupplier<E, T> supplier) {
		return of(new ReentrantLock(), supplier);
	}

	/**
	 * Provides a lazily instantiated constant using given supplier.
	 */
	public static <E extends Exception, T> Supplier<E, T> of(Lock lock,
		ExceptionSupplier<E, T> supplier) {
		Objects.requireNonNull(lock);
		return supplier(lock, supplier);
	}

	/**
	 * Provides a lazily-instantiated constant using the given supplier. Does not use a lock, so the
	 * supplier may be called more than once.
	 */
	public static <E extends Exception, T> Supplier<E, T> unsafe(ExceptionSupplier<E, T> supplier) {
		return supplier(null, supplier);
	}

	private static <E extends Exception, T> Supplier<E, T> supplier(Lock lock,
		ExceptionSupplier<E, T> supplier) {
		Objects.requireNonNull(supplier);
		var cc = new Lazy<T>(lock);
		return new Supplier<>() {
			@Override
			public T get() throws E {
				return cc.get(supplier);
			}

			@Override
			public T value() {
				return cc.value;
			}
		};
	}

	private Lazy(Lock lock) {
		this.lock = lock;
	}

	private <E extends Exception> T get(ExceptionSupplier<E, T> supplier) throws E {
		if (value == null) try (var x = locker()) { // double-checked locking
			value = BasicUtil.defaultValue(value, supplier);
		}
		return value;
	}

	private RuntimeCloseable locker() {
		if (lock != null) return ConcurrentUtil.locker(lock);
		return RuntimeCloseable.NULL;
	}
}
