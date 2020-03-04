package ceri.common.function;

import java.util.Objects;
import java.util.function.ObjIntConsumer;

public interface ObjBooleanConsumer<T> {

	void accept(T t, boolean value);
	
	default ObjBooleanConsumer<T> andThen(ObjBooleanConsumer<? super T> after) {
		Objects.requireNonNull(after);
		return (t, i) -> {
			accept(t, i);
			after.accept(t, i);
		};
	}
	
	static <T> ObjIntConsumer<T> toInt(ObjBooleanConsumer<T> consumer) {
		Objects.requireNonNull(consumer);
		return (t, i) -> consumer.accept(t, i != 0);
	}

}
