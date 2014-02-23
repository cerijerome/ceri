package ceri.x10.command;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;

public class CommandDispatcher implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int POLL_TIMEOUT_MS_DEF = 10000;
	private final long pollTimeoutMs;
	private final BlockingQueue<? extends BaseCommand<?>> queue;
	private final CommandListener listener;
	private final Thread thread;

	public CommandDispatcher(BlockingQueue<? extends BaseCommand<?>> queue, CommandListener listener) {
		this(queue, listener, POLL_TIMEOUT_MS_DEF);
	}

	public CommandDispatcher(BlockingQueue<? extends BaseCommand<?>> queue,
		CommandListener listener, long pollTimeoutMs) {
		this.queue = queue;
		this.listener = listener;
		this.pollTimeoutMs = pollTimeoutMs;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				CommandDispatcher.this.run();
			}
		});
		thread.start();
	}

	public static void dispatch(BaseCommand<?> command, CommandListener listener) {
		switch (command.type) {
		case ALL_UNITS_OFF:
			listener.allUnitsOff((HouseCommand) command);
			break;
		case ALL_LIGHTS_OFF:
			listener.allLightsOff((HouseCommand) command);
			break;
		case ALL_LIGHTS_ON:
			listener.allLightsOn((HouseCommand) command);
			break;
		case OFF:
			listener.off((UnitCommand) command);
			break;
		case ON:
			listener.on((UnitCommand) command);
			break;
		case DIM:
			listener.dim((DimCommand) command);
			break;
		case BRIGHT:
			listener.bright((DimCommand) command);
			break;
		case EXTENDED:
			listener.extended((ExtCommand) command);
			break;
		default:
			throw new UnsupportedOperationException("Function type not supported: " + command.type);
		}
	}

	@Override
	public void close() {
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
	}

	void run() {
		logger.info("Dispatcher thread started");
		try {
			process();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			logger.info("Dispatcher thread interrupted");
		} catch (RuntimeException e) {
			logger.catching(e);
		} finally {
			logger.info("Dispatcher thread stopped");
		}
	}

	private void process() throws InterruptedException {
		while (true) {
			try {
				BaseCommand<?> command = queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
				if (command != null && listener != null) {
					logger.info("Dispatching: {}", command);
					dispatch(command, listener);
				}
			} catch (RuntimeException e) {
				logger.catching(e);
			}
		}
	}

}
