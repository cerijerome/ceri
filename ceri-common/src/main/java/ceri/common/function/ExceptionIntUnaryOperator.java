package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntUnaryOperator;

public interface ExceptionIntUnaryOperator<E extends Exception> {

	int applyAsInt(int operand) throws E;

	default ExceptionIntUnaryOperator<E> compose(ExceptionIntUnaryOperator<? extends E> before) {
		Objects.requireNonNull(before);
		return (int v) -> applyAsInt(before.applyAsInt(v));
	}

	default ExceptionIntUnaryOperator<E> andThen(ExceptionIntUnaryOperator<? extends E> after) {
		Objects.requireNonNull(after);
		return (int t) -> after.applyAsInt(applyAsInt(t));
	}

	default IntUnaryOperator asIntUnaryOperator() {
		return i -> RUNTIME.getInt(() -> applyAsInt(i));
	}

	static ExceptionIntUnaryOperator<RuntimeException> of(IntUnaryOperator fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsInt;
	}
}