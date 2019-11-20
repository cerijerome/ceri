package ceri.common.function;

import static ceri.common.util.ExceptionAdapter.RUNTIME;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	void run() throws E;

	default Runnable asRunnable() {
		return () -> RUNTIME.run(this);
	}

	static ExceptionRunnable<RuntimeException> of(Runnable runnable) {
		return runnable::run;
	}
}