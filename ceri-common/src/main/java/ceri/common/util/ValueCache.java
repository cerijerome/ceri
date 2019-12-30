package ceri.common.util;

import java.util.function.Supplier;

/**
 * A lazy-loaded value using a supplier. Unlike Optional, this allows null value.
 * Not thread-safe.
 */
public class ValueCache<T> implements Supplier<T> {
	private final Supplier<T> supplier;
	private boolean hasValue = false;
	private T value = null;
	
	public static <T> ValueCache<T> of(Supplier<T> supplier) {
		return new ValueCache<>(supplier);
	}
	
	private ValueCache(Supplier<T> supplier) {
		this.supplier = supplier;
	}
	
	public boolean hasValue() {
		return hasValue;
	}
	
	@Override
	public T get() {
		if (!hasValue) {
			value = supplier.get();
			hasValue = true;
		}
		return value;
	}
	
	@Override
	public String toString() {
		return hasValue ? "value=" + String.valueOf(value) : "none";
	}
}
