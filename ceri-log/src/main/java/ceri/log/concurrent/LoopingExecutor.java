package ceri.log.concurrent;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.ExceptionRunnable;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.log.util.LogUtil;

/**
 * Executes a runnable method in a repeating loop until an exception is thrown.
 */
public abstract class LoopingExecutor implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int EXIT_TIMEOUT_MS_DEF = 1000;
	private final int exitTimeoutMs;
	private final ExecutorService executor;
	
	public static LoopingExecutor create(ExceptionRunnable<Exception> runnable) {
		return create(EXIT_TIMEOUT_MS_DEF, runnable);
	}
	
	public static LoopingExecutor create(int exitTimeoutMs, ExceptionRunnable<Exception> runnable) {
		return new LoopingExecutor(exitTimeoutMs) {
			@Override
			protected void loop() throws Exception {
				runnable.run();
			}
		};
	}
	
	public LoopingExecutor() {
		this(EXIT_TIMEOUT_MS_DEF);
	}

	public LoopingExecutor(int exitTimeoutMs) {
		this.exitTimeoutMs = exitTimeoutMs;
		executor = Executors.newSingleThreadExecutor();
	}

	protected void start() {
		logger.info("{} started", getClass().getSimpleName());
		executor.execute(this::loops);
	}
	
	protected abstract void loop() throws Exception;
	
	@Override
	public void close() {
		LogUtil.close(logger, executor, exitTimeoutMs);
		logger.info("{} stopped", getClass().getSimpleName());
	}

	private void loops() {
		try {
			while (true) {
				ConcurrentUtil.checkInterrupted();
				loop();
			}
		} catch (InterruptedException | RuntimeInterruptedException e) {
			logger.info("{} interrupted", getClass().getSimpleName());
		} catch (Exception e) {
			logger.catching(e);
		}
	}

}
