package ceri.common.test;

import ceri.common.function.ExceptionRunnable;
import ceri.common.util.BasicUtil;

/**
 * A runnable container for testing behavior in a thread.
 */
public abstract class TestThread<T extends Throwable> {
	private static final int DEFAULT_WAIT_MS = 1000;
	private final Thread thread;
	private T error;
	private boolean started = false;
	private volatile boolean completed;

	/**
	 * Creates the thread. Call start to execute.
	 */
	public TestThread() {
		thread = new Thread(() -> TestThread.this.execute());
	}

	/**
	 * Creates the thread. Call start to execute.
	 */
	public static <T extends Exception> TestThread<T> create(ExceptionRunnable<T> runnable) {
		return new TestThread<>() {
			@Override
			protected void run() throws T {
				runnable.run();
			}
		};
	}

	/**
	 * Override this to execute when the thread starts.
	 */
	protected abstract void run() throws T;

	/**
	 * Interrupt the thread.
	 */
	public void interrupt() {
		thread.interrupt();
	}

	/**
	 * Starts the thread. Will throw an exception if called more than once.
	 */
	public void start() {
		if (started) throw new IllegalStateException("Thread is already running.");
		error = null;
		completed = false;
		started = true;
		thread.start();
	}

	/**
	 * Attempts to stop the thread, waiting for a maximum time of 1 second. If the thread does not
	 * stop in the given time a RuntimeException will be thrown.
	 */
	public void stop() throws T {
		interrupt();
		join();
	}

	/**
	 * Attempts to stop the thread, waiting for given maximum number of milliseconds. A value of 0
	 * will wait indefinitely. If the thread does not stop in the given time a RuntimeException will
	 * be thrown.
	 */
	public void stop(int ms) throws T {
		interrupt();
		join(ms);
	}

	/**
	 * Waits for max of 1 second for thread to complete. A value of 0 will wait indefinitely. If the
	 * thread does not stop in the given time a RuntimeException will be thrown.
	 */
	public void join() throws T {
		join(DEFAULT_WAIT_MS);
	}

	/**
	 * Waits given maximum number of milliseconds for thread to complete. A value of 0 will wait
	 * indefinitely. If the thread does not stop in the given time a RuntimeException will be
	 * thrown.
	 */
	public void join(int ms) throws T {
		try {
			thread.join(ms);
			if (!started) throw new RuntimeException("Thread was not started");
			if (!completed()) throw new RuntimeException("Thread not complete");
			if (error != null) throw error;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks if the thread has completed.
	 */
	public boolean completed() {
		return completed;
	}

	void execute() {
		try {
			run();
		} catch (Throwable t) {
			error = BasicUtil.uncheckedCast(t);
		}
		completed = true;
	}

}
