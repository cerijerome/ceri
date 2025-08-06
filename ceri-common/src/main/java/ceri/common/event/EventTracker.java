package ceri.common.event;

import java.util.LinkedList;
import java.util.List;

/**
 * Tracks timestamps of events within a window of time.
 */
public class EventTracker {
	private final int maxEvents;
	private final long windowMs;
	private final List<Long> timeStamps = new LinkedList<>();

	/**
	 * Creates a new instance.
	 */
	public static EventTracker of(int maxEvents, long windowMs) {
		return new EventTracker(maxEvents, windowMs);
	}

	protected EventTracker(int maxEvents, long windowMs) {
		this.maxEvents = maxEvents;
		this.windowMs = windowMs;
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
		purge(t);
		timeStamps.add(t);
		return timeStamps.size() <= maxEvents;
	}

	/**
	 * Returns the number of events in the current window.
	 */
	public int events() {
		return timeStamps.size();
	}

	/**
	 * Clears events.
	 */
	public void clear() {
		timeStamps.clear();
	}

	private void purge(long t) {
		long t0 = t - windowMs;
		timeStamps.removeIf(ts -> ts < t0);
	}

	long currentTimeMs() {
		return System.currentTimeMillis();
	}
}
