package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;
import java.util.Objects;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	void run() throws E;

	default Runnable asRunnable() {
		return () -> RUNTIME.run(this);
	}

	static ExceptionRunnable<RuntimeException> of(Runnable runnable) {
		Objects.requireNonNull(runnable);
		return runnable::run;
	}
}