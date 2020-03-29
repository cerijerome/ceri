package ceri.common.function;

import java.util.Objects;
import java.util.function.ToIntFunction;
import ceri.common.math.MathUtil;

public interface ToShortFunction<T> {
	short applyAsShort(T value);

	static <T> ToIntFunction<T> toUint(ToShortFunction<T> fn) {
		Objects.requireNonNull(fn);
		return t -> MathUtil.ushort(fn.applyAsShort(t));
	}

}
