package ceri.common.test;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import ceri.common.collection.ArrayUtil;
import ceri.common.math.MathUtil;
import ceri.common.time.TimeSupplier;

/**
 * A utility to collect timing info on a loop with a delay.
 */
public class TimeCollector {
	private final PrintStream out;
	public final TimeSupplier supplier;
	public final long period;
	public final long total;
	public final int n;
	private final long[] start;
	private final long[] end;
	private final long[] diff;
	private int i = 0;

	/**
	 * Create an instance using millisecond units, and printing to stdout.
	 */
	public static TimeCollector millis(long total, long period) {
		return of(System.out, TimeUnit.MILLISECONDS, total, period);
	}

	/**
	 * Create an instance using microsecond units, and printing to stdout.
	 */
	public static TimeCollector micros(long total, long period) {
		return of(System.out, TimeUnit.MICROSECONDS, total, period);
	}

	/**
	 * Create an instance.
	 */
	public static TimeCollector of(PrintStream out, TimeUnit unit, long total, long period) {
		return new TimeCollector(out, unit, total, period);
	}

	private TimeCollector(PrintStream out, TimeUnit unit, long total, long period) {
		this.out = out;
		supplier = TimeSupplier.from(unit);
		this.total = total;
		this.period = period;
		n = (int) (total / period);
		start = new long[n];
		end = new long[n];
		diff = new long[n];
	}

	/**
	 * Start one period.
	 */
	public TimeCollector start() {
		start[i] = supplier.time();
		return this;
	}

	/**
	 * Complete one period.
	 */
	public TimeCollector end() {
		end[i] = supplier.time();
		diff[i] = end[i] - start[i];
		i++;
		return this;
	}

	/**
	 * Delay for the remaining time in the period.
	 */
	public TimeCollector delayRemaining() {
		return delayRemaining(0);
	}
	
	/**
	 * Delay for the remaining time in the period with an offset.
	 */
	public TimeCollector delayRemaining(int diff) {
		long d = start[i] + period + diff - supplier.time();
		if (d >= 0) supplier.delay(d);
		return this;
	}

	/**
	 * Delay for the period time.
	 */
	public TimeCollector delayPeriod() {
		return delayPeriod(0);
	}

	/**
	 * Delay for the period time with an offset.
	 */
	public TimeCollector delayPeriod(int diff) {
		supplier.delay(period + diff);
		return this;
	}

	/**
	 * Print the results, optionally printing the full diff array.
	 */
	public void report(boolean full) {
		long t = end[i - 1] - start[0];
		out.printf("total = %d [%d = %d x %d]\n", t, total, period, n);
		out.printf("mean = %.01f, median = %.01f, range = [%d..%d], [0] = %d, [%d] = %d\n",
			MathUtil.mean(diff, 0, i), MathUtil.median(diff.clone(), 0, i),
			MathUtil.min(diff, 0, i), MathUtil.max(diff, 0, i), diff[0], i - 1, diff[i - 1]);
		if (full) out.println(ArrayUtil.toString(diff, 0, i));
	}
}
