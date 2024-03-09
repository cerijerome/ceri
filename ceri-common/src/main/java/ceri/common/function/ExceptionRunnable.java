package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	static ExceptionRunnable<RuntimeException> NULL = () -> {};

	void run() throws E;

	default Runnable asRunnable() {
		return () -> RUNTIME.run(this);
	}

	static ExceptionRunnable<RuntimeException> of(Runnable runnable) {
		Objects.requireNonNull(runnable);
		return runnable::run;
	}

	/**
	 * Converts a runnable to a function that ignores input and returns the given result.
	 */
	static <E extends Exception, T, R> ExceptionFunction<E, T, R>
		asFunction(ExceptionRunnable<E> runnable, R result) {
		return t -> {
			runnable.run();
			return result;
		};
	}

	/**
	 * Converts a runnable to a supplier that returns the given value.
	 */
	static <E extends Exception, T> ExceptionSupplier<E, T>
		asSupplier(ExceptionRunnable<E> runnable, T t) {
		return () -> {
			runnable.run();
			return t;
		};
	}
}