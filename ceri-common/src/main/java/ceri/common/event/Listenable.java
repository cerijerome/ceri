package ceri.common.event;

import java.util.function.Consumer;
import ceri.common.util.BasicUtil;
import ceri.common.util.Enclosed;

/**
 * Interface to add/remove notification listeners.
 */
public interface Listenable<T> {

	/**
	 * Attempts to listen, and returns a closable wrapper that unlistens on close. If the call to
	 * listen returns false, the listener is already registered, and close() will do nothing.
	 */
	default <U extends Consumer<? super T>> Enclosed<RuntimeException, U> enclose(U listener) {
		boolean added = listen(listener);
		if (!added) return Enclosed.noOp(listener); // no unlisten on close
		return Enclosed.of(listener, this::unlisten); // unlistens on close
	}

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

	/**
	 * Returns a typed, stateless, no-op listenable.
	 */
	static <T> Null<T> ofNull() {
		return BasicUtil.uncheckedCast(Null.INSTANCE);
	}

	static class Null<T> implements Listenable<T>, Listenable.Indirect<T> {
		private static final Null<?> INSTANCE = new Null<>();

		private Null() {}

		@Override
		public Listenable<T> listeners() {
			return this;
		}

		@Override
		public boolean listen(Consumer<? super T> listener) {
			return false;
		}

		@Override
		public boolean unlisten(Consumer<? super T> listener) {
			return false;
		}
	}
}
