package ceri.common.event;

import java.util.function.IntConsumer;
import ceri.common.util.BasicUtil;
import ceri.common.util.Enclosed;

/**
 * Interface to add/remove notification listeners.
 */
public interface IntListenable {

	/**
	 * Attempts to listen, and returns a closable wrapper that unlistens on close. If the call to
	 * listen returns false, close() will do nothing.
	 */
	default <T extends IntConsumer> Enclosed<RuntimeException, T> enclose(T listener) {
		boolean added = listen(listener);
		if (!added) return Enclosed.noOp(listener); // no unlisten on close
		return Enclosed.of(listener, this::unlisten); // unlistens on close
	}

	/**
	 * Adds a listener to receive notifications. Returns true if added.
	 */
	boolean listen(IntConsumer listener);

	/**
	 * Removes a listener from receiving notifications. Returns true if removed.
	 */
	boolean unlisten(IntConsumer listener);

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
		return BasicUtil.uncheckedCast(Null.INSTANCE);
	}

	static class Null implements IntListenable, IntListenable.Indirect {
		private static final Null INSTANCE = new Null();

		private Null() {}

		@Override
		public IntListenable listeners() {
			return this;
		}

		@Override
		public boolean listen(IntConsumer listener) {
			return false;
		}

		@Override
		public boolean unlisten(IntConsumer listener) {
			return false;
		}

	}
}
