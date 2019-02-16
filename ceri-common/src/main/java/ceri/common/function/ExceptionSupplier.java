package ceri.common.function;

import java.util.function.Supplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionSupplier<E extends Exception, T> {
	T get() throws E;

	default Supplier<T> asSupplier() {
		return () -> {
			try {
				return get();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static <T> ExceptionSupplier<RuntimeException, T> of(Supplier<T> supplier) {
		return () -> supplier.get();
	}
}