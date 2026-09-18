package ceri.common.concurrent;

import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Basics;

/**
 * Interface to add/remove notification listeners.
 */
public interface Listenable<T> {
	/**
	 * Interface to add/remove int notification listeners.
	 */
	interface OfInt {
		/** A no-op stateless instance. */
		Null NULL = new Null() {};

		/**
		 * Attempts to listen, and returns a closable wrapper that unlistens on close. If the call
		 * to listen returns false, close() will do nothing.
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
			return () -> this;
		}

		/**
		 * Interface to indirectly add/remove notification listeners. Useful when classes use a
		 * listeners instance.
		 */
		interface Indirect {
			OfInt listeners();
		}

		/**
		 * A no-op stateless implementation.
		 */
		interface Null extends OfInt, OfInt.Indirect {
			@Override
			default OfInt listeners() {
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

	/**
	 * Attempts to listen, and returns a closable wrapper that unlistens on close. If the call to
	 * listen returns false, the listener is already registered, and close() will do nothing.
	 */
	default <U extends Functions.Consumer<? super T>> Enclosure<U> enclose(U listener) {
		boolean added = listen(listener);
		if (!added) return Enclosure.noOp(listener); // no unlisten on close
		return Enclosure.of(listener, this::unlisten); // unlistens on close
	}

	/**
	 * Adds a listener to receive notifications. Returns true if added.
	 */
	boolean listen(Functions.Consumer<? super T> listener);

	/**
	 * Removes a listener from receiving notifications. Returns true if removed.
	 */
	boolean unlisten(Functions.Consumer<? super T> listener);

	/**
	 * Converts into an indirect listenable type.
	 */
	default Indirect<T> indirect() {
		return () -> this;
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
	}

	/**
	 * Adapts the listenable type for listeners to only receive filtered events.
	 */
	static <T> Listenable<T> filter(Listenable<T> listenable,
		Functions.Predicate<? super T> predicate) {
		return adapt(listenable, t -> predicate.test(t) ? t : null);
	}

	/**
	 * Adapts the listenable type for listeners to receive adapted events. If the adapter function
	 * returns null, the event is ignored.
	 */
	static <T, U> Listenable<T> adapt(Listenable<U> listenable,
		Functions.Function<? super U, ? extends T> adapter) {
		return new Adapter<>(listenable, adapter);
	}

	/**
	 * Adapter for listeners to filter or receive adapted events.
	 */
	static class Adapter<T, U> implements Listenable<T> {
		private final Map<Functions.Consumer<? super T>, Functions.Consumer<U>> lookup =
			Maps.concurrent();
		private final Listenable<U> listenable;
		private final Functions.Function<? super U, ? extends T> adapter;

		private Adapter(Listenable<U> listenable,
			Functions.Function<? super U, ? extends T> adapter) {
			this.listenable = listenable;
			this.adapter = adapter;
		}

		@Override
		public boolean listen(Functions.Consumer<? super T> listener) {
			return listenable.listen(lookup.computeIfAbsent(listener, l -> t -> {
				var u = adapter.apply(t);
				if (u != null) l.accept(u);
			}));
		}

		@Override
		public boolean unlisten(Functions.Consumer<? super T> listener) {
			var adapted = lookup.remove(listener);
			return (adapted != null) && listenable.unlisten(adapted);
		}
	}

	/**
	 * Provides a no-op listenable type if null.
	 */
	static <T> Indirect<T> safe(Indirect<T> indirect) {
		return Basics.def(indirect, Listenable::ofNull);
	}

	/**
	 * Provides a no-op listenable type if null.
	 */
	static <T> Listenable<T> safe(Listenable<T> listenable) {
		return Basics.def(listenable, Listenable::ofNull);
	}

	/**
	 * Returns a typed, stateless, no-op listenable.
	 */
	static <T> Null<T> ofNull() {
		return Reflect.unchecked(Null.INSTANCE);
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
		public boolean listen(Functions.Consumer<? super T> listener) {
			return false;
		}

		@Override
		public boolean unlisten(Functions.Consumer<? super T> listener) {
			return false;
		}
	}
}
