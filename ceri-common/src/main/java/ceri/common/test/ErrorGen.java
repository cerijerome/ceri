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

	public void set(Exception... errors) {
		setErrorFn(sequentialSupplier(errors));
	}

	@SafeVarargs
	public final void setFrom(Function<String, Exception>... errorFns) {
		var sequential = sequentialSupplier(errorFns);
		setErrorFn(() -> safeApply(safeApply(sequential, Supplier::get), s -> s.apply(MESSAGE)));
	}

	public void clear() {
		setErrorFn(null);
	}

	public void call() {
		call(ExceptionAdapter.RUNTIME);
	}

	public void callWithInterrupt() throws InterruptedException {
		callWithInterrupt(ExceptionAdapter.RUNTIME);
	}

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
	 * String for debugging purposes. This call errorFn.get(), which may have side effects,
	 * depending on the function.
	 */
	@Override
	public String toString() {
		return errorFn == null ? "[none]" : "[" + errorFn.get() + "]";
	}

	private ErrorGen setErrorFn(Supplier<Exception> errorFn) {
		this.errorFn = errorFn;
		return this;
	}

}
