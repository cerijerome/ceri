package ceri.common.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Tracks timestamps of events within a window of time.
 */
public class EventTracker {
	private final int maxEvents;
	private final long windowMs;
	private final List<Long> timeStamps = new LinkedList<>();

	public static enum State {
		ok, exceeded;
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
		Iterator<Long> i = timeStamps.iterator();
		long t0 = t - windowMs;
		while (i.hasNext()) if (i.next().longValue() < t0) i.remove();
	}

	long currentTimeMs() {
		return System.currentTimeMillis();
	}

}
