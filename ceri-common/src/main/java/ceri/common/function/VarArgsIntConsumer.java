package ceri.common.function;

import java.util.Objects;

/**
 * Function that accepts int varargs. 
 */
public interface VarArgsIntConsumer {
	void accept(int... values);

	default VarArgsIntConsumer andThen(VarArgsIntConsumer after) {
		Objects.requireNonNull(after);
		return (int... ts) -> {
			accept(ts);
			after.accept(ts);
		};
	}
}
