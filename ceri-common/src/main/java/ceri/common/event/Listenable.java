package ceri.common.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
		/**
		 * Provides access to listen and unlisten to events.
		 */
		Listenable<T> listeners();

		/**
		 * Converts a listenable type into an indirect listenable type.
		 */
		static <T> Indirect<T> from(Listenable<T> listenable) {
			return () -> listenable;
		}
	}

	/**
	 * Returns an adapter for listeners to only receive filtered events.
	 */
	static <T> Listenable<T> filter(Listenable<T> listenable, Predicate<? super T> predicate) {
		return new Filter<>(listenable, predicate);
	}
	
	/**
	 * Adapter for listeners to only receive filtered events.
	 */
	static class Filter<T> implements Listenable<T> {
		private final Map<Consumer<? super T>, Consumer<T>> lookup = new ConcurrentHashMap<>();
		private final Listenable<T> listenable;
		private final Predicate<? super T> predicate;
		
		private Filter(Listenable<T> listenable, Predicate<? super T> predicate) {
			this.listenable = listenable;
			this.predicate = predicate;
		}
		
		@Override
		public boolean listen(Consumer<? super T> listener) {
			return listenable.listen(lookup.computeIfAbsent(listener, l -> t -> {
				if (predicate.test(t)) l.accept(t);
			}));
		}
		
		@Override
		public boolean unlisten(Consumer<? super T> listener) {
			var adapted = lookup.remove(listener);
			return (adapted != null) && listenable.unlisten(adapted); 
		}
	}
	
	/**
	 * Provides a no-op listenable type if null.
	 */
	static <T> Indirect<T> safe(Indirect<T> indirect) {
		return BasicUtil.defaultValue(indirect, Listenable::ofNull);
	}

	/**
	 * Provides a no-op listenable type if null.
	 */
	static <T> Listenable<T> safe(Listenable<T> listenable) {
		return BasicUtil.defaultValue(listenable, Listenable::ofNull);
	}

	/**
	 * Returns a typed, stateless, no-op listenable.
	 */
	static <T> Null<T> ofNull() {
		return BasicUtil.uncheckedCast(Null.INSTANCE);
	}

	/**
	 * No-op, stateless implementation.
	 */
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
