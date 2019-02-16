package ceri.common.function;

import java.util.function.BiFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionBiFunction<E extends Exception, T, U, R> {
	R apply(T t, U u) throws E;

	default BiFunction<T, U, R> asBiFunction() {
		return (t, u) -> {
			try {
				return apply(t, u);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T, U, R> ExceptionBiFunction<RuntimeException, T, U, R> of(BiFunction<T, U, R> fn) {
		return (t, u) -> fn.apply(t, u);
	}
}