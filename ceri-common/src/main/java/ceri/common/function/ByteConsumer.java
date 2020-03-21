package ceri.common.function;

import java.util.Objects;
import java.util.function.IntConsumer;
import ceri.common.math.MathUtil;

public interface ByteConsumer {

	void accept(byte value);

	default ByteConsumer andThen(ByteConsumer after) {
		Objects.requireNonNull(after);
		return i -> {
			accept(i);
			after.accept(i);
		};
	}

	static IntConsumer toInt(ByteConsumer consumer) {
		Objects.requireNonNull(consumer);
		return i -> consumer.accept((byte) i);
	}

	static IntConsumer toIntExact(ByteConsumer consumer) {
		Objects.requireNonNull(consumer);
		return i -> consumer.accept(MathUtil.byteExact(i));
	}

}
