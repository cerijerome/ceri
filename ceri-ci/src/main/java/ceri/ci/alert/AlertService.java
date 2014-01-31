package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.service.CiAlertService;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;

public class AlertService implements CiAlertService, Closeable {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Alerters alerters;
	private final long reminderMs;
	private final long shutdownTimeoutMs;
	private final Lock lock = new ReentrantLock();
	private final Condition condition = lock.newCondition();
	private final Builds builds = new Builds();
	private boolean buildsChanged = false;

	public static void main(String[] args) throws IOException {
		try (AlertService service = new AlertService()) {
			service.broken("bolt", "smoke", Arrays.asList("cdehaudt"));
			BasicUtil.delay(10000);
			service.fixed("bolt", "smoke", Arrays.asList("cdehaudt"));
			service.broken("bolt", "regression", Arrays.asList("machung"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("dxie"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
			//BasicUtil.delay(10000);
		}
	}

	public AlertService() throws IOException {
		Properties properties = PropertyUtil.load(AlertService.class, "alert.properties");
		AlertServiceProperties serviceProps = new AlertServiceProperties(properties, "alert");
		alerters = new Alerters(properties, null);
		reminderMs = serviceProps.reminderMs();
		shutdownTimeoutMs = serviceProps.shutdownTimeoutMs();
		startThread();
	}

	AlertService(Alerters alerters, long reminderMs, long shutdownTimeoutMs) {
		this.alerters = alerters;
		this.reminderMs = reminderMs;
		this.shutdownTimeoutMs = shutdownTimeoutMs;
		startThread();
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
			executor.awaitTermination(shutdownTimeoutMs, TimeUnit.MILLISECONDS);
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
		try {
			while (true) {
				Builds builds = waitForAndCopyChangedBuilds(reminderMs);
				System.out.println("Here we go...");
				if (builds == null) alerters.remind();
				else if (builds.builds.isEmpty()) alerters.clear();
				else alerters.alert(builds);
			}
		} catch (RuntimeInterruptedException | InterruptedException e) {
			e.printStackTrace(System.out);
			// exit 
		}
	}

}
