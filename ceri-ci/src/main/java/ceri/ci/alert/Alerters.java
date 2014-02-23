package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.log.LogUtil;

/**
 * Container for alerter components. Executes alerts in parallel. - only allow
 * one call at a time (lock) - count active threads
 */
public class Alerters implements Closeable {
	static final Logger logger = LogManager.getLogger();
	private static final long TIMEOUT_MS_DEF = 5000;
	private static final int ASYNC_COMPONENT_COUNT = 2; // x10, zwave
	private final ExecutorService executor = Executors.newFixedThreadPool(ASYNC_COMPONENT_COUNT);
	private final Lock lock = new ReentrantLock();
	private final long timeoutMs;
	public final X10Alerter x10;
	public final ZWaveAlerter zwave;
	public final AudioAlerter audio;
	public final WebAlerter web;

	public static class Builder {
		long timeoutMs = TIMEOUT_MS_DEF;
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

		public Builder timeoutMs(long timeoutMs) {
			this.timeoutMs = timeoutMs;
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
		timeoutMs = builder.timeoutMs;
	}

	/**
	 * Notify alerters that builds have changed.
	 */
	public void alert(Builds builds) {
		logger.debug("Alert: {}", LogUtil.compact(builds));
		lock.lock();
		try {
			final Builds summarizedBuilds = BuildUtil.summarize(builds);
			final Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
			Future<?> x10Future = x10 == null ? null : execute(new Runnable() {
				@Override
				public void run() {
					x10.alert(breakNames);
				}
			});
			Future<?> zwaveFuture = zwave == null ? null : execute(new Runnable() {
				@Override
				public void run() {
					zwave.alert(breakNames);
				}
			});
			if (web != null) web.update(summarizedBuilds);
			if (audio != null) audio.alert(summarizedBuilds);
			awaitFuture(x10Future);
			awaitFuture(zwaveFuture);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear alerters' states.
	 */
	public void clear() {
		logger.debug("Clear");
		lock.lock();
		try {
			Future<?> x10Future = x10 == null ? null : execute(new Runnable() {
				@Override
				public void run() {
					x10.clear();
				}
			});
			Future<?> zwaveFuture = zwave == null ? null : execute(new Runnable() {
				@Override
				public void run() {
					zwave.clear();
				}
			});
			if (web != null) web.clear();
			if (audio != null) audio.clear();
			awaitFuture(x10Future);
			awaitFuture(zwaveFuture);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Remind alerters of current state.
	 */
	public void remind() {
		logger.debug("Remind");
		lock.lock();
		try {
			if (audio != null) audio.remind();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		logger.info("Shutting down any running threads");
		executor.shutdownNow();
		awaitTermination();
	}

	private Future<?> execute(final Runnable runnable) {
		return executor.submit(new Runnable() {
			@Override
			public void run() {
				logger.debug("Thread started");
				try {
					runnable.run();
				} catch (RuntimeInterruptedException e) {
					logger.info("Thread interrupted");
				} catch (RuntimeException e) {
					logger.catching(e);
				}
				logger.debug("Thread complete");
			}
		});
	}

	private void awaitFuture(Future<?> future) {
		if (future == null) return;
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	private boolean awaitTermination() {
		try {
			logger.debug("Awaiting termination of threads");
			boolean complete = executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
			if (!complete) logger.warn("Threads did not shut down in {}ms", timeoutMs);
			else logger.debug("Threads shut down successfully");
			return complete;
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		}
	}

}
