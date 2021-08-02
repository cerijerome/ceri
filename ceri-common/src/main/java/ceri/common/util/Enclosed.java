package ceri.common.util;

import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;

/**
 * Provides an AutoCloseable type for an object and a given close method on that object.
 */
public class Enclosed<E extends Exception, T> implements AutoCloseable {
	private static final Enclosed<RuntimeException, ?> EMPTY = of(null, null);
	public final T subject;
	private final ExceptionConsumer<E, T> closer;

	/**
	 * Return an empty instance.
	 */
	public static <T> Enclosed<RuntimeException, T> empty() {
		return BasicUtil.uncheckedCast(EMPTY);
	}

	/**
	 * Create an instance that has no close operation.
	 */
	public static <T> Enclosed<RuntimeException, T> noOp(T subject) {
		return of(subject, null);
	}

	public static <E extends Exception, T> Enclosed<E, T> of(T subject,
		ExceptionConsumer<E, T> closer) {
		return new Enclosed<>(subject, closer);
	}

	/**
	 * Create an instance with no subject, just a close method.
	 */
	public static <E extends Exception> Enclosed<E, ?> of(ExceptionRunnable<E> closer) {
		return new Enclosed<>(Boolean.TRUE, x -> closer.run());
	}

	private Enclosed(T subject, ExceptionConsumer<E, T> closer) {
		this.subject = subject;
		this.closer = closer;
	}

	/**
	 * Returns true if this instance has no subject.
	 */
	public boolean isEmpty() {
		return subject == null;
	}

	/**
	 * Returns true if this instance has no close action.
	 */
	public boolean isNoOp() {
		return closer == null || isEmpty();
	}

	@Override
	public void close() throws E {
		if (subject == null || closer == null) return;
		closer.accept(subject);
	}

}
