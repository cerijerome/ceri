package ceri.common.function;

import java.util.function.Function;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionFunction<E extends Exception, T, R> {
	R apply(T t) throws E;

	default Function<T, R> asFunction() {
		return t -> {
			try {
				return apply(t);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T, R> ExceptionFunction<RuntimeException, T, R> of(Function<T, R> fn) {
		return t -> fn.apply(t);
	}
}
