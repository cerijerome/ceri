package ceri.ci.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import ceri.common.comparator.Comparators;

/**
 * Keeps state of fix/break events on a named job.
 */
public class Job implements Comparable<Job> {
	// Events sorted in order of descending time-stamp
	private final Collection<Event> mutableEvents = new TreeSet<>(Comparators
		.reverse(EventComparators.TIMESTAMP));
	public final Collection<Event> events = Collections.unmodifiableCollection(mutableEvents);
	public final String name;

	public Job(String name) {
		this.name = name;
	}

	/**
	 * Copy constructor 
	 */
	public Job(Job job) {
		this(job.name);
		mutableEvents.addAll(job.events);
	}

	public void event(Event event) {
		mutableEvents.add(event);
	}

	/**
	 * Clear all events.
	 */
	public void clear() {
		mutableEvents.clear();
	}

	/**
	 * Remove events up to the latest break and fix event sequences.
	 */
	public void purge() {
		boolean fixTransition = false;
		boolean breakTransition = false;
		Event lastEvent = null;
		Iterator<Event> i = mutableEvents.iterator();
		while (i.hasNext() && (!fixTransition || !breakTransition)) {
			Event event = i.next();
			// Check if transition from one event type to another
			if (lastEvent != null && !event.equals(lastEvent)) {
				if (event.type == Event.Type.broken) breakTransition = true;
				else fixTransition = true;
			}
			lastEvent = event;
		}
		// Remove remaining events
		while (i.hasNext())
			i.remove();
	}

	@Override
	public int compareTo(Job job) {
		return Comparators.STRING.compare(name, job.name);
	}
	
}
