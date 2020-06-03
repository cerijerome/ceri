package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.LongConsumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionLongConsumer<E extends Exception> {
	void accept(long value) throws E;

	default ExceptionLongConsumer<E> andThen(ExceptionLongConsumer<? extends E> after) {
		Objects.requireNonNull(after);
		return (long t) -> {
			accept(t);
			after.accept(t);
		};
	}

	default LongConsumer asLongConsumer() {
		return t -> RUNTIME.run(() -> accept(t));
	}

	static ExceptionLongConsumer<RuntimeException> of(LongConsumer consumer) {
		Objects.requireNonNull(consumer);
		return consumer::accept;
	}
}