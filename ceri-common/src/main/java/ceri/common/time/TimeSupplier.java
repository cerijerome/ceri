package ceri.common.time;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import ceri.common.collection.Enums;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.Functions;
import ceri.common.math.Maths;

/**
 * Provide a time supplier by unit. Nanoseconds and microseconds provide elapsed time; all others
 * provide absolute times.
 */
public enum TimeSupplier {
	nanos(TimeUnit.NANOSECONDS), // elapsed time
	micros(TimeUnit.MICROSECONDS), // elapsed time
	millis(TimeUnit.MILLISECONDS), // absolute time
	seconds(TimeUnit.SECONDS), // absolute time
	minutes(TimeUnit.MINUTES), // absolute time
	hours(TimeUnit.HOURS), // absolute time
	days(TimeUnit.DAYS); // absolute time

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
				long ms = TimeUnit.MILLISECONDS.convert(1, unit);
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
				long ms = TimeUnit.MILLISECONDS.convert(1, unit);
				yield n -> ConcurrentUtil.delay(Maths.multiplyLimit(n, ms));
			}
		};
	}
}
