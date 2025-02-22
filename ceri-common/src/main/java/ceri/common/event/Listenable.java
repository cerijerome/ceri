package ceri.common.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
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
	 * Adapts the listenable type for listeners to only receive filtered events.
	 */
	static <T> Listenable<T> filter(Listenable<T> listenable, Predicate<? super T> predicate) {
		return adapt(listenable, t -> predicate.test(t) ? t : null);
	}

	/**
	 * Adapts the listenable type for listeners to receive adapted events. If the adapter function
	 * returns null, the event is ignored.
	 */
	static <T, U> Listenable<T> adapt(Listenable<U> listenable,
		Function<? super U, ? extends T> adapter) {
		return new Adapter<>(listenable, adapter);
	}

	/**
	 * Adapter for listeners to filter or receive adapted events.
	 */
	static class Adapter<T, U> implements Listenable<T> {
		private final Map<Consumer<? super T>, Consumer<U>> lookup = new ConcurrentHashMap<>();
		private final Listenable<U> listenable;
		private final Function<? super U, ? extends T> adapter;

		private Adapter(Listenable<U> listenable, Function<? super U, ? extends T> adapter) {
			this.listenable = listenable;
			this.adapter = adapter;
		}

		@Override
		public boolean listen(Consumer<? super T> listener) {
			return listenable.listen(lookup.computeIfAbsent(listener, l -> t -> {
				var u = adapter.apply(t);
				if (u != null) l.accept(u);
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
