package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Build;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.log.LogUtil;

public class AlertService implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final Alerters alerters;
	private final long reminderMs;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private final Thread thread;
	private boolean buildsChanged = false;

	public AlertService(Alerters alerters, long reminderMs) {
		this.alerters = alerters;
		this.reminderMs = reminderMs;
		thread = new Thread(() -> AlertService.this.run());
		thread.start();
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

	public Event fixed(String build, String job, String... names) {
		return fixed(build, job, Arrays.asList(names));
	}

	public Event fixed(String build, String job, Collection<String> names) {
		logger.debug("fixed: {}, {}, {}", build, job, names);
		return event(build, job, Event.fixed(names));
	}

	public Event broken(String build, String job, String... names) {
		return broken(build, job, Arrays.asList(names));
	}

	public Event broken(String build, String job, Collection<String> names) {
		logger.debug("broken: {}, {}, {}", build, job, names);
		return event(build, job, Event.broken(names));
	}

	@Override
	public void close() throws IOException {
		logger.debug("close");
		thread.interrupt();
		try {
			logger.debug("joining...");
			thread.join();
			logger.debug("join complete");
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
	}

	void run() {
		logger.info("Service thread started");
		try {
			process();
		} catch (InterruptedException | RuntimeInterruptedException e) {
			logger.info("Service thread interrupted");
		} catch (RuntimeException e) {
			logger.catching(e);
		} finally {
			logger.info("Service thread stopped");
		}
	}

	private Event event(String build, String job, Event event) {
		lock.lock();
		try {
			builds.build(build).job(job).event(event);
			signal();
			return event;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Waits for builds to change with given millisecond timeout. If a clear was called then builds
	 * will be empty. Null is returned if the wait timeout expires.
	 */
	private Builds waitForAndCopyChangedBuilds(long ms) throws InterruptedException {
		lock.lock();
		try {
			while (!buildsChanged) {
				ConcurrentUtil.checkInterrupted();
				if (reminderMs == 0) condition.await();
				else if (!condition.await(ms, TimeUnit.MILLISECONDS)) return null;
			}
			buildsChanged = false;
			logger.info("-------------------- buildsChanged = false");
			//logger.info("{}", builds);
			return new Builds(builds);
		} finally {
			lock.unlock();
		}
	}

	private void signal() {
		buildsChanged = true;
		logger.info("++++++++++++++++++++ buildsChanged = true");
		//logger.info("{}", builds);
		condition.signal();
	}

	void process() throws InterruptedException {
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
