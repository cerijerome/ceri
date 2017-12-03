package ceri.common.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import ceri.common.util.BasicUtil;

/**
 * Wraps and unwraps functions that throw exceptions, for use in functional interfaces. Not
 * thread-safe.
 */
public class FunctionWrapper<E extends Exception> {
	private WrapperException.Agent<E> agent = WrapperException.agent();
	
	public static <E extends Exception> FunctionWrapper<E> create() {
		return new FunctionWrapper<>();
	}

	private FunctionWrapper() {}

	public void handle(ExceptionRunnable<E> runnable) throws E {
		handleIt(asFunction(runnable), null);
	}

	public <T> T handle(ExceptionSupplier<E, T> supplier) throws E {
		return handleIt(asFunction(supplier), null);
	}

	public <T, R> R handle(ExceptionFunction<E, T, R> function, T t) throws E {
		return handleIt(function, t);
	}

	public <T> void handle(ExceptionConsumer<E, T> consumer, T t) throws E {
		handleIt(asFunction(consumer), t);
	}

	public Runnable wrap(ExceptionRunnable<E> runnable) {
		return () -> wrapIt(asFunction(runnable), null);
	}

	public <T, R> Function<T, R> wrap(ExceptionFunction<E, T, R> function) {
		return t -> wrapIt(function, t);
	}

	public <T> Consumer<T> wrap(ExceptionConsumer<E, T> consumer) {
		return t -> wrapIt(asFunction(consumer), t);
	}

	public <T> Supplier<T> wrap(ExceptionSupplier<E, T> supplier) {
		return () -> wrapIt(asFunction(supplier), null);
	}

	private <T> ExceptionFunction<E, T, Boolean> asFunction(ExceptionRunnable<E> runnable) {
		return t -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	private <T> ExceptionFunction<E, T, Boolean> asFunction(ExceptionConsumer<E, T> consumer) {
		return t -> {
			consumer.accept(t);
			return Boolean.TRUE;
		};
	}

	private <T> ExceptionFunction<E, ?, T> asFunction(ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

	private <T, R> R handleIt(ExceptionFunction<E, T, R> function, T t) throws E {
		try {
			return function.apply(t);
		} catch (WrapperException ex) {
			return agent.handle(ex);
		}
	}

	private <T, R> R wrapIt(ExceptionFunction<E, T, R> function, T t) {
		try {
			return function.apply(t);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			return agent.wrap(BasicUtil.uncheckedCast(e));
		}
	}

}
