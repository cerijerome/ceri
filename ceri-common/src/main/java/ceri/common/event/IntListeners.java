package ceri.common.event;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;

public class IntListeners implements IntConsumer, IntListenable {
	private final Collection<IntConsumer> listeners = new ConcurrentLinkedQueue<>();

	@Override
	public boolean listen(IntConsumer listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean unlisten(IntConsumer listener) {
		return listeners.remove(listener);
	}

	@Override
	public void accept(int value) {
		listeners.forEach(l -> l.accept(value));
	}

}
