package ceri.common.time;

import java.util.LinkedList;
import java.util.List;
import ceri.common.concurrent.Locker;
import ceri.common.util.Basics;

/**
 * Tracks timestamps of events within a window of time.
 */
public class EventTracker {
	private final int maxEvents;
	private final long windowMs;
	private final TimeSupplier time;
	private final Locker locker;
	private final List<Long> timeStamps = new LinkedList<>();

	/**
	 * Creates a new non-thread-safe instance.
	 */
	public static EventTracker unsafe(int maxEvents, long windowMs) {
		return unsafe(null, maxEvents, windowMs);
	}

	/**
	 * Creates a new non-thread-safe instance.
	 */
	public static EventTracker unsafe(TimeSupplier time, int maxEvents, long window) {
		return new EventTracker(time, null, maxEvents, window);
	}

	/**
	 * Creates a new thread-safe instance.
	 */
	public static EventTracker of(int maxEvents, long windowMs) {
		return of(null, maxEvents, windowMs);
	}

	/**
	 * Creates a new thread-safe instance with time supplier.
	 */
	public static EventTracker of(TimeSupplier time, int maxEvents, long window) {
		return new EventTracker(time, Locker.of(), maxEvents, window);
	}

	protected EventTracker(TimeSupplier time, Locker locker, int maxEvents, long window) {
		this.time = Basics.def(time, TimeSupplier.millis);
		this.maxEvents = maxEvents;
		this.windowMs = window;
		this.locker = Basics.def(locker, Locker.NULL);
	}

	/**
	 * Purges the current window and adds an event. Returns false if the max events have been
	 * exceeded.
	 */
	public boolean add() {
		return add(currentTimeMs());
	}

	/**
	 * Purges the current window and adds an event. Returns false if the max events have been
	 * exceeded.
	 */
	public boolean add(long t) {
		return locker.getAsInt(() -> {
			purge(t);
			timeStamps.add(t);
			return timeStamps.size();
		}) <= maxEvents;
	}

	/**
	 * Returns the number of events in the current window.
	 */
	public int events() {
		return locker.getAsInt(timeStamps::size);
	}

	/**
	 * Clears events.
	 */
	public void clear() {
		locker.run(timeStamps::clear);
	}

	private void purge(long t) {
		long t0 = t - windowMs;
		timeStamps.removeIf(ts -> ts < t0);
	}

	long currentTimeMs() {
		return time.time();
	}
}
