package ceri.common.util;

/**
 * Mutable reference to an object. Not thread-safe.
 */
public class Holder<T> {
	private T value;

	public static <T> Holder<T> of() {
		return new Holder<>();
	}

	public static <T> Holder<T> init(T initialValue) {
		Holder<T> holder = new Holder<>();
		holder.set(initialValue);
		return holder;
	}

	private Holder() {}

	public T set(T value) {
		T oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public T get() {
		return value;
	}

	public T verify() {
		if (value == null) throw new IllegalStateException("Value is null");
		return value;
	}

}
