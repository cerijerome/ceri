package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.common.ee.LoggingExecutor;
import ceri.common.log.LogUtil;

/**
 * Container for alerter components. Executes alerts in parallel. - only allow one call at a time
 * (lock) - count active threads
 */
public class Alerters implements Closeable {
	static final Logger logger = LogManager.getLogger();
	private static final int ASYNC_COMPONENT_COUNT = 2; // x10, zwave
	private final LoggingExecutor executor;
	private final Lock lock = new ReentrantLock();
	public final X10Alerter x10;
	public final ZWaveAlerter zwave;
	public final AudioAlerter audio;
	public final WebAlerter web;

	public static class Builder {
		X10Alerter x10 = null;
		ZWaveAlerter zwave = null;
		AudioAlerter audio = null;
		WebAlerter web = null;

		Builder() {}

		public Builder x10(X10Alerter x10) {
			this.x10 = x10;
			return this;
		}

		public Builder zwave(ZWaveAlerter zwave) {
			this.zwave = zwave;
			return this;
		}

		public Builder audio(AudioAlerter audio) {
			this.audio = audio;
			return this;
		}

		public Builder web(WebAlerter web) {
			this.web = web;
			return this;
		}

		public Alerters build() {
			return new Alerters(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	Alerters(Builder builder) {
		x10 = builder.x10;
		zwave = builder.zwave;
		audio = builder.audio;
		web = builder.web;
		executor = new LoggingExecutor(Executors.newFixedThreadPool(ASYNC_COMPONENT_COUNT));
	}

	/**
	 * Notify alerters that builds have changed.
	 */
	public void alert(Builds builds) {
		logger.debug("alert: {}", LogUtil.compact(builds));
		lock.lock();
		try {
			final Builds summarizedBuilds = BuildUtil.summarize(builds);
			final Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
			if (x10 != null) executor.execute(() -> x10.alert(breakNames));
			if (zwave != null) executor.execute(() -> zwave.alert(breakNames));
			if (web != null) web.update(summarizedBuilds);
			if (audio != null) audio.alert(summarizedBuilds);
			executor.awaitCompletion();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear alerters' states.
	 */
	public void clear() {
		logger.debug("clear");
		lock.lock();
		try {
			if (x10 != null) executor.execute(() -> x10.clear());
			if (zwave != null) executor.execute(() -> zwave.clear());
			if (web != null) web.clear();
			if (audio != null) audio.clear();
			executor.awaitCompletion();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Remind alerters of current state.
	 */
	public void remind() {
		logger.debug("remind");
		lock.lock();
		try {
			if (audio != null) audio.remind();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		executor.close();
		if (audio != null) audio.interrupt();
	}

}
