package ceri.common.function;

import java.util.concurrent.TimeUnit;

/**
 * An interruptible supplier that accepts a time value and unit.
 */
public interface TimedSupplier<E extends Exception, T> {
	/**
	 * Apply the time, and return the result.
	 */
	T get(long time, TimeUnit unit) throws E, InterruptedException;
}
