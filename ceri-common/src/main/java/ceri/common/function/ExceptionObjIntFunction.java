package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjIntFunction<E extends Exception, T, R> {
	R apply(T t, int value) throws E;

	default ObjIntFunction<T, R> asObjIntFunction() {
		return (t, i) -> RUNTIME.get(() -> apply(t, i));
	}

	static <T, R> ExceptionObjIntFunction<RuntimeException, T, R> of(ObjIntFunction<T, R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
