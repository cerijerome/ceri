package ceri.common.collection;

import java.util.Objects;
import java.util.function.ObjIntConsumer;
import ceri.common.function.ObjIntFunction;

/**
 * Useful for streaming an object and its index, to replace a for loop.
 */
public class Indexed<T> {
	public final T val;
	public final int i;

	public static <T> Indexed<T> of(T val, int i) {
		return new Indexed<>(val, i);
	}

	private Indexed(T val, int i) {
		this.val = val;
		this.i = i;
	}

	public void consume(ObjIntConsumer<T> consumer) {
		consumer.accept(val, i);
	}

	public <R> R apply(ObjIntFunction<T, R> function) {
		return function.apply(val, i);
	}

	@Override
	public int hashCode() {
		return Objects.hash(val, i);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Indexed)) return false;
		Indexed<?> other = (Indexed<?>) obj;
		if (i != other.i) return false;
		if (!Objects.equals(val, other.val)) return false;
		return true;
	}

	@Override
	public String toString() {
		return val + ":" + i;
	}
}
