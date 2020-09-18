package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionBiFunction<E extends Exception, T, U, R> {
	R apply(T t, U u) throws E;

	default BiFunction<T, U, R> asBiFunction() {
		return (t, u) -> RUNTIME.get(() -> apply(t, u));
	}

	static <T, U, R> ExceptionBiFunction<RuntimeException, T, U, R> of(BiFunction<T, U, R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}