package ceri.ci.alert;

import java.io.Closeable;
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
import ceri.ci.common.LoggingExecutor;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.io.IoUtil;
import ceri.common.log.LogUtil;

/**
 * Service that manages the state of builds, and call update, remind, and clear on the
 * alerter group. Is thread-safe.
 */
public class AlertService implements Closeable, BuildEventProcessor {
	private static final Logger logger = LogManager.getLogger();
	private final AlerterGroup alerterGroup;
	private final long reminderMs;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private final LoggingExecutor executor;
	private boolean buildsChanged = false;

	public AlertService(AlerterGroup alerterGroup, long reminderMs, long shutdownTimeoutMs) {
		this.alerterGroup = alerterGroup;
		this.reminderMs = reminderMs;
		executor =
			new LoggingExecutor(Executors.newSingleThreadExecutor(), shutdownTimeoutMs, null);
		executor.execute(() -> run());
	}

	/**
	 * Purges older events from the builds.
	 * 
	 * @TODO: put on timer, or based on event count
	 */
	public void purge() {
		logger.info("Purging events");
		lock.lock();
		try {
			builds.purge();
			// No need for notification.
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns a copy of the state of all builds.
	 */
	public Builds builds() {
		lock.lock();
		try {
			return new Builds(builds);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns a copy of the state of the specified build.
	 */
	public Build build(String build) {
		lock.lock();
		try {
			return new Build(builds.build(build));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Returns a copy of the state of the specified job.
	 */
	public Job job(String build, String job) {
		lock.lock();
		try {
			return new Job(builds.build(build).job(job));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clears events from jobs. If build is null, all events are cleared. If job is null, all
	 * events for the build are cleared. Otherwise only events for job are cleared.
	 */
	public void clear(String build, String job) {
		logger.debug("Clearing events from {}/{}", build, job);
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

	/**
	 * Deletes builds and/or jobs. If build is null, all builds are deleted. If job is null, all
	 * jobs for the build are deleted. Otherwise only the job specified is deleted.
	 */
	public void delete(String build, String job) {
		logger.debug("Deleting {}/{}", build, job);
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

	/**
	 * Processes new build events.
	 */
	public void process(BuildEvent... events) {
		process(Arrays.asList(events));
	}

	/**
	 * Processes new build events. Any running alerts will complete, then these events will trigger
	 * the alerts again.
	 */
	@Override
	public void process(Collection<BuildEvent> events) {
		logger.info("Processing {} build events", events.size());
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
	public void close() {
		logger.info("Closing the alert service");
		IoUtil.close(executor);
	}

	/**
	 * Waits for builds to change with given millisecond timeout. If the clear method was called
	 * then builds will be empty. Null is returned if the wait timeout expires; this is the reminder
	 * timeout.
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
	 * Signals build state has changed. Must be called within a lock block.
	 */
	private void signal() {
		buildsChanged = true;
		condition.signal();
	}

	/**
	 * Execution thread method. Waits for build changes and calls update, remind, or clear on the
	 * alerters, depending on the change. Null indicates the wait timed out and a reminder will be
	 * sent. Empty builds indicate a full delete or clear was called and alerters should clear their
	 * states. Every other case will call update on the alerters.
	 */
	private void run() {
		while (true) {
			try {
				logger.debug("Waiting for signal");
				Builds builds = waitForAndCopyChangedBuilds(reminderMs);
				logger.debug("Alerting for builds: {}", LogUtil.compact(builds));
				if (builds == null) alerterGroup.remind();
				else if (builds.builds.isEmpty()) alerterGroup.clear();
				else alerterGroup.update(builds);
			} catch (RuntimeInterruptedException e) {
				throw e;
			} catch (RuntimeException e) {
				logger.catching(e);
			}
		}
	}

}
