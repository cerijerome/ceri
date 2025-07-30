package ceri.log.concurrent;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.exception.ExceptionTracker;
import ceri.common.util.Enclosure;

/**
 * A dispatcher thread for notifying listeners of events. Useful to prevent a misbehaving listener
 * from slowing down processing. An adapter allows listener types to have multiple consuming methods
 * for the event type. If a single consumer is required, then the Direct nested class can be used.
 */
public class Dispatcher<L, T> extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private final long pollTimeoutMs;
	private final Function<T, Consumer<L>> adapter;
	private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
	private final Collection<L> listeners = new ConcurrentLinkedQueue<>();
	private final ExceptionTracker exceptions = ExceptionTracker.of();

	public static class Direct<T> extends Dispatcher<Consumer<T>, T> {
		private Direct(long pollTimeoutMs) {
			super(pollTimeoutMs, t -> l -> l.accept(t));
		}
	}

	public static <T> Direct<T> direct(long pollTimeoutMs) {
		return new Direct<>(pollTimeoutMs);
	}

	public static <L, T> Dispatcher<L, T> of(long pollTimeoutMs, Function<T, Consumer<L>> adapter) {
		return new Dispatcher<>(pollTimeoutMs, adapter);
	}

	private Dispatcher(long pollTimeoutMs, Function<T, Consumer<L>> adapter) {
		this.adapter = adapter;
		this.pollTimeoutMs = pollTimeoutMs;
		start();
	}

	public Enclosure<L> listen(L listener) {
		listeners.add(listener);
		return Enclosure.of(listener, listeners::remove);
	}

	public void dispatch(T t) {
		queue.add(t);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			T t = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
			// if (t == null || listeners.isEmpty()) return;
			if (t == null) return;
			if (listeners.isEmpty()) return;
			logger.debug("Dispatching: {}", t);
			Consumer<L> consumer = adapter.apply(t);
			listeners.forEach(l -> consumer.accept(l));
		} catch (InterruptedException | RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			if (exceptions.add(e)) logger.catching(Level.WARN, e);
		}
	}

}
