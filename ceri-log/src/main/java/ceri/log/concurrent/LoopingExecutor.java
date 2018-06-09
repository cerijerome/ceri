package ceri.log.concurrent;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LoggingException;
import ceri.common.concurrent.BooleanCondition;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.ExceptionRunnable;
import ceri.log.util.LogUtil;

/**
 * Executes a runnable method in a repeating loop until an exception is thrown.
 */
public abstract class LoopingExecutor implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int EXIT_TIMEOUT_MS_DEF = 1000;
	private final int exitTimeoutMs;
	private final String logName;
	private final ExecutorService executor;
	private final BooleanCondition stopped = BooleanCondition.create();

	public static LoopingExecutor start(ExceptionRunnable<Exception> runnable) {
		return start(null, EXIT_TIMEOUT_MS_DEF, runnable);
	}

	public static LoopingExecutor start(String logName, int exitTimeoutMs,
		ExceptionRunnable<Exception> runnable) {
		if (logName == null) logName = LoggingException.class.getSimpleName();
		LoopingExecutor executor = new LoopingExecutor(logName, exitTimeoutMs) {
			@Override
			protected void loop() throws Exception {
				runnable.run();
			}
		};
		executor.start();
		return executor;
	}

	public LoopingExecutor() {
		this(null);
	}

	public LoopingExecutor(String logName) {
		this(logName, EXIT_TIMEOUT_MS_DEF);
	}

	public LoopingExecutor(String logName, int exitTimeoutMs) {
		this.logName = logName == null ? getClass().getSimpleName() : logName;
		this.exitTimeoutMs = exitTimeoutMs;
		executor = Executors.newSingleThreadExecutor();
	}

	protected void start() {
		executor.execute(this::loops);
	}

	protected abstract void loop() throws Exception;

	@Override
	public void close() {
		LogUtil.close(logger, executor, exitTimeoutMs);
	}

	public void waitUntilStopped() throws InterruptedException {
		stopped.await();
	}

	public void waitUntilStopped(long timeoutMs) throws InterruptedException {
		stopped.await(timeoutMs);
	}

	public boolean stopped() {
		return stopped.isSet();
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
