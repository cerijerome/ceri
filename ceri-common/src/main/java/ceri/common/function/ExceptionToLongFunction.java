package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.ToLongFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToLongFunction<E extends Exception, T> {
	long applyAsLong(T value) throws E;

	default ToLongFunction<T> asToLongFunction() {
		return t -> RUNTIME.getLong(() -> applyAsLong(t));
	}

	static <T> ExceptionToLongFunction<RuntimeException, T> of(ToLongFunction<T> fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsLong;
	}
}
