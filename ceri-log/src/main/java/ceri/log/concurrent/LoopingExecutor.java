package ceri.log.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Functions;
import ceri.log.util.LogUtil;

/**
 * Executes a runnable method in a repeating loop until an exception is thrown.
 */
public abstract class LoopingExecutor implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int EXIT_TIMEOUT_MS_DEF = 10000;
	private final int exitTimeoutMs;
	private final String logName;
	private final ExecutorService executor;
	private final BooleanCondition stopped = BooleanCondition.of();
	private volatile boolean closed = false;

	protected LoopingExecutor() {
		this(null);
	}

	protected LoopingExecutor(String logName) {
		this(logName, EXIT_TIMEOUT_MS_DEF);
	}

	protected LoopingExecutor(String logName, int exitTimeoutMs) {
		this.logName = logName == null ? getClass().getSimpleName() : logName;
		this.exitTimeoutMs = exitTimeoutMs;
		executor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Start the loop.
	 */
	protected void start() {
		executor.execute(this::loops);
	}

	/**
	 * Loop implementation.
	 */
	protected abstract void loop() throws Exception;

	/**
	 * Wait for the loop to stop.
	 */
	public void waitUntilStopped() throws InterruptedException {
		stopped.awaitPeek();
	}

	/**
	 * Wait for the loop to stop or timeout.
	 */
	public void waitUntilStopped(long timeoutMs) throws InterruptedException {
		stopped.awaitPeek(timeoutMs);
	}

	/**
	 * Returns true if the loop has stopped.
	 */
	public boolean stopped() {
		return stopped.isSet();
	}

	/**
	 * Returns true if close() been called.
	 */
	public boolean closed() {
		return closed;
	}

	@Override
	public void close() {
		closed = true;
		LogUtil.close(executor, exitTimeoutMs);
	}

	private void loops() {
		logger.info("{} started", logName);
		try {
			while (true) {
				ConcurrentUtil.checkInterrupted();
				loop();
			}
		} catch (InterruptedException | RuntimeInterruptedException e) {
			logger.debug("{} interrupted", logName);
		} catch (Exception e) {
			logger.catching(e);
		}
		logger.info("{} stopped", logName);
		stopped.signal();
	}

}
