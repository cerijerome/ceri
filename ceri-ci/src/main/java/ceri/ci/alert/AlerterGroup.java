package ceri.ci.alert;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;
import ceri.ci.common.LoggingExecutor;
import ceri.common.collection.ImmutableUtil;
import ceri.common.io.IoUtil;

/**
 * Container for alerter components. Executes alerter methods in parallel. Manages the summarized
 * builds state. Is thread-safe.
 */
public class AlerterGroup implements Alerter, Closeable {
	static final Logger logger = LogManager.getLogger();
	private final LoggingExecutor executor;
	private final Lock lock = new ReentrantLock();
	private final Collection<Alerter> alerters;

	public static class Builder {
		long shutdownTimeoutMs = 3000;
		final Collection<Alerter> alerters = new ArrayList<>();

		Builder() {}

		public Builder alerters(Alerter... alerters) {
			return alerters(Arrays.asList(alerters));
		}

		public Builder alerters(Collection<? extends Alerter> alerters) {
			this.alerters.addAll(alerters);
			return this;
		}

		public Builder shutdownTimeoutMs(long shutdownTimeoutMs) {
			this.shutdownTimeoutMs = shutdownTimeoutMs;
			return this;
		}

		public AlerterGroup build() {
			return new AlerterGroup(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	AlerterGroup(Builder builder) {
		alerters = ImmutableUtil.copyAsList(builder.alerters);
		int threadPoolSize = Math.max(1, alerters.size());
		ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);
		executor = new LoggingExecutor(service, builder.shutdownTimeoutMs, null);
	}

	/**
	 * Notify alerters that builds have changed.
	 */
	@Override
	public void update(Builds builds) {
		logger.info("Update");
		lock.lock();
		try {
			for (Alerter alerter : alerters)
				executor.execute(() -> alerter.update(builds));
			executor.awaitCompletion();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear alerters' states.
	 */
	@Override
	public void clear() {
		logger.info("Clear");
		lock.lock();
		try {
			for (Alerter alerter : alerters)
				executor.execute(alerter::clear);
			executor.awaitCompletion();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Remind alerters of current state.
	 */
	@Override
	public void remind() {
		logger.info("Remind");
		lock.lock();
		try {
			for (Alerter alerter : alerters)
				executor.execute(alerter::remind);
			executor.awaitCompletion();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() {
		logger.info("Closing");
		IoUtil.close(executor);
	}

}
