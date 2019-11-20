package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.function.ToIntFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToIntFunction<E extends Exception, T> {
	int applyAsInt(T value)throws E;

	default ToIntFunction<T> asToIntFunction() {
		return t -> RUNTIME.getInt(() -> applyAsInt(t));
	}

	static <T> ExceptionToIntFunction<RuntimeException, T> of(ToIntFunction<T> fn) {
		return fn::applyAsInt;
	}
}
