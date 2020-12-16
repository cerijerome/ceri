package ceri.common.util;

import ceri.common.function.ExceptionSupplier;

/**
 * A lazy-loaded value using a supplier. Unlike Optional, this allows null values. Not thread-safe.
 */
public class ValueCache<E extends Exception, T> implements ExceptionSupplier<E, T> {
	private final ExceptionSupplier<E, T> supplier;
	private boolean hasValue = false;
	private T value = null;

	public static <E extends Exception, T> ValueCache<E, T> of(ExceptionSupplier<E, T> supplier) {
		return new ValueCache<>(supplier);
	}

	private ValueCache(ExceptionSupplier<E, T> supplier) {
		this.supplier = supplier;
	}

	public boolean hasValue() {
		return hasValue;
	}

	public void set(T value) {
		this.value = value;
		hasValue = true;
	}
	
	@Override
	public T get() throws E {
		if (!hasValue) set(supplier.get());
		return value;
	}

	@Override
	public String toString() {
		return hasValue ? "value=" + String.valueOf(value) : "none";
	}
}
