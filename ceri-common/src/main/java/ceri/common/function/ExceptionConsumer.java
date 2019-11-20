package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.function.Consumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionConsumer<E extends Exception, T> {
	void accept(T t) throws E;

	default Consumer<T> asConsumer() {
		return t -> RUNTIME.run(() -> accept(t));
	}

	static <T> ExceptionConsumer<RuntimeException, T> of(Consumer<T> consumer) {
		return consumer::accept;
	}
}