package ceri.ent.mongo.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Functions;
import ceri.common.process.Parameters;
import ceri.common.process.ProcessUtil;
import ceri.log.util.LogUtil;

public class ProcessRunner implements Functions.Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final int STARTUP_TIME_MS_DEF = 1000;
	private final Process process;
	private final long startTimeMs;

	protected ProcessRunner(Parameters params) throws IOException {
		logger.info("Starting process: {}", params);
		process = new ProcessBuilder(params.list()).start();
		startTimeMs = System.currentTimeMillis();
	}

	public void stop() {
		process.destroy();
	}

	public String stdOut() {
		try {
			return ProcessUtil.stdOut(process);
		} catch (IOException e) {
			return "";
		}
	}

	public String stdErr() {
		try {
			return ProcessUtil.stdErr(process);
		} catch (IOException e) {
			return "";
		}
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	public void verifyStartup() throws IOException {
		verifyStartup(STARTUP_TIME_MS_DEF);
	}

	/**
	 * Make sure server has started - check server is alive after startup time has elapsed
	 */
	public void verifyStartup(long startupMs) throws IOException {
		try {
			long waitMs = Math.max(0, startTimeMs + startupMs - System.currentTimeMillis());
			if (!process.waitFor(waitMs, TimeUnit.MILLISECONDS)) return;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
		String stdOut = stdOut().trim();
		String stdErr = stdErr().trim();
		if (!stdOut.isEmpty()) logger.info("stdout:\n{}", stdOut);
		if (!stdErr.isEmpty()) logger.warn("stderr:\n{}", stdErr);
		throw new IOException("Process failed to start, code:" + process.exitValue());
	}

	@Override
	public void close() {
		LogUtil.close(process);
		logger.info("Process stopped");
	}

}
