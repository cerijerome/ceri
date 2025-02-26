package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Function that can throw exceptions.
 */
public interface ExceptionByteFunction<E extends Exception, R> {

	R apply(byte value) throws E;

	default ByteFunction<R> asByteFunction() {
		return i -> RUNTIME.get(() -> apply(i));
	}

	static <R> ExceptionByteFunction<RuntimeException, R> of(ByteFunction<R> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}
