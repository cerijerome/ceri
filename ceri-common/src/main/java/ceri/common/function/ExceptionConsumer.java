package ceri.common.function;

import java.util.function.Consumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionConsumer<E extends Exception, T> {
	void accept(T t) throws E;

	default Consumer<T> asConsumer() {
		return t -> {
			try {
				accept(t);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> ExceptionConsumer<RuntimeException, T> of(Consumer<T> consumer) {
		return t -> consumer.accept(t);
	}
}