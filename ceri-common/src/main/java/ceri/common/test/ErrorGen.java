package ceri.common.test;

import static ceri.common.function.FunctionUtil.safeApply;
import static ceri.common.function.FunctionUtil.sequentialSupplier;
import java.io.IOException;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.function.FunctionUtil;
import ceri.common.function.Functions;
import ceri.common.function.Lambdas;
import ceri.common.reflect.Reflect;

/**
 * Utility for generating errors during tests.
 */
public class ErrorGen {
	private static final String EXCEPTION = "Exception";
	private static final Collector<CharSequence, ?, String> JOINER =
		Collectors.joining(",", "[", "]");
	public static final String MESSAGE = "generated";
	public static final Functions.Supplier<Exception> RTX = errorFn(RuntimeException::new, "RTX");
	public static final Functions.Supplier<Exception> RIX =
		errorFn(RuntimeInterruptedException::new, "RIX");
	public static final Functions.Supplier<Exception> INX =
		errorFn(InterruptedException::new, "INX");
	public static final Functions.Supplier<Exception> IOX = errorFn(IOException::new, "IOX");
	private volatile Functions.Supplier<Exception> errorFn = null;

	/**
	 * Convert an exception constructor to accept the standard message.
	 */
	public static Functions.Supplier<Exception>
		errorFn(Functions.Function<String, Exception> errorFn) {
		return () -> errorFn.apply(MESSAGE);
	}

	/**
	 * Convert an exception constructor to accept the standard message, and apply a name for debug
	 * purposes.
	 */
	public static Functions.Supplier<Exception>
		errorFn(Functions.Function<String, Exception> errorFn, String name) {
		return Lambdas.register(errorFn(errorFn), name);
	}

	/**
	 * Create an error generator instance.
	 */
	public static ErrorGen of() {
		return new ErrorGen();
	}

	private ErrorGen() {}

	/**
	 * Generate errors in given order. Use no-args to disable errors.
	 */
	public void set(Exception... errors) {
		if (errors.length == 0) clear();
		else {
			var sequential = sequentialSupplier(errors);
			var name = Stream.of(errors).map(e -> name(e)).collect(JOINER);
			setErrorFn(sequential, name);
		}
	}

	/**
	 * Generate errors from given functions in order, repeating the last function. Defined constants
	 * may be used: RTX, RIX, INX, IOX. The generated errors will have a fixed message.
	 */
	@SafeVarargs
	public final void setFrom(Functions.Supplier<? extends Exception>... errorFns) {
		if (errorFns.length == 0) clear();
		else {
			var sequential = sequentialSupplier(errorFns);
			var name = Stream.of(errorFns).map(Lambdas::nameOrSymbol).collect(JOINER);
			setErrorFn(() -> FunctionUtil.safeApply(sequential.get(), Functions.Supplier::get),
				name);
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
		call(ExceptionAdapter.runtime);
	}

	/**
	 * Execute the error generator if set. InterruptedExceptions are allowed; other exceptions are
	 * adapted to runtime exceptions.
	 */
	public void callWithInterrupt() throws InterruptedException {
		callWithInterrupt(ExceptionAdapter.runtime);
	}

	/**
	 * Execute the error generator if set. Exceptions are adapted according to the adapter.
	 */
	public <E extends Exception> void call(ExceptionAdapter<E> adapter) throws E {
		Exception ex = safeApply(errorFn, Functions.Supplier::get);
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
		Exception ex = safeApply(errorFn, Functions.Supplier::get);
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
	 * String for debugging purposes.
	 */
	@Override
	public String toString() {
		return errorFn == null ? "none" : Lambdas.name(errorFn);
	}

	private ErrorGen setErrorFn(Functions.Supplier<Exception> errorFn, String name) {
		return setErrorFn(Lambdas.register(errorFn, name));
	}

	private ErrorGen setErrorFn(Functions.Supplier<Exception> errorFn) {
		this.errorFn = errorFn;
		return this;
	}

	private static String name(Exception e) {
		String s = Reflect.className(e);
		return !s.endsWith(EXCEPTION) ? s : s.substring(0, s.length() - EXCEPTION.length());
	}
}
