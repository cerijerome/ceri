package ceri.common.function;

import java.util.function.ToIntFunction;

public interface ToByteFunction<T> {
	byte applyAsByte(T value);
	
	static <T> ToIntFunction<T> toUint(ToByteFunction<T> toByte) {
		return t -> toByte.applyAsByte(t) & 0xff;
	}
	
}
