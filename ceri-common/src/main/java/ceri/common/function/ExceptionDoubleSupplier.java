package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionDoubleSupplier<E extends Exception> {
	double getAsDouble() throws E;

	default DoubleSupplier asDoubleSupplier() {
		return () -> RUNTIME.getDouble(this);
	}

	static ExceptionDoubleSupplier<RuntimeException> of(DoubleSupplier supplier) {
		Objects.requireNonNull(supplier);
		return supplier::getAsDouble;
	}
}
