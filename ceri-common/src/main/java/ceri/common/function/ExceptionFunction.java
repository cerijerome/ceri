package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.Function;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionFunction<E extends Exception, T, R> {
	R apply(T t) throws E;

	default Function<T, R> asFunction() {
		return t -> RUNTIME.get(() -> apply(t));
	}

	static <T, R> ExceptionFunction<RuntimeException, T, R> of(Function<T, R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
