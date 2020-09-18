package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

public interface ExceptionIntBinaryOperator<E extends Exception> {

	int applyAsInt(int left, int right) throws E;

	default IntBinaryOperator asIntBinaryOperator() {
		return (l, r) -> RUNTIME.getInt(() -> applyAsInt(l, r));
	}

	static ExceptionIntBinaryOperator<RuntimeException> of(IntBinaryOperator fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsInt;
	}
}