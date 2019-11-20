package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionIntConsumer<E extends Exception> {
	void accept(int value) throws E;

	default ExceptionIntConsumer<E> andThen(ExceptionIntConsumer<? extends E> after) {
		Objects.requireNonNull(after);
		return (int t) -> { accept(t); after.accept(t); };
	}

	default IntConsumer asIntConsumer() {
		return t -> RUNTIME.run(() -> accept(t));
	}

	static ExceptionIntConsumer<RuntimeException> of(IntConsumer consumer) {
		return consumer::accept;
	}
}