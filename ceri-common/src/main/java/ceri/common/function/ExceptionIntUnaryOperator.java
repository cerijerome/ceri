package ceri.common.function;

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
		return value -> {
			try {
				return applyAsInt(value);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T, R> ExceptionIntUnaryOperator<RuntimeException> of(IntUnaryOperator fn) {
		return fn::applyAsInt;
	}
}