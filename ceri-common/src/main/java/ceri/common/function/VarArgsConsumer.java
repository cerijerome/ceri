package ceri.common.function;

import java.util.Objects;

/**
 * Function that accepts varargs. Care should be taken to avoid heap pollution.
 */
public interface VarArgsConsumer<T> {

	void accept(@SuppressWarnings("unchecked") T... ts);

	default VarArgsConsumer<T> andThen(VarArgsConsumer<? super T> after) {
		Objects.requireNonNull(after);
		return (@SuppressWarnings("unchecked") T... ts) -> {
			accept(ts);
			after.accept(ts);
		};
	}
}
