package ceri.common.collection;

import java.util.function.ObjIntConsumer;
import ceri.common.function.ObjIntFunction;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

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

	@Override
	public int hashCode() {
		return HashCoder.hash(i, val);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Indexed)) return false;
		Indexed<?> other = (Indexed<?>) obj;
		if (i != other.i) return false;
		if (!EqualsUtil.equals(val, other.val)) return false;
		return true;
	}

	@Override
	public String toString() {
		return i + ":" + val;
	}
}
