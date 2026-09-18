package ceri.common.concurrent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import ceri.common.function.Functions;

/**
 * Convenience class to track event listeners and send notifications. There is no
 * ConcurrentLinkedHashSet, so choosing multiple ordered entries (list) over over single unordered
 * entries (set). This means listeners may register multiple times, and be notified multiple times.
 * Thread safe.
 */
public class Listeners<T> implements Functions.Consumer<T>, Listenable<T> {
	private final Collection<Functions.Consumer<? super T>> listeners =
		new ConcurrentLinkedQueue<>();

	/**
	 * Convenience class to track int event listeners and send notifications. Thread safe.
	 */
	public static class OfInt implements Functions.IntConsumer, Listenable.OfInt {
		private final Collection<Functions.IntConsumer> listeners = new ConcurrentLinkedQueue<>();

		protected OfInt() {}

		/**
		 * Returns the number of current listeners.
		 */
		public int size() {
			return listeners().size();
		}

		/**
		 * Returns true if there are no current listeners.
		 */
		public boolean isEmpty() {
			return listeners().isEmpty();
		}

		/**
		 * Removes all listeners.
		 */
		public void clear() {
			listeners().clear();
		}

		@Override
		public boolean listen(Functions.IntConsumer listener) {
			return listeners().add(listener);
		}

		@Override
		public boolean unlisten(Functions.IntConsumer listener) {
			return listeners().remove(listener);
		}

		/**
		 * Sends notification to listeners.
		 */
		@Override
		public void accept(int value) {
			listeners().forEach(l -> l.accept(value));
		}

		/**
		 * Sends notification to listeners for each event.
		 */
		public void acceptAll(int... events) {
			for (var event : events)
				accept(event);
		}

		protected Collection<Functions.IntConsumer> listeners() {
			return listeners;
		}
	}

	/**
	 * Creates a new instance for int events.
	 */
	public static OfInt ofInt() {
		return new OfInt();
	}

	/**
	 * Creates a new instance for typed events.
	 */
	public static <T> Listeners<T> of() {
		return new Listeners<>();
	}

	protected Listeners() {}

	/**
	 * Returns the number of current listeners.
	 */
	public int size() {
		return listeners().size();
	}

	/**
	 * Returns true if there are no current listeners.
	 */
	public boolean isEmpty() {
		return listeners().isEmpty();
	}

	/**
	 * Removes all listeners.
	 */
	public void clear() {
		listeners().clear();
	}

	@Override
	public boolean listen(Functions.Consumer<? super T> listener) {
		return listeners().add(listener);
	}

	@Override
	public boolean unlisten(Functions.Consumer<? super T> listener) {
		return listeners().remove(listener);
	}

	/**
	 * Sends notification to listeners.
	 */
	@Override
	public void accept(T value) {
		listeners().forEach(l -> l.accept(value));
	}

	/**
	 * Sends notification to listeners for each event.
	 */
	@SafeVarargs
	public final void acceptAll(T... events) {
		acceptAll(Arrays.asList(events));
	}

	/**
	 * Sends notification to listeners for each event.
	 */
	public void acceptAll(Collection<T> events) {
		for (var event : events)
			accept(event);
	}

	protected Collection<Functions.Consumer<? super T>> listeners() {
		return listeners;
	}
}
