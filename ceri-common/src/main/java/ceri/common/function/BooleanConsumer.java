package ceri.common.function;

import java.util.Objects;
import java.util.function.IntConsumer;

public interface BooleanConsumer {

	void accept(boolean value);

	default BooleanConsumer andThen(BooleanConsumer after) {
		Objects.requireNonNull(after);
		return (boolean t) -> {
			accept(t);
			after.accept(t);
		};
	}

	static IntConsumer toInt(BooleanConsumer con) {
		Objects.requireNonNull(con);
		return i -> con.accept(i != 0);
	}

}
