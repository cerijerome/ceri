package ceri.common.function;

import java.util.function.BiConsumer;

/**
 * Consumer that can throw exceptions.
 */
public interface ExceptionBiConsumer<E extends Exception, T, U> {
	void accept(T t, U u) throws E;

	default BiConsumer<T, U> asBiConsumer() {
		return (t, u) -> {
			try {
				accept(t, u);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T, U> ExceptionBiConsumer<RuntimeException, T, U> of(BiConsumer<T, U> consumer) {
		return consumer::accept;
	}
}