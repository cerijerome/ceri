package ceri.ci.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.ci.alert.Alerters;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class CiAlertServiceImpl implements CiAlertService, Closeable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Alerters alerters;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private boolean buildsChanged = false;
	
	public CiAlertServiceImpl(Alerters alerters) {
		this.alerters = alerters;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				processAlerts();
			}
		});
	}

	@Override
	public void close() throws IOException {
		executor.shutdown();
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

	private Builds waitForAndCopyChangedBuilds() throws InterruptedException {
		lock.lock();
		try {
			while (!buildsChanged) condition.await();
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
		try {
			while (true) {
				Builds builds = waitForAndCopyChangedBuilds();
				if (builds.builds.isEmpty()) alerters.clear();
				else alerters.alert(builds);
			}
		} catch (InterruptedException e) {
			// exit 
		}
	}

}
