package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionByteSupplier<E extends Exception> {
	byte getAsByte() throws E;

	default ByteSupplier asByteSupplier() {
		return () -> RUNTIME.getByte(this);
	}

	static ExceptionByteSupplier<RuntimeException> of(ByteSupplier supplier) {
		Objects.requireNonNull(supplier);
		return supplier::getAsByte;
	}

}
