package ceri.common.function;

/**
 * Runnable that can throw exceptions.
 */
public interface ExceptionRunnable<E extends Exception> {
	void run() throws E;

	default Runnable asRunnable() {
		return () -> {
			try {
				run();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	static ExceptionRunnable<RuntimeException> of(Runnable runnable) {
		return runnable::run;
	}
}