package ceri.common.util;

/**
 * Simple interface for a object that can be initialized.
 */
public interface Initializable<E extends Exception> {

	/**
	 * Initialize the instance.
	 */
	void init() throws E;

	interface Runtime extends Initializable<RuntimeException> {}
}
