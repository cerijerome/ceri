package ceri.common.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Excepts.RuntimeCloseable;
import ceri.common.util.CloseableUtil;

/**
 * A utility to propagate events to listeners in a separate thread.
 */
public class EventThread<T> implements RuntimeCloseable, Consumer<T>, Listenable.Indirect<T> {
	private final Listeners<T> listeners = Listeners.of();
	private final BlockingQueue<T> events = new LinkedBlockingQueue<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Consumer<? super RuntimeException> errorListener;

	public static <T> EventThread<T> of() {
		return of(null);
	}

	public static <T> EventThread<T> of(Consumer<? super RuntimeException> errorListener) {
		return new EventThread<>(errorListener);
	}

	private EventThread(Consumer<? super RuntimeException> errorListener) {
		this.errorListener = errorListener;
		executor.execute(this::loop);
	}

	@Override
	public Listenable<T> listeners() {
		return listeners;
	}

	@Override
	public void accept(T event) {
		events.offer(event);
	}

	@Override
	public void close() {
		CloseableUtil.close(executor);
	}

	private void loop() {
		while (true) {
			try {
				listeners.accept(events.take());
			} catch (RuntimeInterruptedException | InterruptedException e) {
				break; // stop requested
			} catch (RuntimeException e) {
				if (errorListener != null) errorListener.accept(e);
			}
		}
	}
}
