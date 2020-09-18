package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.LongFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionLongFunction<E extends Exception, R> {
	R apply(long value) throws E;

	default LongFunction<R> asLongFunction() {
		return i -> RUNTIME.get(() -> apply(i));
	}

	static <R> ExceptionLongFunction<RuntimeException, R> of(LongFunction<R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
