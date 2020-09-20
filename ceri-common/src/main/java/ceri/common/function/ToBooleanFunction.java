package ceri.common.function;

import java.util.Objects;
import java.util.function.ToIntFunction;

public interface ToBooleanFunction<T> {
	boolean applyAsBoolean(T value);

	static <T> ToIntFunction<T> toInt(ToBooleanFunction<T> fn) {
		Objects.requireNonNull(fn);
		return t -> fn.applyAsBoolean(t) ? 1 : 0;
	}

}
