package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToByteFunction<E extends Exception, T> {
	byte applyAsByte(T value) throws E;

	default ToByteFunction<T> asToByteFunction() {
		return t -> RUNTIME.getByte(() -> applyAsByte(t));
	}

	static <T> ExceptionToByteFunction<RuntimeException, T> of(ToByteFunction<T> fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsByte;
	}
}
