package ceri.common.function;

/**
 * Interface that accepts various function types by adapting the given function.
 */
public interface Functional<T> {

	/**
	 * Applies the function.
	 */
	<E extends Exception, R> R apply(ExceptionFunction<E, T, R> function) throws E;

	/**
	 * Accepts the consumer.
	 */
	default <E extends Exception> void accept(ExceptionConsumer<E, T> consumer) throws E {
		apply(t -> {
			consumer.accept(t);
			return null;
		});
	}

	/**
	 * Gets the supplier.
	 */
	default <E extends Exception, R> R get(ExceptionSupplier<E, R> supplier) throws E {
		return apply(_ -> supplier.get());
	}

	/**
	 * Runs the runnable.
	 */
	default <E extends Exception> void run(ExceptionRunnable<E> runnable) throws E {
		apply(_ -> {
			runnable.run();
			return null;
		});
	}
}
