package ceri.common.test;

import java.io.Closeable;
import java.util.function.Consumer;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.CloseableListener;
import ceri.common.event.Listenable;

/**
 * Use to wait for listen notifications in tests, and unlisten on close.
 */
public class TestListener<T> implements Closeable {
	public final ValueCondition<T> listen = ValueCondition.of();
	public final CloseableListener<T> listener;

	public static <T> TestListener<T> of(Listenable<T> listenable) {
		return new TestListener<>(listenable);
	}
	
	private TestListener(Listenable<T> listenable) {
		listener = CloseableListener.of(listenable, listen::signal);
	}

	public Consumer<T> listener() {
		return listener.listener;
	}
	
	public Listenable<T> listenable() {
		return listener.listenable;
	}
	
	public T await(boolean clear) throws InterruptedException {
		if (clear) listen.clear();
		return listen.await();
	}
	
	@Override
	public void close() {
		listener.close();
	}
}
