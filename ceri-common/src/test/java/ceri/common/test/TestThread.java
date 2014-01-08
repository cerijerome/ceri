package ceri.common.test;

/**
 * A runnable container for testing behavior in a thread.
 */
public abstract class TestThread {
	private static final int DEFAULT_WAIT_MS = 1000;
	private final Thread thread;
	private Exception error;
	private boolean completed;
	
	public TestThread() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				TestThread.this.execute();
			}
		});
	}
	
	protected abstract void run() throws Exception;
	
	public void start() {
		if (completed) throw new IllegalStateException("Thread is already running.");
		error = null;
		completed = false;
		thread.start();
	}
	
	public void stop() throws Exception {
		stop(DEFAULT_WAIT_MS);
	}
	
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
