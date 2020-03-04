package ceri.common.function;

import java.util.Objects;
import java.util.function.ToIntFunction;

public interface ToByteFunction<T> {
	byte applyAsByte(T value);
	
	static <T> ToIntFunction<T> toUint(ToByteFunction<T> fn) {
		Objects.requireNonNull(fn);
		return t -> fn.applyAsByte(t) & 0xff;
	}
	
}
