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

	public enum State {
		ok,
		exceeded;
	}

	public EventTracker(int maxEvents, long windowMs) {
		this.maxEvents = maxEvents;
		this.windowMs = windowMs;
	}

	public State addEvent() {
		return addEvent(currentTimeMs());
	}

	public State addEvent(long t) {
		purge(t);
		timeStamps.add(t);
		if (timeStamps.size() > maxEvents) return State.exceeded;
		return State.ok;
	}

	public int events() {
		return timeStamps.size();
	}

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
