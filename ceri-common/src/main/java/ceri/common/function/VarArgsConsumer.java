package ceri.common.function;

import java.util.Objects;

/**
 * Function that accepts varargs. 
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
