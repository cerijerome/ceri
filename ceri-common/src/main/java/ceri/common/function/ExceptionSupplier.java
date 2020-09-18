package ceri.common.function;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Supplier that can throw exceptions.
 */
public interface ExceptionSupplier<E extends Exception, T> {
	T get() throws E;

	default Supplier<T> asSupplier() {
		return () -> RUNTIME.get(this);
	}

	default Callable<T> asCallable() {
		return this::get;
	}

	static <T> ExceptionSupplier<RuntimeException, T> of(Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		return supplier::get;
	}

	static <T> ExceptionSupplier<Exception, T> of(Callable<T> supplier) {
		Objects.requireNonNull(supplier);
		return supplier::call;
	}

}