package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionObjLongFunction<E extends Exception, T, R> {
	R apply(T t, long value) throws E;

	default ObjLongFunction<T, R> asObjLongFunction() {
		return (t, l) -> RUNTIME.get(() -> apply(t, l));
	}

	static <T, R> ExceptionObjLongFunction<RuntimeException, T, R> of(ObjLongFunction<T, R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
