package ceri.common.concurrent;

import java.util.Optional;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionSupplier;

public class VolatileField<T> {
	private volatile T value;

	public T value(T value) {
		this.value = value;
		return value;
	}

	public T value() {
		return value;
	}

	/**
	 * Computes the value from the current value, and saves the result if non-null.
	 */
	public <E extends Exception> T compute(ExceptionFunction<E, T, T> function) throws E {
		var value = function.apply(value());
		return value == null ? value : value(value);
	}

	/**
	 * Computes and saves the computed value if current value is null.
	 */
	public <E extends Exception> T computeIfAbsent(ExceptionSupplier<E, T> supplier) throws E {
		var value = value();
		if (value == null) value = supplier.get();
		return value == null ? value : value(value);
	}

	/**
	 * Computes and saves the computed value if current value is not null.
	 */
	public <E extends Exception> T computeIfPresent(ExceptionFunction<E, T, T> function) throws E {
		var value = value();
		if (value != null) value = function.apply(value);
		return value == null ? value : value(value);
	}

	/**
	 * Returns the value as an Optional instance.
	 */
	public Optional<T> optional() {
		return Optional.ofNullable(value());
	}
}
