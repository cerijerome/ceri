package ceri.common.collection;

import java.util.function.ObjIntConsumer;
import ceri.common.function.ObjIntFunction;

/**
 * Useful for streaming an object and its index, to replace a for loop.
 */
public class Indexed<T> {
	public final int i;
	public final T val;

	public static <T> Indexed<T> of(int i, T val) {
		return new Indexed<>(i, val);
	}

	private Indexed(int i, T val) {
		this.i = i;
		this.val = val;
	}

	public void consume(ObjIntConsumer<T> consumer) {
		consumer.accept(val, i);
	}

	public <R> R apply(ObjIntFunction<T, R> function) {
		return function.apply(val, i);
	}

}
