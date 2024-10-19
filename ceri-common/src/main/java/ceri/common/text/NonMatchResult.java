package ceri.common.text;

/**
 * A non-match result group.
 */
public interface NonMatchResult {
	/**
	 * Start index of the non-matched group.
	 */
	int start();

	/**
	 * End index of the non-matched group.
	 */
	int end();

	/**
	 * The non-matched group.
	 */
	String group();
}
