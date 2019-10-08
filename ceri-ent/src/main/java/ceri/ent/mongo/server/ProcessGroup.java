package ceri.ent.mongo.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.ImmutableUtil;
import ceri.log.util.LogUtil;

public abstract class ProcessGroup implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final List<? extends ProcessRunner> runners;

	protected ProcessGroup(List<? extends ProcessRunner> runners) {
		this.runners = ImmutableUtil.copyAsList(runners);
	}

	public boolean isEmpty() {
		return runners.isEmpty();
	}

	public int count() {
		return runners.size();
	}

	public void stop() {
		for (ProcessRunner runner : runners) runner.stop();
	}

	public void waitFor() throws InterruptedException {
		for (ProcessRunner runner : runners) runner.waitFor();
	}

	public void verifyStartup() throws IOException {
		for (ProcessRunner runner : runners) runner.verifyStartup();
	}

	/**
	 * Make sure server has started - check server is alive after startup time has elapsed
	 */
	public void verifyStartup(long startupMs) throws IOException {
		for (ProcessRunner runner : runners) runner.verifyStartup(startupMs);
	}

	@Override
	public void close() {
		stop();
		LogUtil.close(logger, runners);
		logger.info("Process group stopped");
	}

}
