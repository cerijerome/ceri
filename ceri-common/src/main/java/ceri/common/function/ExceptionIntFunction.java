package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionIntFunction<E extends Exception, R> {
	R apply(int value) throws E;

	default IntFunction<R> asIntFunction() {
		return i -> RUNTIME.get(() -> apply(i));
	}

	static <R> ExceptionIntFunction<RuntimeException, R> of(IntFunction<R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
