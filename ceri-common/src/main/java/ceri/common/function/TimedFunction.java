package ceri.common.function;

import java.util.concurrent.TimeUnit;

/**
 * An interruptible function that also accepts a time value and unit.
 */
public interface TimedFunction<E extends Exception, T, R> {
	/**
	 * Apply the value with time, and return the result.
	 */
	R apply(T t, long time, TimeUnit unit) throws E, InterruptedException;
}
