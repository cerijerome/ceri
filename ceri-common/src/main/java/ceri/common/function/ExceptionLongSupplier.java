package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionLongSupplier<E extends Exception> {
	long getAsLong() throws E;

	default LongSupplier asLongSupplier() {
		return () -> RUNTIME.getLong(this);
	}

	static ExceptionLongSupplier<RuntimeException> of(LongSupplier supplier) {
		Objects.requireNonNull(supplier);
		return supplier::getAsLong;
	}
}
