package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.service.CiAlertService;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.log.LogUtil;

public class AlertService implements CiAlertService, Closeable {
	private static final Logger logger = LogManager.getFormatterLogger(AlertService.class);
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Alerters alerters;
	private final long reminderMs;
	private final long shutdownTimeoutMs;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private boolean buildsChanged = false;

	public AlertService(Alerters alerters, long reminderMs, long shutdownTimeoutMs) {
		this.alerters = alerters;
		this.reminderMs = reminderMs;
		this.shutdownTimeoutMs = shutdownTimeoutMs;
		startThread();
	}

	public static void main(String[] args) {
		logger.info("info");
		logger.warn("warn");
		logger.error("error %x", 10000);
		logger.fatal("fatal");
		Exception e = new RuntimeException();
		logger.fatal(e);
		logger.throwing(e);
		logger.catching(Level.FATAL, e);
	}

	private void startThread() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				processAlerts();
			}
		});
	}

	@Override
	public void close() throws IOException {
		executor.shutdownNow();
		try {
			boolean shutdown = executor.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
			if (!shutdown) logger.warn("Processing thread did not shut down within limit of {} ms",
				shutdownTimeoutMs);
		} catch (InterruptedException e) {
			throw new InterruptedIOException();
		}
	}

	public void purge() {
		// @TODO: put on timer, or based on event count
		lock.lock();
		try {
			builds.purge();
			// No need for notification.
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			builds.clear();
			signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear(String build) {
		lock.lock();
		try {
			builds.build(build).clear();
			signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear(String build, String job) {
		lock.lock();
		try {
			builds.build(build).job(job).clear();
			signal();
		} finally {
			lock.unlock();
		}
	}

	public void fixed(String build, String job, String... names) {
		fixed(build, job, Arrays.asList(names));
	}

	@Override
	public void fixed(String build, String job, Collection<String> names) {
		lock.lock();
		try {
			builds.build(build).job(job).event(Event.fixed(names));
			signal();
		} finally {
			lock.unlock();
		}
	}

	public void broken(String build, String job, String... names) {
		broken(build, job, Arrays.asList(names));
	}

	@Override
	public void broken(String build, String job, Collection<String> names) {
		lock.lock();
		try {
			builds.build(build).job(job).event(Event.broken(names));
			signal();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Waits for builds to change with given millisecond timeout. If a clear was
	 * called then builds will be empty. Null is returned if the wait timeout
	 * expires.
	 */
	private Builds waitForAndCopyChangedBuilds(long ms) throws InterruptedException {
		lock.lock();
		try {
			while (!buildsChanged) {
				if (reminderMs == 0) condition.await();
				else if (!condition.await(ms, TimeUnit.MILLISECONDS)) return null;
			}
			buildsChanged = false;
			return new Builds(builds);
		} finally {
			lock.unlock();
		}
	}

	private void signal() {
		buildsChanged = true;
		condition.signal();
	}

	void processAlerts() {
		logger.info("Started processing thread: {}", Thread.currentThread());
		try {
			while (true) {
				logger.info("Waiting for signal");
				Builds builds = waitForAndCopyChangedBuilds(reminderMs);
				//logger.info("Alerting for builds: {}", builds);
				logger.info("Alerting for builds: {}", LogUtil.compact(builds));
				if (builds == null) alerters.remind();
				else if (builds.builds.isEmpty()) alerters.clear();
				else alerters.alert(builds);
			}
		} catch (RuntimeInterruptedException | InterruptedException e) {
			// interrupt exit request
			logger.catching(Level.INFO, e);
		}
		logger.info("Stopped processing thread: {}", Thread.currentThread());
	}

}
