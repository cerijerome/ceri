package ceri.common.function;

import java.util.function.Function;

public interface ObjIntFunction<T, R> {

	R apply(T t, int value);

	/**
	 * Wraps a function, ignoring int value;
	 */
	static <T, R> ObjIntFunction<T, R> wrap(Function<T, R> fn){
		return (t, value) -> fn.apply(t);
	}
}
