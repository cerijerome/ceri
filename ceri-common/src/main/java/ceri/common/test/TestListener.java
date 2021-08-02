package ceri.common.test;

import java.io.Closeable;
import java.util.function.Consumer;
import ceri.common.concurrent.ValueCondition;
import ceri.common.event.Listenable;
import ceri.common.util.Enclosed;

/**
 * Use to wait for listen notifications in tests, and unlisten on close.
 */
public class TestListener<T> implements Closeable {
	public final ValueCondition<T> listen = ValueCondition.of();
	public final Enclosed<RuntimeException, Consumer<T>> listener;

	public static <T> TestListener<T> of(Listenable<T> listenable) {
		return new TestListener<>(listenable);
	}

	private TestListener(Listenable<T> listenable) {
		listener = listenable.enclose(listen::signal);
	}

	public Consumer<T> listener() {
		return listener.subject;
	}

	public T await() throws InterruptedException {
		return listen.await();
	}

	public T awaitClear() throws InterruptedException {
		listen.clear();
		return await();
	}

	@Override
	public void close() {
		listener.close();
	}
}
