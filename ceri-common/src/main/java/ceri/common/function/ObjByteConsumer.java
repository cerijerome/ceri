package ceri.common.function;

import java.util.Objects;
import java.util.function.ObjIntConsumer;
import ceri.common.math.MathUtil;

public interface ObjByteConsumer<T> {

	void accept(T t, byte value);

	default ObjByteConsumer<T> andThen(ObjByteConsumer<? super T> after) {
		Objects.requireNonNull(after);
		return (t, i) -> {
			accept(t, i);
			after.accept(t, i);
		};
	}

	static <T> ObjIntConsumer<T> toInt(ObjByteConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, (byte) i);
	}

	static <T> ObjIntConsumer<T> toIntExact(ObjByteConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, MathUtil.byteExact(i));
	}

	static <T> ObjIntConsumer<T> toUintExact(ObjByteConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, MathUtil.ubyteExact(i));
	}

}
