package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToDoubleFunction<E extends Exception, T> {
	double applyAsDouble(T value) throws E;

	default ToDoubleFunction<T> asToDoubleFunction() {
		return t -> RUNTIME.getDouble(() -> applyAsDouble(t));
	}

	static <T> ExceptionToDoubleFunction<RuntimeException, T> of(ToDoubleFunction<T> fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsDouble;
	}
}
