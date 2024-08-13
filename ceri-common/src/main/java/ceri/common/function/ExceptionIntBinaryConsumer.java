package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

public interface ExceptionIntBinaryConsumer<E extends Exception> {

	void accept(int left, int right) throws E;

	default IntBinaryConsumer asIntBinaryConsumer() {
		return (l, r) -> RUNTIME.run(() -> accept(l, r));
	}

	static ExceptionIntBinaryConsumer<RuntimeException> of(IntBinaryConsumer fn) {
		Objects.requireNonNull(fn);
		return fn::accept;
	}
}