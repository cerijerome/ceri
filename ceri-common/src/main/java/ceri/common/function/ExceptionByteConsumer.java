package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionByteConsumer<E extends Exception> {
	void accept(byte value) throws E;

	default ExceptionByteConsumer<E> andThen(ExceptionByteConsumer<? extends E> after) {
		Objects.requireNonNull(after);
		return (byte t) -> {
			accept(t);
			after.accept(t);
		};
	}

	default ByteConsumer asByteConsumer() {
		return t -> RUNTIME.run(() -> accept(t));
	}

	static ExceptionByteConsumer<RuntimeException> of(ByteConsumer consumer) {
		Objects.requireNonNull(consumer);
		return consumer::accept;
	}

}