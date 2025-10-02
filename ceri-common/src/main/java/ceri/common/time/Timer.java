package ceri.common.time;

import java.util.concurrent.TimeUnit;
import ceri.common.function.Excepts;
import ceri.common.text.ToString;
import ceri.common.util.Validate;

/**
 * Timer to keep track of elapsed and remaining time. Must call start() to start the timer. Can be
 * paused/resumed multiple times without resetting elapsed time. Not thread-safe.
 */
public class Timer {
	private static final long INFINITE_PERIOD = -1L;
	public static final Timer INFINITE = infinite(TimeSupplier.millis);
	public static final Timer ZERO = of(0, TimeSupplier.millis);
	public final long period;
	public final TimeSupplier timeSupplier;
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
	public record Snapshot(Timer timer, State state, long started, long current, long remaining) {

		public TimeUnit unit() {
			return timer.timeSupplier.unit;
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

		public <E extends Exception> Snapshot applyRemaining(Excepts.LongConsumer<E> consumer)
			throws E {
			if (infinite() || remaining <= 0) return this;
			consumer.accept(remaining);
			return this;
		}

		public <E extends Exception> Snapshot applyRemainingInt(Excepts.IntConsumer<E> consumer)
			throws E {
			if (infinite() || remaining <= 0) return this;
			consumer.accept(remainingInt());
			return this;
		}

		@Override
		public String toString() {
			return ToString.forClass(this, timer.period, Dates.symbol(unit()), state, started,
				current, remaining);
		}
	}

	/**
	 * Creates an infinite timer with granularity of the given time unit.
	 */
	public static Timer infinite(TimeUnit unit) {
		return infinite(TimeSupplier.from(unit));
	}

	/**
	 * Creates an infinite timer with granularity of the given time unit.
	 */
	public static Timer infinite(TimeSupplier timeSupplier) {
		return of(INFINITE_PERIOD, timeSupplier);
	}

	/**
	 * Creates a timer with second granularity.
	 */
	public static Timer secs(long period) {
		return of(period, TimeUnit.SECONDS);
	}

	/**
	 * Creates a timer with millisecond granularity.
	 */
	public static Timer millis(long period) {
		return of(period, TimeUnit.MILLISECONDS);
	}

	/**
	 * Creates a timer with microsecond granularity.
	 */
	public static Timer micros(long period) {
		return of(period, TimeUnit.MICROSECONDS);
	}

	/**
	 * Creates a timer with nanosecond granularity.
	 */
	public static Timer nanos(long period) {
		return of(period, TimeUnit.NANOSECONDS);
	}

	/**
	 * Creates a timer with granularity of the given time unit.
	 */
	public static Timer of(long period, TimeUnit unit) {
		return new Timer(period, TimeSupplier.from(unit));
	}

	/**
	 * Creates a timer with granularity of the given time unit.
	 */
	public static Timer of(long period, TimeSupplier timeSupplier) {
		Validate.min(period, INFINITE_PERIOD);
		Validate.validateNotNull(timeSupplier);
		return new Timer(period, timeSupplier);
	}

	private Timer(long period, TimeSupplier timeSupplier) {
		this.period = period;
		this.timeSupplier = timeSupplier;
	}

	public Timer start() {
		started = timeSupplier.time();
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
		lastStart = timeSupplier.time();
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

	public <E extends Exception> Snapshot applyRemaining(Excepts.LongConsumer<E> consumer)
		throws E {
		return snapshot().applyRemaining(consumer);
	}

	public <E extends Exception> Snapshot applyRemainingInt(Excepts.IntConsumer<E> consumer)
		throws E {
		return snapshot().applyRemainingInt(consumer);
	}

	public Snapshot snapshot() {
		long t = timeSupplier.time();
		return new Snapshot(this, state, started, t, remaining(t));
	}

	private long remaining(long t) {
		if (isInfinite() || isZero()) return 0;
		long elapsed = this.elapsed;
		if (state == State.started) elapsed += (t - lastStart);
		return period - elapsed;
	}

	private void updateElapsed() {
		long t = timeSupplier.time();
		elapsed += (t - lastStart);
	}
}
