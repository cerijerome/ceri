package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Build;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.BuildEventProcessor;
import ceri.ci.build.Builds;
import ceri.ci.build.Job;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.ee.LoggingExecutor;
import ceri.common.log.LogUtil;

public class AlertService implements Closeable, BuildEventProcessor {
	private static final Logger logger = LogManager.getLogger();
	private final Alerters alerters;
	private final long reminderMs;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private final LoggingExecutor executor;
	private boolean buildsChanged = false;

	public AlertService(Alerters alerters, long reminderMs, long shutdownTimeoutMs) {
		this.alerters = alerters;
		this.reminderMs = reminderMs;
		executor = new LoggingExecutor(Executors.newSingleThreadExecutor(), shutdownTimeoutMs);
		executor.execute(() -> run());
	}

	// @TODO: put on timer, or based on event count
	public void purge() {
		logger.debug("purge");
		lock.lock();
		try {
			builds.purge();
			// No need for notification.
		} finally {
			lock.unlock();
		}
	}

	public Builds builds() {
		lock.lock();
		try {
			return new Builds(builds);
		} finally {
			lock.unlock();
		}
	}

	public Build build(String build) {
		lock.lock();
		try {
			return new Build(builds.build(build));
		} finally {
			lock.unlock();
		}
	}

	public Job job(String build, String job) {
		lock.lock();
		try {
			return new Job(builds.build(build).job(job));
		} finally {
			lock.unlock();
		}
	}

	public void clear(String build, String job) {
		logger.debug("clear: {}, {}", build, job);
		lock.lock();
		try {
			if (build == null) builds.clear();
			else if (job == null) builds.build(build).clear();
			else builds.build(build).job(job).clear();
			signal();
		} finally {
			lock.unlock();
		}
	}

	public void delete(String build, String job) {
		logger.debug("delete: {}, {}", build, job);
		lock.lock();
		try {
			if (build == null) builds.delete();
			else if (job == null) builds.delete(build);
			else builds.build(build).delete(job);
			// No need for notification.
		} finally {
			lock.unlock();
		}
	}

	public void process(BuildEvent... events) {
		process(Arrays.asList(events));
	}

	@Override
	public void process(Collection<BuildEvent> events) {
		lock.lock();
		try {
			for (BuildEvent event : events) {
				builds.build(event.build).job(event.job).event(event.event);
			}
			signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void close() throws IOException {
		executor.close();
	}

	/**
	 * Waits for builds to change with given millisecond timeout. If a clear was called then builds
	 * will be empty. Null is returned if the wait timeout expires.
	 */
	private Builds waitForAndCopyChangedBuilds(long ms) {
		lock.lock();
		try {
			while (!buildsChanged) {
				ConcurrentUtil.checkInterrupted();
				if (reminderMs == 0) condition.await();
				else if (!condition.await(ms, TimeUnit.MILLISECONDS)) return null;
			}
			buildsChanged = false;
			return new Builds(builds);
		} catch (InterruptedException e) {
			throw new RuntimeInterruptedException(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Must be called within a lock block.
	 */
	private void signal() {
		buildsChanged = true;
		condition.signal();
	}

	private void run() {
		while (true) {
			logger.debug("Waiting for signal");
			Builds builds = waitForAndCopyChangedBuilds(reminderMs);
			logger.debug("Alerting for builds: {}", LogUtil.compact(builds));
			if (builds == null) alerters.remind();
			else if (builds.builds.isEmpty()) alerters.clear();
			else alerters.alert(builds);
		}
	}

}
