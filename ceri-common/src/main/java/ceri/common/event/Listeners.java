package ceri.common.event;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class Listeners<T> implements Consumer<T>, Listenable<T> {
	private final Collection<Consumer<? super T>> listeners = new ConcurrentLinkedQueue<>();

	@Override
	public boolean listen(Consumer<? super T> listener) {
		return listeners.add(listener);
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		return listeners.remove(listener);
	}

	@Override
	public void accept(T value) {
		listeners.forEach(l -> l.accept(value));
	}

}
