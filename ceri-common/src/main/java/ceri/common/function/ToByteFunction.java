package ceri.common.function;

import java.util.Objects;
import java.util.function.ToIntFunction;
import ceri.common.math.MathUtil;

public interface ToByteFunction<T> {
	byte applyAsByte(T value);

	static <T> ToIntFunction<T> toUint(ToByteFunction<T> fn) {
		Objects.requireNonNull(fn);
		return t -> MathUtil.ubyte(fn.applyAsByte(t));
	}

}
