package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.function.BooleanSupplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionBooleanSupplier<E extends Exception> {
	boolean getAsBoolean() throws E;

	default BooleanSupplier asBooleanSupplier() {
		return () -> RUNTIME.getBoolean(this);
	}

	static ExceptionBooleanSupplier<RuntimeException> of(BooleanSupplier supplier) {
		return supplier::getAsBoolean;
	}
}
