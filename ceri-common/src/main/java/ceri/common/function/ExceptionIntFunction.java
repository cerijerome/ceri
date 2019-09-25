package ceri.common.function;

import java.util.function.IntFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionIntFunction<E extends Exception, R> {
	R apply(int value) throws E;

	default IntFunction<R> asIntFunction() {
		return value -> {
			try {
				return apply(value);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T, R> ExceptionIntFunction<RuntimeException, R> of(IntFunction<R> fn) {
		return fn::apply;
	}
}
