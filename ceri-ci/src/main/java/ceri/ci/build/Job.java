package ceri.ci.build;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Keeps state of fix/break events on a named job. Events are sorted in order of
 * descending time-stamp.
 */
public class Job implements Iterable<Event> {
	private transient final Collection<Event> mutableEvents = new TreeSet<>(Comparators
		.reverse(EventComparators.TIMESTAMP));
	public final Collection<Event> events = Collections.unmodifiableCollection(mutableEvents);
	public final String name;

	public Job(String name) {
		if (BasicUtil.isEmpty(name)) throw new IllegalArgumentException("Name cannot be empty: " +
			name);
		this.name = name;
	}

	/**
	 * Copy constructor.
	 */
	public Job(Job job) {
		this(job.name);
		mutableEvents.addAll(job.events);
	}

	/**
	 * Apply new events to this job.
	 */
	public void event(Event... events) {
		events(Arrays.asList(events));
	}

	/**
	 * Apply new events to this job.
	 */
	public void events(Collection<Event> events) {
		mutableEvents.addAll(events);
	}

	/**
	 * Clear all events.
	 */
	public void clear() {
		mutableEvents.clear();
	}

	/**
	 * Remove events up to the latest break and fix event sequences. Checks for
	 * transitions from one event type to another, and keeps the latest of each.
	 */
	public void purge() {
		boolean fixTransition = false;
		boolean breakTransition = false;
		Event lastEvent = null;
		Iterator<Event> i = mutableEvents.iterator();
		while (i.hasNext() && (!fixTransition || !breakTransition)) {
			Event event = i.next();
			// Check if transition from one event type to another
			if (lastEvent != null && event.type != lastEvent.type) {
				if (event.type == Event.Type.failure) fixTransition = true;
				else breakTransition = true;
				if (breakTransition && fixTransition) i.remove();
			}
			lastEvent = event;
		}
		// Remove remaining events
		while (i.hasNext()) {
			i.next();
			i.remove();
		}
	}

	@Override
	public Iterator<Event> iterator() {
		return events.iterator();
	}
	
	@Override
	public int hashCode() {
		return HashCoder.hash(name, mutableEvents);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Job)) return false;
		Job job = (Job) obj;
		return name.equals(job.name) && mutableEvents.equals(job.mutableEvents);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name).childrens(mutableEvents).toString();
	}

}
