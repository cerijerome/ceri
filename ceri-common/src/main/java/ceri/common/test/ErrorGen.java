package ceri.common.test;

import static ceri.common.function.FunctionUtil.safeApply;
import static ceri.common.function.FunctionUtil.sequentialSupplier;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionAdapter;

/**
 * Utility for generating errors during tests.
 */
public class ErrorGen {
	public static final String MESSAGE = "generated";
	public static final Function<String, Exception> RTX = RuntimeException::new;
	public static final Function<String, Exception> RIX = RuntimeInterruptedException::new;
	public static final Function<String, Exception> INX = InterruptedException::new;
	public static final Function<String, Exception> IOX = IOException::new;
	private volatile Supplier<Exception> errorFn = null;

	public static ErrorGen of() {
		return new ErrorGen();
	}

	private ErrorGen() {}

	/**
	 * Generate errors in given order. Use no-args to generate no errors.
	 */
	public void set(Exception... errors) {
		setErrorFn(errors.length == 0 ? null : sequentialSupplier(errors));
	}

	/**
	 * Generate errors from given functions in order, repeating the last function. Defined constants
	 * may be used: RTX, RIX, INX, IOX. The generated errors will have a fixed message.
	 */
	@SafeVarargs
	public final void setFrom(Function<String, Exception>... errorFns) {
		if (errorFns.length == 0) setErrorFn(null);
		else {
			var sequential = sequentialSupplier(errorFns);
			setErrorFn(() -> safeApply(sequential.get(), s -> s.apply(MESSAGE)));
		}
	}

	/**
	 * Clear error generation.
	 */
	public void clear() {
		setErrorFn(null);
	}

	/**
	 * Execute the error generator if set. Exceptions are adapted to runtime exceptions.
	 */
	public void call() {
		call(ExceptionAdapter.RUNTIME);
	}

	/**
	 * Execute the error generator if set. InterruptedExceptions are allowed; other exceptions are
	 * adapted to runtime exceptions.
	 */
	public void callWithInterrupt() throws InterruptedException {
		callWithInterrupt(ExceptionAdapter.RUNTIME);
	}

	/**
	 * Execute the error generator if set. Exceptions are adapted according to the adapter.
	 */
	public <E extends Exception> void call(ExceptionAdapter<E> adapter) throws E {
		Exception ex = safeApply(errorFn, Supplier::get);
		if (ex == null) return;
		try {
			throw ex;
		} catch (RuntimeException e) {
			throw e;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (Exception e) {
			throw adapter.apply(e);
		}
	}

	/**
	 * Execute the error generator if set. InterruptedExceptions are allowed; other exceptions are
	 * adapted according to the adapter.
	 */
	public <E extends Exception> void callWithInterrupt(ExceptionAdapter<E> adapter)
		throws InterruptedException, E {
		Exception ex = safeApply(errorFn, Supplier::get);
		if (ex == null) return;
		try {
			throw ex;
		} catch (InterruptedException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw adapter.apply(e);
		}
	}

	/**
	 * String for debugging purposes. This calls errorFn.get(), which may have side effects,
	 * depending on the function.
	 */
	@Override
	public String toString() {
		return errorFn == null ? "[none]" : "[" + errorFn.get() + "]";
	}

	/**
	 * Sets the error function; use null for no error generation.
	 */
	private ErrorGen setErrorFn(Supplier<Exception> errorFn) {
		this.errorFn = errorFn;
		return this;
	}

}
