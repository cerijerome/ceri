package ceri.common.event;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;

/**
 * Convenience class to track listeners and send notifications. There is no ConcurrentLinkedHashSet,
 * so choosing multiple ordered entries (list) over over single unordered entries (set). This means
 * listeners may register multiple times, and be notified multiple times. Thread safe.
 */
public class IntListeners implements IntConsumer, IntListenable {
	private final Collection<IntConsumer> listeners = new ConcurrentLinkedQueue<>();

	public static IntListeners of() {
		return new IntListeners();
	}

	protected IntListeners() {}

	public int size() {
		return listeners.size();
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

	@Override
	public boolean listen(IntConsumer listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean unlisten(IntConsumer listener) {
		return listeners.remove(listener);
	}

	/**
	 * Sends notification to listeners.
	 */
	@Override
	public void accept(int value) {
		listeners.forEach(l -> l.accept(value));
	}

}
