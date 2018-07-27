package ceri.common.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import ceri.common.function.ExceptionConsumer;
import ceri.common.text.ToStringHelper;

/**
 * Timer to keep track of elapsed and remaining time. Not thread-safe.
 */
public class Timer {
	public static final Timer INFINITE = new Timer(-1);
	public static final Timer ZERO = new Timer(0);
	public final long periodMs;
	private State state = State.notStarted;
	private long started = 0;
	private long lastStart = 0;
	private long elapsed = 0;

	public static enum State {
		notStarted,
		started,
		paused,
		stopped;
	}

	public static class Snapshot {
		public final Timer timer;
		public final State state;
		public final long started;
		public final long current;
		public final long remaining;

		Snapshot(Timer timer, State state, long started, long current, long remaining) {
			this.timer = timer;
			this.state = state;
			this.started = started;
			this.current = current;
			this.remaining = remaining;
		}

		public boolean expired() {
			if (state == State.notStarted) return false;
			if (state == State.stopped) return true;
			if (timer.isInfinite()) return false;
			return remaining == 0;
		}

		public int remainingInt() {
			if (remaining <= Integer.MAX_VALUE) return (int) remaining;
			return Integer.MAX_VALUE;
		}

		public boolean infinite() {
			return timer.isInfinite();
		}

		public <E extends Exception> Timer.Snapshot
			applyRemaining(ExceptionConsumer<E, Integer> consumer) throws E {
			if (infinite() || remaining == 0) return this;
			consumer.accept(remainingInt());
			return this;
		}

		@Override
		public int hashCode() {
			return HashCoder.hash(timer, state, started, current, remaining);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (!(obj instanceof Snapshot)) return false;
			Snapshot other = (Snapshot) obj;
			if (!EqualsUtil.equals(timer, other.timer)) return false;
			if (!EqualsUtil.equals(state, other.state)) return false;
			if (started != other.started) return false;
			if (current != other.current) return false;
			if (remaining != other.remaining) return false;
			return true;
		}

		@Override
		public String toString() {
			return ToStringHelper
				.createByClass(this, timer.periodMs, state, started, current, remaining).toString();
		}

	}

	public static Timer of(long periodMs) {
		if (periodMs == ZERO.periodMs) return ZERO;
		if (periodMs == INFINITE.periodMs) return INFINITE;
		validateMin(periodMs, 0);
		return new Timer(periodMs);
	}

	private Timer(long periodMs) {
		this.periodMs = periodMs;
	}

	public Timer start() {
		started = System.currentTimeMillis();
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
		lastStart = System.currentTimeMillis();
		state = State.started;
		return true;
	}

	public void stop() {
		if (state == State.started) updateElapsed();
		state = State.stopped;
	}

	public boolean isZero() {
		return periodMs == ZERO.periodMs;
	}

	public boolean isInfinite() {
		return periodMs == INFINITE.periodMs;
	}

	public <E extends Exception> Timer.Snapshot
		applyRemaining(ExceptionConsumer<E, Integer> consumer) throws E {
		return snapshot().applyRemaining(consumer);
	}

	public Snapshot snapshot() {
		long t = System.currentTimeMillis();
		return new Snapshot(this, state, started, t, remaining(t));
	}

	private long remaining(long t) {
		if (isInfinite() || isZero()) return 0;
		long elapsed = this.elapsed;
		if (state == State.started) elapsed += (t - lastStart);
		return Math.max(0, periodMs - elapsed);
	}

	private void updateElapsed() {
		long t = System.currentTimeMillis();
		elapsed += (t - lastStart);
	}

}
