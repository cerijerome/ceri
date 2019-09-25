package ceri.common.function;

import java.util.Objects;
import java.util.function.Consumer;
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

	static ExceptionIntConsumer<RuntimeException> of(IntConsumer consumer) {
		return consumer::accept;
	}
}