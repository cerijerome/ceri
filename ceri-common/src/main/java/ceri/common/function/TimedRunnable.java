package ceri.common.function;

import java.util.concurrent.TimeUnit;

/**
 * An interruptible runnable that accepts a time value and unit.
 */
public interface TimedRunnable<E extends Exception> {
	/**
	 * Apply the time.
	 */
	void run(long t, TimeUnit unit) throws E, InterruptedException;
}
