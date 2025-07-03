package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.UnaryOperator;

public interface ExceptionUnaryOperator<E extends Exception, T> extends ExceptionFunction<E, T, T> {

	default UnaryOperator<T> asUnaryOperator() {
		return t -> RUNTIME.get(() -> apply(t));
	}

	static <T> ExceptionUnaryOperator<RuntimeException, T> of(UnaryOperator<T> fn) {
		Objects.requireNonNull(fn);
		return fn::apply;
	}
}