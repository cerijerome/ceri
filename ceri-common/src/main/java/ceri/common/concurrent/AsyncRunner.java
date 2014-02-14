package ceri.common.concurrent;

import ceri.common.util.BasicUtil;

/**
 * Simple class for running a separate thread, capturing errors,
 * and surfacing them on the main thread on completion.
 * Most use cases better served with Future
 */
public abstract class AsyncRunner<T extends Exception> {
	private final Thread thread;
	private Class<T> errorClass;
	Exception error = null;

	public AsyncRunner(Class<T> errorClass) {
		this.errorClass = errorClass;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AsyncRunner.this.run();
				} catch (Exception e) {
					AsyncRunner.this.error = e;
				}
			}
		});
	}

	public void start() {
		thread.start();
	}

	public void interrupt() {
		thread.interrupt();
	}

	public void join(long ms) throws T {
		try {
			thread.join(ms);
		} catch (InterruptedException e) {
			// exit and let the next thread-wait interrupt
		} finally {
			thread.interrupt(); // does nothing if thread is complete
		}
		if (error != null) {
			if (errorClass.isInstance(error)) throw BasicUtil.<T>uncheckedCast(error);
			if (error instanceof RuntimeException) throw (RuntimeException)error;
			throw new RuntimeException(error);
		}
	}

	protected abstract void run() throws Exception;

}
