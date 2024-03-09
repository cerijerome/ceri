package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionBiConsumer<E extends Exception, T, U> {
	void accept(T t, U u) throws E;

	default ExceptionBiConsumer<E, T, U>
		andThen(ExceptionBiConsumer<? extends E, ? super T, ? super U> after) {
		Objects.requireNonNull(after);
		return (t, u) -> {
			accept(t, u);
			after.accept(t, u);
		};
	}

	default BiConsumer<T, U> asBiConsumer() {
		return (t, u) -> RUNTIME.run(() -> accept(t, u));
	}

	static <T, U> ExceptionBiConsumer<RuntimeException, T, U> of(BiConsumer<T, U> consumer) {
		Objects.requireNonNull(consumer);
		return consumer::accept;
	}

	/**
	 * Converts a bi-consumer to a bi-function that returns the given result.
	 */
	static <E extends Exception, T, U, R> ExceptionBiFunction<E, T, U, R>
		asBiFunction(ExceptionBiConsumer<E, T, U> consumer, R result) {
		return (t, u) -> {
			consumer.accept(t, u);
			return result;
		};
	}
}