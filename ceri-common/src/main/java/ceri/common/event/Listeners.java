package ceri.common.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Convenience class to track listeners and send notifications. There is no ConcurrentLinkedHashSet,
 * so choosing multiple ordered entries (list) over over single unordered entries (set). This means
 * listeners may register multiple times, and be notified multiple times. Thread safe.
 */
public class Listeners<T> implements Consumer<T>, Listenable<T> {
	private final Collection<Consumer<? super T>> listeners = new ConcurrentLinkedQueue<>();

	public static <T> Listeners<T> of() {
		return new Listeners<>();
	}

	protected Listeners() {}

	public int size() {
		return listeners().size();
	}

	public boolean isEmpty() {
		return listeners().isEmpty();
	}

	public void clear() {
		listeners().clear();
	}

	@Override
	public boolean listen(Consumer<? super T> listener) {
		return listeners().add(listener);
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
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

	protected Collection<Consumer<? super T>> listeners() {
		return listeners;
	}
}
