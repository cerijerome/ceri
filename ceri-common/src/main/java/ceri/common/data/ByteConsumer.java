package ceri.common.data;

import java.util.Objects;

public interface ByteConsumer {

	void accept(byte value);

	default ByteConsumer andThen(ByteConsumer after) {
		Objects.requireNonNull(after);
		return (byte t) -> {
			accept(t);
			after.accept(t);
		};
	}

}
