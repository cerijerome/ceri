package ceri.common.util;

import java.util.Arrays;

/**
 * Simple interface for a object that can be initialized.
 */
public interface Initializable<E extends Exception> {

	/**
	 * Initialize the instance.
	 */
	void init() throws E;

	interface Runtime extends Initializable<RuntimeException> {}

	/**
	 * Initializes the given types.
	 */
	@SafeVarargs
	static <E extends Exception> void init(Initializable<E>... initializables) throws E {
		init(Arrays.asList(initializables));
	}

	/**
	 * Initializes the given types.
	 */
	static <E extends Exception> void init(Iterable<Initializable<E>> initializables) throws E {
		for (var initializable : initializables)
			if (initializable != null) initializable.init();
	}
}
