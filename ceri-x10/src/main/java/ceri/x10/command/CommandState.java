package ceri.x10.command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maintains state for re-sending commands that have failed. Keeps track of retries and logs when
 * maximum attempts have been exceeded. Manages pulling the next command from the queue.
 */
public class CommandState {
	private static final Logger logger = LogManager.getLogger();
	private final BlockingQueue<? extends BaseCommand<?>> queue;
	private final int maxSendAttempts;
	private final long pollTimeoutMs;
	private BaseCommand<?> command = null;
	private int sendAttempt = 0;

	public CommandState(BlockingQueue<? extends BaseCommand<?>> queue,
		int maxSendAttempts, long pollTimeoutMs) {
		this.maxSendAttempts = maxSendAttempts;
		this.queue = queue;
		this.pollTimeoutMs = pollTimeoutMs;
	}

	public BaseCommand<?> command() throws InterruptedException {
		if (command != null) {
			if (sendAttempt++ < maxSendAttempts) return command;
			logger.error("Exceeded command send attempts: " + sendAttempt);
			reset();
		}
		return queue.poll(pollTimeoutMs, TimeUnit.MILLISECONDS);
	}

	public void success() {
		reset();
	}

	private void reset() {
		command = null;
		sendAttempt = 0;
	}

}
