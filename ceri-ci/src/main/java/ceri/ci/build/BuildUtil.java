package ceri.ci.build;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

public class BuildUtil {

	private BuildUtil() {}

	/**
	 * Finds the earliest event of given type. 
	 */
	public static Event earliest(Event.Type type, Event... events) {
		return earliest(type, Arrays.asList(events));
	}
	
	/**
	 * Finds the earliest event of given type. 
	 */
	public static Event earliest(Event.Type type, Collection<Event> events) {
		long t = Long.MAX_VALUE;
		Event earliest = null;
		for (Event event : events) {
			if (event.type != type) continue;
			if (event.timeStamp > t) continue;
			t = event.timeStamp;
			earliest = event;
		}
		return earliest;
	}
	
	/**
	 * Finds the latest event of given type. 
	 */
	public static Event latest(Event.Type type, Event... events) {
		return latest(type, Arrays.asList(events));
	}
	
	/**
	 * Finds the latest event of given type. 
	 */
	public static Event latest(Event.Type type, Collection<Event> events) {
		long t = Long.MIN_VALUE;
		Event latest = null;
		for (Event event : events) {
			if (event.type != type) continue;
			if (event.timeStamp < t) continue;
			t = event.timeStamp;
			latest = event;
		}
		return latest;
	}
	
	/**
	 * Aggregates the events of given type into a single event.
	 * The earliest time-stamp is used, and all unique names are combined.
	 * If no events of the given type are found null is returned.
	 */
	public static Event aggregate(Event.Type type, Event... events) {
		return aggregate(type, Arrays.asList(events));
	}

	/**
	 * Aggregates the events of given type into a single event.
	 * The earliest time-stamp is used, and all unique names are combined.
	 * If no events of the given type are found null is returned.
	 */
	public static Event aggregate(Event.Type type, Collection<Event> events) {
		boolean found = false;
		long t = Long.MAX_VALUE;
		Collection<String> names = new LinkedHashSet<>();
		for (Event event : events) {
			if (event.type != type) continue;
			found = true;
			if (event.timeStamp < t) t = event.timeStamp;
			names.addAll(event.names);
		}
		if (!found) return null;
		return new Event(type, t, names);
	}

}
