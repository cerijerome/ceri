package ceri.common.util;

import java.util.function.Consumer;

/**
 * Provides an AutoCloseable type for an object and a given close method on that object.
 */
public class Enclosed<T> implements AutoCloseable {
	private static final Enclosed<?> EMPTY = of(null, null);
	public final T subject;
	private final Consumer<T> closer;

	/**
	 * Return an empty instance.
	 */
	public static <T> Enclosed<T> empty() {
		return BasicUtil.uncheckedCast(EMPTY);
	}

	/**
	 * Create an instance that has no close operation.
	 */
	public static <T> Enclosed<T> noOp(T subject) {
		return of(subject, null);
	}

	public static <T> Enclosed<T> of(T subject, Consumer<T> closer) {
		return new Enclosed<>(subject, closer);
	}

	private Enclosed(T subject, Consumer<T> closer) {
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
	public void close() {
		if (subject == null || closer == null) return;
		closer.accept(subject);
	}

}
