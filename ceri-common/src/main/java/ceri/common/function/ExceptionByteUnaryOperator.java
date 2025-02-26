package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

public interface ExceptionByteUnaryOperator<E extends Exception> {

	byte applyAsByte(byte operand) throws E;

	default ExceptionByteUnaryOperator<E> compose(ExceptionByteUnaryOperator<? extends E> before) {
		Objects.requireNonNull(before);
		return (byte v) -> applyAsByte(before.applyAsByte(v));
	}

	default ExceptionByteUnaryOperator<E> andThen(ExceptionByteUnaryOperator<? extends E> after) {
		Objects.requireNonNull(after);
		return (byte t) -> after.applyAsByte(applyAsByte(t));
	}

	default ByteUnaryOperator asByteUnaryOperator() {
		return i -> RUNTIME.getByte(() -> applyAsByte(i));
	}

	static ExceptionByteUnaryOperator<RuntimeException> of(ByteUnaryOperator fn) {
		Objects.requireNonNull(fn);
		return fn::applyAsByte;
	}
}