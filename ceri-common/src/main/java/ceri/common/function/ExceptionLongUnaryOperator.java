package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.LongUnaryOperator;

public interface ExceptionLongUnaryOperator<E extends Exception> {

	long applyAsLong(long operand) throws E;

	default ExceptionLongUnaryOperator<E> compose(ExceptionLongUnaryOperator<? extends E> before) {
		Objects.requireNonNull(before);
		return (long v) -> applyAsLong(before.applyAsLong(v));
	}

	default ExceptionLongUnaryOperator<E> andThen(ExceptionLongUnaryOperator<? extends E> after) {
		Objects.requireNonNull(after);
		return (long t) -> after.applyAsLong(applyAsLong(t));
	}

	default LongUnaryOperator asLongUnaryOperator() {
		return i -> RUNTIME.getLong(() -> applyAsLong(i));
	}

	static ExceptionLongUnaryOperator<RuntimeException> of(LongUnaryOperator fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsLong;
	}
}