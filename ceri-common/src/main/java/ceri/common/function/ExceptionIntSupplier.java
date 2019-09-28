package ceri.common.function;

import java.util.function.IntSupplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionIntSupplier<E extends Exception> {
	int getAsInt() throws E;

	default IntSupplier asIntSupplier() {
		return () -> {
			try {
				return getAsInt();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static ExceptionIntSupplier<RuntimeException> of(IntSupplier supplier) {
		return supplier::getAsInt;
	}
}
