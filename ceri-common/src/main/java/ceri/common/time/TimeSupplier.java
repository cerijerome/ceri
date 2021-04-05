package ceri.common.time;

import static ceri.common.collection.ImmutableUtil.enumMap;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

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

	private static final Map<TimeUnit, TimeSupplier> map = enumMap(t -> t.unit, TimeSupplier.class);
	public final LongSupplier supplier;
	public final TimeUnit unit;

	public static TimeSupplier from(TimeUnit unit) {
		return map.get(unit);
	}

	private TimeSupplier(TimeUnit unit) {
		this.supplier = supplier(unit);
		this.unit = unit;
	}

	public long time() {
		return supplier.getAsLong();
	}

	private static LongSupplier supplier(TimeUnit unit) {
		return switch (unit) {
		case NANOSECONDS -> System::nanoTime;
		case MICROSECONDS -> {
			long ns = NANOSECONDS.convert(1, unit);
			yield () -> System.nanoTime() / ns;
		}
		case MILLISECONDS -> System::currentTimeMillis;
		default -> {
			long ms = MILLISECONDS.convert(1, unit);
			yield () -> System.currentTimeMillis() / ms;
		}
		};
	}
}
