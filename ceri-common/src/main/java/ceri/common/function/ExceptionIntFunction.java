package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
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
		return fn::apply;
	}
}
