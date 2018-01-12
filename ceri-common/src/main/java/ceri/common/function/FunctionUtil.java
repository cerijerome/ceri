package ceri.common.function;

import java.util.function.Function;
import java.util.stream.Stream;
import ceri.common.util.EqualsUtil;

public class FunctionUtil {
	private static final int MAX_RECURSIONS_DEF = 20;
	
	private FunctionUtil() {}

	/**
	 * Wraps a function that passes through null values.
	 */
	public static <T, R> Function<T, R> safe(Function<T, R> function) {
		return t -> t == null ? null : function.apply(t);
	}
	
	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<T, T> fn) {
		return recurse(t, fn, MAX_RECURSIONS_DEF);
	}
	
	/**
	 * Execute the function until no change, or the maximum number of recursions is met.
	 */
	public static <T> T recurse(T t, Function<T, T> fn, int max) {
		T last;
		while (max-- > 0) {
			last = t;
			t = fn.apply(t);
			if (EqualsUtil.equals(t, last)) break;
		}
		return t;
	}
	
	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Iterable<T> iter,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.handle(() -> iter.forEach(w.wrap(consumer)));
	}

	/**
	 * Executes for-each, allowing exception of given type to be thrown.
	 */
	public static <E extends Exception, T> void forEach(Stream<T> stream,
		ExceptionConsumer<E, ? super T> consumer) throws E {
		FunctionWrapper<E> w = FunctionWrapper.create();
		w.handle(() -> stream.forEach(w.wrap(consumer)));
	}

	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionRunnable<E> runnable) {
		return t -> {
			runnable.run();
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception, T> ExceptionFunction<E, T, Boolean>
		asFunction(ExceptionConsumer<E, T> consumer) {
		return t -> {
			consumer.accept(t);
			return Boolean.TRUE;
		};
	}

	public static <E extends Exception, T, U> ExceptionFunction<E, U, T>
		asFunction(ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

}
