package ceri.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;

/**
 * Provides an AutoCloseable type encapsulating an object reference and a close method.
 */
public class Enclosed<E extends Exception, T> implements Excepts.Closeable<E> {
	private static final Enclosed<?, ?> EMPTY = new Enclosed<>(null, null);
	public final T ref;
	private final Excepts.Runnable<E> closer;

	/**
	 * A wrapper type that can be repeatedly initialized and closed.
	 */
	public static class Repeater<E extends Exception, T>
		implements Excepts.Closeable<E>, Excepts.Supplier<E, T> {
		private final Lock lock;
		private final Excepts.Supplier<E, Enclosed<E, T>> constructor;
		private volatile Enclosed<E, T> enc = null;
		private volatile T ref = null;

		/**
		 * Creates an instance without using a lock. Not thread-safe.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			unsafe(Excepts.Supplier<E, Enclosed<E, T>> constructor) {
			return of(null, constructor);
		}

		/**
		 * Creates an instance using a new lock.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			of(Excepts.Supplier<E, Enclosed<E, T>> constructor) {
			return of(new ReentrantLock(), constructor);
		}

		/**
		 * Creates an instance using the given lock. If the lock is null, it behaves as unsafe.
		 */
		public static <E extends Exception, T> Repeater<E, T> of(Lock lock,
			Excepts.Supplier<E, Enclosed<E, T>> constructor) {
			return new Repeater<>(lock, constructor);
		}

		private Repeater(Lock lock, Excepts.Supplier<E, Enclosed<E, T>> constructor) {
			this.lock = lock;
			this.constructor = constructor;
		}

		/**
		 * Closes any current reference, and creates a new reference.
		 */
		public T init() throws E {
			try (var _ = locker()) {
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
			try (var _ = locker()) {
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
		public void close() throws E {
			try (var _ = locker()) {
				CloseableUtil.close(enc);
				enc = null;
				ref = null;
			}
		}

		private Functions.Closeable locker() {
			if (lock != null) return ConcurrentUtil.locker(lock);
			return Functions.Closeable.NULL;
		}
	}

	/**
	 * Return an empty instance. Use of(null, null) for typed exception.
	 */
	public static <T> Enclosed<RuntimeException, T> empty() {
		return of(null, null);
	}

	/**
	 * Create an instance that has no close operation. Use of(T, null) for typed exception.
	 */
	public static <T> Enclosed<RuntimeException, T> noOp(T subject) {
		return of(subject, null);
	}

	/**
	 * Transforms a closeable function to an enclosed type.
	 */
	public static <E extends Exception, T> Enclosed<E, T> from(T subject,
		Excepts.Closeable<E> closeable) {
		return of(subject, _ -> closeable.close());
	}

	/**
	 * Creates a type then wraps as an enclosed type.
	 */
	public static <E extends Exception, F extends Exception, T, U extends T> Enclosed<F, T>
		from(Excepts.Supplier<E, ? extends U> constructor, Excepts.Consumer<F, ? super U> closer)
			throws E {
		return of(constructor.get(), closer);
	}

	/**
	 * Adapts a closeable type to a new enclosed type; closing the type on close or failure.
	 */
	public static <E extends Exception, T extends Excepts.Closeable<E>, R> Enclosed<E, R>
		adaptOrClose(T subject, Excepts.Function<E, T, R> adapter) throws E {
		try {
			var result = adapter.apply(subject);
			return of(result, _ -> subject.close());
		} catch (Exception e) {
			subject.close();
			throw e;
		}
	}

	/**
	 * Create an instance of a closeable subject.
	 */
	public static <E extends Exception, T extends Excepts.Closeable<E>> Enclosed<E, T>
		of(T subject) {
		return from(subject, subject);
	}

	/**
	 * Create an instance with a subject, and a close method. If the subject is null, the close
	 * method is not executed.
	 */
	public static <E extends Exception, T, U extends T> Enclosed<E, T> of(U subject,
		Excepts.Consumer<E, ? super U> closer) {
		if (subject == null) return BasicUtil.unchecked(EMPTY);
		if (closer == null) return new Enclosed<>(subject, null);
		return new Enclosed<>(subject, () -> closer.accept(subject));
	}

	/**
	 * Create an instance with a collection of AutoCloseables. The collection is closed in reverse
	 * order.
	 */
	@SafeVarargs
	public static <T extends AutoCloseable> Enclosed<RuntimeException, List<T>>
		ofAll(T... closeables) {
		return ofAll(Arrays.asList(closeables));
	}

	/**
	 * Create an instance with a collection of AutoCloseables. The collection is closed in reverse
	 * order.
	 */
	public static <T extends AutoCloseable> Enclosed<RuntimeException, List<T>>
		ofAll(List<T> closeables) {
		return new Enclosed<>(closeables, () -> CloseableUtil.closeReversed(closeables));
	}

	private Enclosed(T subject, Excepts.Runnable<E> closer) {
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
	public void close() throws E {
		if (closer != null) closer.run();
	}

	@Override
	public String toString() {
		return "[" + ref + "]";
	}
}
