package ceri.common.function;

import java.util.function.ToIntFunction;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionToIntFunction<E extends Exception, T> {
	int applyAsInt(T value)throws E;

	default ToIntFunction<T> asToIntFunction() {
		return t -> {
			try {
				return applyAsInt(t);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> ExceptionToIntFunction<RuntimeException, T> of(ToIntFunction<T> fn) {
		return fn::applyAsInt;
	}
}
