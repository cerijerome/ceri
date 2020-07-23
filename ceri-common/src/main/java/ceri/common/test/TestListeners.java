package ceri.common.test;

import java.util.Collection;
import java.util.function.Consumer;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.event.Listeners;

/**
 * Overrides Listeners to notify when listeners are added or removed.
 */
public class TestListeners<T> extends Listeners<T> {
	public final BooleanCondition sync = BooleanCondition.of();

	@Override
	public boolean listen(Consumer<? super T> listener) {
		boolean result = super.listen(listener);
		sync.signal();
		return result;
	}

	@Override
	public boolean unlisten(Consumer<? super T> listener) {
		boolean result = super.unlisten(listener);
		sync.signal();
		return result;
	}

	@Override
	public Collection<Consumer<? super T>> listeners() {
		return super.listeners();
	}
	
	public void await(boolean clear) throws InterruptedException {
		if (clear) sync.clear();
		sync.await();
	}
	
}
