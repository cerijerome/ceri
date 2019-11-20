package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.function.BiConsumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionBiConsumer<E extends Exception, T, U> {
	void accept(T t, U u) throws E;

	default BiConsumer<T, U> asBiConsumer() {
		return (t, u) -> RUNTIME.run(() -> accept(t, u));
	}

	static <T, U> ExceptionBiConsumer<RuntimeException, T, U> of(BiConsumer<T, U> consumer) {
		return consumer::accept;
	}
}