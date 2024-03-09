package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionIntSupplier<E extends Exception> {
	int getAsInt() throws E;

	default IntSupplier asIntSupplier() {
		return () -> RUNTIME.getInt(this);
	}

	static ExceptionIntSupplier<RuntimeException> of(IntSupplier supplier) {
		Objects.requireNonNull(supplier);
		return supplier::getAsInt;
	}
	
	/**
	 * Converts a supplier to a function that ignores input.
	 */
	static <E extends Exception, T> ExceptionToIntFunction<E, T>
		asToIntFunction(ExceptionIntSupplier<E> supplier) {
		return t -> supplier.getAsInt();
	}

}
