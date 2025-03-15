package ceri.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionCloseable;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;

/**
 * Provides an AutoCloseable type encapsulating an object reference and a close method.
 */
public class Enclosed<E extends Exception, T> implements ExceptionCloseable<E> {
	private static final Enclosed<?, ?> EMPTY = new Enclosed<>(null, null);
	public final T ref;
	private final ExceptionRunnable<E> closer;

	/**
	 * A wrapper type that can be repeatedly initialized and closed.
	 */
	public static class Repeater<E extends Exception, T>
		implements ExceptionCloseable<E>, ExceptionSupplier<E, T> {
		private final Lock lock;
		private final ExceptionSupplier<E, Enclosed<E, T>> constructor;
		private volatile Enclosed<E, T> enc = null;
		private volatile T ref = null;

		/**
		 * Creates an instance without using a lock. Not thread-safe.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			unsafe(ExceptionSupplier<E, Enclosed<E, T>> constructor) {
			return of(null, constructor);
		}

		/**
		 * Creates an instance using a new lock.
		 */
		public static <E extends Exception, T> Repeater<E, T>
			of(ExceptionSupplier<E, Enclosed<E, T>> constructor) {
			return of(new ReentrantLock(), constructor);
		}

		/**
		 * Creates an instance using the given lock. If the lock is null, it behaves as unsafe.
		 */
		public static <E extends Exception, T> Repeater<E, T> of(Lock lock,
			ExceptionSupplier<E, Enclosed<E, T>> constructor) {
			return new Repeater<>(lock, constructor);
		}

		private Repeater(Lock lock, ExceptionSupplier<E, Enclosed<E, T>> constructor) {
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

		private RuntimeCloseable locker() {
			if (lock != null) return ConcurrentUtil.locker(lock);
			return RuntimeCloseable.NULL;
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
		ExceptionCloseable<E> closeable) {
		return of(subject, _ -> closeable.close());
	}

	/**
	 * Adapts a closeable type to a new enclosed type; closing the type on close or failure.
	 */
	public static <E extends Exception, T extends ExceptionCloseable<E>, R> Enclosed<E, R>
		adaptOrClose(T subject, ExceptionFunction<E, T, R> adapter) throws E {
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
	public static <E extends Exception, T extends ExceptionCloseable<E>> Enclosed<E, T>
		of(T subject) {
		return from(subject, subject);
	}

	/**
	 * Create an instance with a subject, and a close method. If the subject is null, the close
	 * method is not executed.
	 */
	public static <E extends Exception, T, U extends T> Enclosed<E, T> of(U subject,
		ExceptionConsumer<E, ? super U> closer) {
		if (subject == null) return BasicUtil.uncheckedCast(EMPTY);
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

	private Enclosed(T subject, ExceptionRunnable<E> closer) {
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
