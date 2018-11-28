package ceri.common.function;

import java.util.function.ToIntFunction;

public interface ToShortFunction<T> {
	short applyAsShort(T value);
	
	static <T> ToIntFunction<T> toUint(ToShortFunction<T> toShort) {
		return t -> toShort.applyAsShort(t) & 0xffff;
	}
	
}
