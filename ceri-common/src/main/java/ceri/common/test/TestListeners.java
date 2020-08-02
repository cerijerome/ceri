package ceri.common.test;

import java.util.Collection;
import java.util.function.Consumer;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.Listeners;

/**
 * Overrides Listeners to notify when listeners are added or removed.
 */
public class TestListeners<T> extends Listeners<T> {
	public final ValueCondition<Integer> sync = ValueCondition.of();

	public static <T> TestListeners<T> of() {
		return new TestListeners<>();
	}

	private TestListeners() {}

	@Override
	public void clear() {
		sync.clear();
		super.clear();
	}

	@Override
	public boolean listen(Consumer<? super T> listener) {
		boolean result = super.listen(listener);
		sync.signal(listeners().size());
		return result;
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		boolean result = super.unlisten(listener);
		sync.signal(listeners().size());
		return result;
	}

	@Override
	public Collection<Consumer<? super T>> listeners() {
		return super.listeners();
	}

	/**
	 * Wait for a change in listeners. Optionally clear the condition before waiting.
	 */
	public void await(boolean clear) throws InterruptedException {
		if (clear) sync.clear();
		sync.await();
	}

}
