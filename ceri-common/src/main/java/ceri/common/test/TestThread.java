package ceri.common.test;

/**
 * A runnable container for testing behavior in a thread.
 */
public abstract class TestThread {
	private static final int DEFAULT_WAIT_MS = 1000;
	private final Thread thread;
	private Exception error;
	private boolean completed;

	/**
	 * Creates the thread. Call start to execute.
	 */
	public TestThread() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				TestThread.this.execute();
			}
		});
	}

	/**
	 * Override this to execute when the thread starts.
	 */
	protected abstract void run() throws Exception;

	/**
	 * Starts the thread. Will throw an exception if called more than once.
	 */
	public void start() {
		if (completed) throw new IllegalStateException("Thread is already running.");
		error = null;
		completed = false;
		thread.start();
	}

	/**
	 * Attempts to stop the thread, waiting for a maximum time of 1 second. If
	 * the thread does not stop in the given time a RuntimeException will be
	 * thrown.
	 */
	public void stop() throws Exception {
		stop(DEFAULT_WAIT_MS);
	}

	/**
	 * Attempts to stop the thread, waiting for given maximum number of
	 * milliseconds. A value of 0 will wait indefinitely. If the thread does not
	 * stop in the given time a RuntimeException will be thrown.
	 */
	public void stop(int ms) throws Exception {
		thread.interrupt();
		try {
			thread.join(ms);
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
		} catch (Exception e) {
			error = e;
		}
		completed = true;
	}

}
