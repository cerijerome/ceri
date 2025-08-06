package ceri.common.time;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import ceri.common.collection.Enums;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.Functions;
import ceri.common.math.MathUtil;

/**
 * Provide a time supplier by unit. Nanoseconds and microseconds provide elapsed time; all others
 * provide absolute times.
 */
public enum TimeSupplier {
	nanos(NANOSECONDS), // elapsed time
	micros(MICROSECONDS), // elapsed time
	millis(MILLISECONDS), // absolute time
	seconds(SECONDS), // absolute time
	minutes(MINUTES), // absolute time
	hours(HOURS), // absolute time
	days(DAYS); // absolute time

	private static final long MICRO_IN_NANOS = 1000;
	private static final Map<TimeUnit, TimeSupplier> map =
		Enums.map(t -> t.unit, TimeSupplier.class);
	public final Functions.LongSupplier supplier;
	public final TimeUnit unit;
	private final Functions.LongConsumer delayFn;

	public static TimeSupplier from(TimeUnit unit) {
		return map.get(unit);
	}

	private TimeSupplier(TimeUnit unit) {
		this.supplier = supplier(unit);
		this.delayFn = delayFn(unit);
		this.unit = unit;
	}

	public void delay(long count) {
		delayFn.accept(count);
	}

	public long time() {
		return supplier.getAsLong();
	}

	public String symbol() {
		return DateUtil.symbol(unit);
	}

	private static Functions.LongSupplier supplier(TimeUnit unit) {
		return switch (unit) {
			case NANOSECONDS -> System::nanoTime;
			case MICROSECONDS -> () -> System.nanoTime() / MICRO_IN_NANOS;
			case MILLISECONDS -> System::currentTimeMillis;
			default -> {
				long ms = MILLISECONDS.convert(1, unit);
				yield () -> System.currentTimeMillis() / ms;
			}
		};
	}

	private static Functions.LongConsumer delayFn(TimeUnit unit) {
		return switch (unit) {
			case NANOSECONDS -> ConcurrentUtil::delayNanos;
			case MICROSECONDS -> ConcurrentUtil::delayMicros;
			case MILLISECONDS -> ConcurrentUtil::delay;
			default -> {
				long ms = MILLISECONDS.convert(1, unit);
				yield n -> ConcurrentUtil.delay(MathUtil.multiplyLimit(n, ms));
			}
		};
	}
}
