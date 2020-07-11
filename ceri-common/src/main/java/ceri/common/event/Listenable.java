package ceri.common.event;

import java.util.function.Consumer;

/**
 * Interface to add/remove notification listeners.
 */
public interface Listenable<T> {

	/**
	 * Adds a listener to receive notifications. Returns true if added.
	 */
	boolean listen(Consumer<? super T> listener);

	/**
	 * Removes a listener from receiving notifications. Returns true if removed.
	 */
	boolean unlisten(Consumer<? super T> listener);

	/**
	 * Converts into an indirect listenable type.
	 */
	default Indirect<T> indirect() {
		return Indirect.from(this);
	}
	
	/**
	 * Interface to indirectly add/remove notification listeners. Useful when classes use a
	 * Listeners instance.
	 */
	static interface Indirect<T> {
		Listenable<T> listeners();

		/**
		 * Converts a listenable type into an indirect listenable type.
		 */
		static <T> Indirect<T> from(Listenable<T> listenable) {
			return () -> listenable;
		}
	}
}
