package ceri.common.concurrent;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.Basics;

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
		T get(Excepts.Supplier<E, T> supplier) throws E;

		/**
		 * Instantiates on first call using the given supplier.
		 */
		default void init(Excepts.Supplier<E, T> supplier) throws E {
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
	public static interface Supplier<E extends Exception, T> extends Excepts.Supplier<E, T> {
		/**
		 * Gets the value, instantiating on first call.
		 */
		@Override
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
	 * A lazily instantiated value, with temporary override. The value can be manually initialized
	 * if called before get().
	 */
	public static class Value<E extends Exception, T> implements Excepts.Supplier<E, T> {
		private final Excepts.Supplier<E, T> supplier;
		private final Lazy.Function<E, T> lazy;
		private volatile T override;

		/**
		 * Create an instance using the initializer without a lock.
		 */
		public static <E extends Exception, T> Value<E, T> unsafe(Excepts.Supplier<E, T> supplier) {
			return new Value<>(Lazy.unsafe(), supplier);
		}

		/**
		 * Create an instance using the fixed value.
		 */
		public static <T> Value<RuntimeException, T> of(T value) {
			return unsafe(() -> value);
		}

		/**
		 * Create an instance using the initializer.
		 */
		public static <E extends Exception, T> Value<E, T> of(Excepts.Supplier<E, T> supplier) {
			return of(new ReentrantLock(), supplier);
		}

		/**
		 * Create an instance using the initializer and lock.
		 */
		public static <E extends Exception, T> Value<E, T> of(Lock lock,
			Excepts.Supplier<E, T> supplier) {
			return new Value<>(Lazy.of(lock), supplier);
		}

		private Value(Lazy.Function<E, T> lazy, Excepts.Supplier<E, T> supplier) {
			this.lazy = lazy;
			this.supplier = supplier;
		}

		/**
		 * Returns the override if set, otherwise the initialized value, initializing if needed.
		 */
		@Override
		public T get() throws E {
			return Basics.def(override, this::value);
		}

		/**
		 * Initializes the value using the value, if not yet initialized.
		 */
		public T init(T value) throws E {
			return lazy.get(() -> value);
		}

		/**
		 * Temporary override. Close to stop the override.
		 */
		public Functions.Closeable override(T override) {
			this.override = override;
			return () -> this.override = null;
		}

		/**
		 * Returns the initialized value ignoring any override, initializing if needed.
		 */
		public T value() throws E {
			return lazy.get(supplier);
		}
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
			public T get(Excepts.Supplier<E, T> supplier) throws E {
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
	public static <E extends Exception, T> Supplier<E, T> of(Excepts.Supplier<E, T> supplier) {
		return of(new ReentrantLock(), supplier);
	}

	/**
	 * Provides a lazily instantiated constant using given supplier.
	 */
	public static <E extends Exception, T> Supplier<E, T> of(Lock lock,
		Excepts.Supplier<E, T> supplier) {
		Objects.requireNonNull(lock);
		return supplier(lock, supplier);
	}

	/**
	 * Provides a lazily-instantiated constant using the given supplier. Does not use a lock, so the
	 * supplier may be called more than once.
	 */
	public static <E extends Exception, T> Supplier<E, T> unsafe(Excepts.Supplier<E, T> supplier) {
		return supplier(null, supplier);
	}

	private static <E extends Exception, T> Supplier<E, T> supplier(Lock lock,
		Excepts.Supplier<E, T> supplier) {
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

	private <E extends Exception> T get(Excepts.Supplier<E, T> supplier) throws E {
		if (value == null) try (var _ = ConcurrentUtil.locker(lock)) { // double-checked locking
			value = Basics.def(value, supplier);
		}
		return value;
	}
}
