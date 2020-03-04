package ceri.common.function;

import java.util.Objects;
import java.util.function.ObjIntConsumer;
import ceri.common.math.MathUtil;

public interface ObjShortConsumer<T> {

	void accept(T t, short value);
	
	default ObjShortConsumer<T> andThen(ObjShortConsumer<? super T> after) {
		Objects.requireNonNull(after);
		return (t, i) -> {
			accept(t, i);
			after.accept(t, i);
		};
	}
	
	static <T> ObjIntConsumer<T> toInt(ObjShortConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, (short) i);
	}

	static <T> ObjIntConsumer<T> toIntExact(ObjShortConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, MathUtil.toShortExact(i));
	}

	static <T> ObjIntConsumer<T> toUintExact(ObjShortConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, MathUtil.toUshortExact(i));
	}
}
