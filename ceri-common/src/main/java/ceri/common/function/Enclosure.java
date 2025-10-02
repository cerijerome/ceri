package ceri.common.function;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.concurrent.Concurrent;
import ceri.common.reflect.Reflect;

/**
 * Provides a reference enclosed in a closeable wrapper.
 */
public class Enclosure<T> implements Functions.Closeable {
	private static final Enclosure<?> EMPTY = new Enclosure<>(null, null);
	public final T ref;
	private final Functions.Runnable closer;

	/**
	 * A wrapper type that can be repeatedly initialized and closed.
	 */
	public static class Repeater<E extends Exception, T>
		implements Functions.Closeable, Excepts.Supplier<E, T> {
		private final Lock lock;
		private final Excepts.Supplier<E, Enclosure<T>> constructor;
		private volatile Enclosure<T> enc = null;
		private volatile T ref = null;

		/**
		 * Creates an instance without using a lock. Not thread-safe.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			unsafe(Excepts.Supplier<E, Enclosure<T>> constructor) {
			return of(null, constructor);
		}

		/**
		 * Creates an instance using a new lock.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			of(Excepts.Supplier<E, Enclosure<T>> constructor) {
			return of(new ReentrantLock(), constructor);
		}

		/**
		 * Creates an instance using the given lock. If the lock is null, it behaves as unsafe.
		 */
		public static <E extends Exception, T> Repeater<E, T> of(Lock lock,
			Excepts.Supplier<E, Enclosure<T>> constructor) {
			return new Repeater<>(lock, constructor);
		}

		private Repeater(Lock lock, Excepts.Supplier<E, Enclosure<T>> constructor) {
			this.lock = lock;
			this.constructor = constructor;
		}

		/**
		 * Closes any current reference, and creates a new reference.
		 */
		public T init() throws E {
			try (var _ = Concurrent.locker(lock)) {
				close();
				enc = constructor.get();
				ref = enc.ref;
				return ref;
			}
		}

		/**
		 * Returns the reference, creating a new reference if not initialized.
		 */
		@Override
		public T get() throws E {
			try (var _ = Concurrent.locker(lock)) {
				if (enc == null) init();
				return ref;
			}
		}

		/**
		 * Returns the current value of the reference, which may be null.
		 */
		public T ref() {
			return ref;
		}

		@Override
		public void close() {
			try (var _ = Concurrent.locker(lock)) {
				Closeables.close(enc);
				enc = null;
				ref = null;
			}
		}
	}

	/**
	 * Return an empty instance. Use of(null, null) for typed exception.
	 */
	public static <T> Enclosure<T> empty() {
		return of(null, null);
	}

	/**
	 * Create an instance that has no close operation. Use of(T, null) for typed exception.
	 */
	public static <T> Enclosure<T> noOp(T subject) {
		return of(subject, null);
	}

	/**
	 * Transforms a closeable function to an enclosed type.
	 */
	public static <T> Enclosure<T> from(T subject, AutoCloseable closeable) {
		return of(subject, _ -> Closeables.close(closeable));
	}

	/**
	 * Creates a type then wraps as an enclosed type.
	 */
	public static <E extends Exception, T, U extends T> Enclosure<T>
		from(Excepts.Supplier<E, ? extends U> constructor, Functions.Consumer<? super U> closer)
			throws E {
		return of(constructor.get(), closer);
	}

	/**
	 * Adapts a closeable type to a new enclosed type; closing the type on close or failure.
	 */
	public static <E extends Exception, T extends AutoCloseable, R> Enclosure<R> adapt(T subject,
		Excepts.Function<E, T, R> adapter) throws E {
		try {
			var result = adapter.apply(subject);
			return of(result, _ -> Closeables.close(subject));
		} catch (Exception e) {
			Closeables.close(subject);
			throw e;
		}
	}

	/**
	 * Create an instance of a closeable subject.
	 */
	public static <T extends AutoCloseable> Enclosure<T> of(T subject) {
		return from(subject, subject);
	}

	/**
	 * Create an instance with a subject, and a close method. If the subject is null, the close
	 * method is not executed.
	 */
	public static <T, U extends T> Enclosure<T> of(U subject,
		Functions.Consumer<? super U> closer) {
		if (subject == null) return Reflect.unchecked(EMPTY);
		if (closer == null) return new Enclosure<>(subject, null);
		return new Enclosure<>(subject, () -> closer.accept(subject));
	}

	/**
	 * Create an instance with a collection of AutoCloseables. The collection is closed in reverse
	 * order.
	 */
	@SafeVarargs
	public static <T extends AutoCloseable> Enclosure<List<T>> ofAll(T... closeables) {
		return ofAll(Arrays.asList(closeables));
	}

	/**
	 * Create an instance with a collection of AutoCloseables. The collection is closed in reverse
	 * order.
	 */
	public static <T extends AutoCloseable> Enclosure<List<T>> ofAll(List<T> closeables) {
		return new Enclosure<>(closeables, () -> Closeables.closeReversed(closeables));
	}

	private Enclosure(T subject, Functions.Runnable closer) {
		this.ref = subject;
		this.closer = closer;
	}

	/**
	 * Returns true if this instance has no subject.
	 */
	public boolean isEmpty() {
		return ref == null;
	}

	/**
	 * Returns true if this instance has no close action.
	 */
	public boolean isNoOp() {
		return closer == null;
	}

	@Override
	public void close() {
		if (closer != null) closer.run();
	}

	@Override
	public String toString() {
		return "[" + ref + "]";
	}
}
