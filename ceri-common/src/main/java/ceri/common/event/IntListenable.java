package ceri.common.event;

import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;

/**
 * Interface to add/remove notification listeners.
 */
public interface IntListenable {

	/**
	 * Attempts to listen, and returns a closable wrapper that unlistens on close. If the call to
	 * listen returns false, close() will do nothing.
	 */
	default <T extends Functions.IntConsumer> Enclosure<T> enclose(T listener) {
		boolean added = listen(listener);
		if (!added) return Enclosure.noOp(listener); // no unlisten on close
		return Enclosure.of(listener, this::unlisten); // unlistens on close
	}

	/**
	 * Adds a listener to receive notifications. Returns true if added.
	 */
	boolean listen(Functions.IntConsumer listener);

	/**
	 * Removes a listener from receiving notifications. Returns true if removed.
	 */
	boolean unlisten(Functions.IntConsumer listener);

	/**
	 * Converts into an indirect listenable type.
	 */
	default Indirect indirect() {
		return Indirect.from(this);
	}

	/**
	 * Interface to indirectly add/remove notification listeners. Useful when classes use an
	 * IntListeners instance.
	 */
	interface Indirect {
		IntListenable listeners();

		/**
		 * Converts a listenable type into an indirect listenable type.
		 */
		static Indirect from(IntListenable listenable) {
			return () -> listenable;
		}
	}

	static Null ofNull() {
		return Reflect.unchecked(Null.INSTANCE);
	}

	interface Null extends IntListenable, IntListenable.Indirect {
		Null INSTANCE = new Null() {};

		@Override
		default IntListenable listeners() {
			return this;
		}

		@Override
		default boolean listen(Functions.IntConsumer listener) {
			return false;
		}

		@Override
		default boolean unlisten(Functions.IntConsumer listener) {
			return false;
		}
	}
}
