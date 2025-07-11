package ceri.common.function;

import ceri.common.function.Excepts.Runnable;
import ceri.common.function.Excepts.Supplier;

/**
 * Adapts various function types.
 */
public class Adapters {
	private Adapters() {}

	/**
	 * Adapts a runnable type to supplier with fixed value.
	 */
	public static <E extends Exception, T> Supplier<E, T> supplier(Runnable<? extends E> runnable,
		T t) {
		return () -> {
			runnable.run();
			return t;
		};
	}

}
