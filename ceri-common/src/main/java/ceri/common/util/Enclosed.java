package ceri.common.util;

import java.util.Arrays;
import java.util.List;
import ceri.common.function.ExceptionCloseable;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;

/**
 * Provides an AutoCloseable type encapsulating an object reference and a close method.
 */
public class Enclosed<E extends Exception, T> implements ExceptionCloseable<E> {
	private static final Enclosed<?, ?> EMPTY = new Enclosed<>(null, null);
	public final T ref;
	private final ExceptionRunnable<E> closer;

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
		return of(subject, x -> closeable.close());
	}

	/**
	 * Create an instance with a subject, and a close method. If the subject is null, the close
	 * method is not executed.
	 */
	public static <E extends Exception, T> Enclosed<E, T> of(T subject,
		ExceptionConsumer<E, T> closer) {
		if (subject == null) return BasicUtil.uncheckedCast(EMPTY);
		if (closer == null) return new Enclosed<>(subject, null);
		return new Enclosed<>(subject, () -> closer.accept(subject));
	}

	/**
	 * Create an instance with no subject, just a close method.
	 */
	public static <E extends Exception, T> Enclosed<E, T> of(ExceptionRunnable<E> closer) {
		return new Enclosed<>((T) null, closer);
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

}
