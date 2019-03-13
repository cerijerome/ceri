package ceri.common.event;

import java.util.function.IntConsumer;

/**
 * Interface to add/remove notification listeners.
 */
public interface IntListenable {

	/**
	 * Adds a listener to receive notifications. Returns true if added.
	 */
	boolean listen(IntConsumer listener);

	/**
	 * Removes a listener from receiving notifications. Returns true if removed.
	 */
	boolean unlisten(IntConsumer listener);

	/**
	 * Interface to indirectly add/remove notification listeners. Useful when classes use an
	 * IntListeners instance.
	 */
	static interface Indirect {
		IntListenable listeners();

		/**
		 * Converts a listenable type into an indirect listenable type.
		 */
		static Indirect from(IntListenable listenable) {
			return () -> listenable;
		}
	}
}
