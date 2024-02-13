package ceri.common.function;

import java.util.Objects;

public interface ObjLongConsumer<T> {

	void accept(T t, long value);

	default ObjLongConsumer<T> andThen(ObjLongConsumer<? super T> after) {
		Objects.requireNonNull(after);
		return (t, l) -> {
			accept(t, l);
			after.accept(t, l);
		};
	}
}
