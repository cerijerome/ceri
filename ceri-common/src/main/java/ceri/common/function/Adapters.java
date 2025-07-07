package ceri.common.function;

/**
 * Adapts various function types.
 */
public class Adapters {
	private Adapters() {}

	public static <E extends Exception, T> Excepts.Supplier<E, T>
		runnableAsSupplier(Excepts.Runnable<E> runnable, T t) {
		return () -> {
			runnable.run();
			return t;
		};
	}

}
