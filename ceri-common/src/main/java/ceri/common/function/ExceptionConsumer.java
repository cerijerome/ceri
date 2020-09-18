package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionConsumer<E extends Exception, T> {
	void accept(T t) throws E;

	default ExceptionConsumer<E, T> andThen(ExceptionConsumer<? extends E, ? super T> after) {
		Objects.requireNonNull(after);
		return t -> {
			accept(t);
			after.accept(t);
		};
	}

	default Consumer<T> asConsumer() {
		return t -> RUNTIME.run(() -> accept(t));
	}

	static <T> ExceptionConsumer<RuntimeException, T> of(Consumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return consumer::accept;
	}
}