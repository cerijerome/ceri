package ceri.common.function;

import java.util.stream.Stream;

public class FunctionUtil {

	private FunctionUtil() {}

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

	public static <E extends Exception, T> ExceptionFunction<E, ?, T>
		asFunction(ExceptionSupplier<E, T> supplier) {
		return t -> supplier.get();
	}

}
