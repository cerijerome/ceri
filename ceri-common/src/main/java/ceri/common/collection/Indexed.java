package ceri.common.collection;

import java.util.function.ObjIntConsumer;
import ceri.common.function.Funcs.ObjIntFunction;

/**
 * Useful for streaming an object and its index, to replace a for loop.
 */
public record Indexed<T>(T val, int i) {

	public static <T> Indexed<T> of(T val, int i) {
		return new Indexed<>(val, i);
	}

	public void consume(ObjIntConsumer<T> consumer) {
		consumer.accept(val, i);
	}

	public <R> R apply(ObjIntFunction<T, R> function) {
		return function.apply(val, i);
	}

	@Override
	public String toString() {
		return val + ":" + i;
	}
}
