package ceri.common.time;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionLongConsumer;
import ceri.common.text.ToString;

/**
 * Timer to keep track of elapsed and remaining time. Can be paused/resumed multiple times without
 * resetting elapsed time. Not thread-safe.
 */
public class Timer {
	private static final int INFINITE_PERIOD = -1;
	public static final Timer INFINITE = millis(INFINITE_PERIOD);
	public static final Timer ZERO = millis(0);
	public final TimeUnit unit;
	public final long period;
	private final LongSupplier timeSupplier;
	private State state = State.notStarted;
	private long started = 0;
	private long lastStart = 0;
	private long elapsed = 0;

	/**
	 * Current state of the timer.
	 */
	public enum State {
		notStarted, // expired = false
		started,
		paused,
		stopped; // expired = true
	}

	/**
	 * Point-in-time snapshot for a timer. Remaining will be negative if time has progressed past
	 * the period.
	 */
	public static class Snapshot {
		public final Timer timer;
		public final State state;
		public final long started;
		public final long current;
		public final long remaining; // negative for time past period

		Snapshot(Timer timer, State state, long started, long current, long remaining) {
			this.timer = timer;
			this.state = state;
			this.started = started;
			this.current = current;
			this.remaining = remaining;
		}

		public TimeUnit unit() {
			return timer.unit;
		}

		public long period() {
			return timer.period;
		}

		public long elapsed() {
			return period() - remaining;
		}

		public boolean expired() {
			if (state == State.notStarted) return false;
			if (state == State.stopped) return true;
			if (timer.isInfinite()) return false;
			return remaining <= 0;
		}

		public int remainingInt() {
			if (remaining < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			if (remaining <= Integer.MAX_VALUE) return (int) remaining;
			return Integer.MAX_VALUE;
		}

		public boolean infinite() {
			return timer.isInfinite();
		}

		public <E extends Exception> Snapshot applyRemaining(ExceptionLongConsumer<E> consumer)
			throws E {
			if (infinite() || remaining <= 0) return this;
			consumer.accept(remaining);
			return this;
		}

		public <E extends Exception> Snapshot applyRemainingInt(ExceptionIntConsumer<E> consumer)
			throws E {
			if (infinite() || remaining <= 0) return this;
			consumer.accept(remainingInt());
			return this;
		}

		@Override
		public int hashCode() {
			return Objects.hash(timer, state, started, current, remaining);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Snapshot)) return false;
			Snapshot other = (Snapshot) obj;
			if (!Objects.equals(timer, other.timer)) return false;
			if (!Objects.equals(state, other.state)) return false;
			if (started != other.started) return false;
			if (current != other.current) return false;
			if (remaining != other.remaining) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, timer.period, DateUtil.symbol(timer.unit), state,
				started, current, remaining);
		}
	}

	/**
	 * Creates an infinite timer with millisecond granularity.
	 */
	public static Timer millis() {
		return millis(INFINITE_PERIOD);
	}

	/**
	 * Creates a timer with millisecond granularity.
	 */
	public static Timer millis(long periodMs) {
		return of(periodMs, TimeUnit.MILLISECONDS, System::currentTimeMillis);
	}

	/**
	 * Creates an infinite timer with microsecond granularity.
	 */
	public static Timer micros() {
		return micros(INFINITE_PERIOD);
	}

	/**
	 * Creates a timer with microsecond granularity.
	 */
	public static Timer micros(long periodUs) {
		return of(periodUs, TimeUnit.MICROSECONDS, ConcurrentUtil::microTime);
	}

	/**
	 * Creates an infinite timer with nanosecond granularity.
	 */
	public static Timer nanos() {
		return nanos(INFINITE_PERIOD);
	}

	/**
	 * Creates a timer with nanosecond granularity.
	 */
	public static Timer nanos(long periodNs) {
		return of(periodNs, TimeUnit.NANOSECONDS, System::nanoTime);
	}

	private static Timer of(long period, TimeUnit unit, LongSupplier timeSupplier) {
		validateMin(period, INFINITE_PERIOD);
		return new Timer(period, unit, timeSupplier);
	}

	private Timer(long period, TimeUnit unit, LongSupplier timeSupplier) {
		this.period = period;
		this.unit = unit;
		this.timeSupplier = timeSupplier;
	}

	public Timer start() {
		started = timeSupplier.getAsLong();
		lastStart = started;
		elapsed = 0;
		state = State.started;
		return this;
	}

	public boolean pause() {
		if (state == State.notStarted) return false;
		if (state == State.stopped) return false;
		updateElapsed();
		state = State.paused;
		return true;
	}

	public boolean resume() {
		if (state != State.paused) return false;
		lastStart = timeSupplier.getAsLong();
		state = State.started;
		return true;
	}

	public void stop() {
		if (state == State.started) updateElapsed();
		state = State.stopped;
	}

	public boolean isZero() {
		return period == 0;
	}

	public boolean isInfinite() {
		return period == INFINITE_PERIOD;
	}

	public <E extends Exception> Snapshot applyRemaining(ExceptionLongConsumer<E> consumer)
		throws E {
		return snapshot().applyRemaining(consumer);
	}

	public <E extends Exception> Snapshot applyRemainingInt(ExceptionIntConsumer<E> consumer)
		throws E {
		return snapshot().applyRemainingInt(consumer);
	}

	public Snapshot snapshot() {
		long t = timeSupplier.getAsLong();
		return new Snapshot(this, state, started, t, remaining(t));
	}

	private long remaining(long t) {
		if (isInfinite() || isZero()) return 0;
		long elapsed = this.elapsed;
		if (state == State.started) elapsed += (t - lastStart);
		return period - elapsed;
	}

	private void updateElapsed() {
		long t = timeSupplier.getAsLong();
		elapsed += (t - lastStart);
	}

}
