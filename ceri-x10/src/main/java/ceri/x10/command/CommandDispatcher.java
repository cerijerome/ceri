package ceri.x10.command;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.util.Enclosed;
import ceri.common.util.ExceptionTracker;
import ceri.log.concurrent.LoopingExecutor;

public class CommandDispatcher extends LoopingExecutor {
	private static final Logger logger = LogManager.getLogger();
	private final long pollTimeoutMs;
	private final BlockingQueue<? extends BaseCommand<?>> queue;
	private final Collection<CommandListener> listeners = new ConcurrentLinkedQueue<>();
	private final ExceptionTracker exceptions = ExceptionTracker.of();

	public CommandDispatcher(BlockingQueue<? extends BaseCommand<?>> queue, long pollTimeoutMs) {
		this.queue = queue;
		this.pollTimeoutMs = pollTimeoutMs;
		start();
	}

	public Enclosed<CommandListener> listen(CommandListener listener) {
		listeners.add(listener);
		return Enclosed.of(listener, listeners::remove);
	}

	@Override
	protected void loop() throws InterruptedException {
		try {
			BaseCommand<?> command = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
			if (command == null || listeners.isEmpty()) return;
			logger.debug("Dispatching: {}", command);
			Consumer<CommandListener> dispatchConsumer = CommandListener.dispatcher(command);
			listeners.forEach(dispatchConsumer::accept);
		} catch (InterruptedException | RuntimeInterruptedException e) {
			throw e;
		} catch (RuntimeException e) {
			if (exceptions.add(e)) logger.catching(Level.WARN, e);
		}
	}

}
