package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToBooleanFunction<E extends Exception, T> {
	boolean applyAsBoolean(T value) throws E;

	default ToBooleanFunction<T> asToBooleanFunction() {
		return t -> RUNTIME.getBoolean(() -> applyAsBoolean(t));
	}

	static <T> ExceptionToBooleanFunction<RuntimeException, T> of(ToBooleanFunction<T> fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsBoolean;
	}
}
