package ceri.common.concurrent;

import ceri.common.function.ExceptionRunnable;
import ceri.common.util.BasicUtil;

/**
 * Simple class for running a separate thread, capturing errors, and surfacing them on the main
 * thread on completion. Most use cases better served with Future
 */
public abstract class AsyncRunner<T extends Exception> {
	private final Thread thread;
	private final Class<T> errorClass;
	Exception error = null;

	public static AsyncRunner<RuntimeException> create(ExceptionRunnable<?> runnable) {
		return create(RuntimeException.class, runnable);
	}
	
	public static <T extends Exception> AsyncRunner<T> create(Class<T> errorClass,
		ExceptionRunnable<?> runnable) {
		return new AsyncRunner<T>(errorClass) {
			@Override
			protected void run() throws Exception {
				runnable.run();
			}
		};
	}
	
	AsyncRunner(Class<T> errorClass) {
		this.errorClass = errorClass;
		thread = new Thread(() -> {
			try {
				AsyncRunner.this.run();
			} catch (Exception e) {
				AsyncRunner.this.error = e;
			}
		});
	}

	public AsyncRunner<T> start() {
		thread.start();
		return this;
	}

	public AsyncRunner<T> interrupt() {
		thread.interrupt();
		return this;
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
			BasicUtil.throwIfType(errorClass, error);
			BasicUtil.throwIfType(RuntimeException.class, error);
			throw new RuntimeException(error);
		}
	}

	protected abstract void run() throws Exception;

}
