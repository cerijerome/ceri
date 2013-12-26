package ceri.ci.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import ceri.common.comparator.Comparators;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Immutable encapsulation of Job. Includes name and history of events to happen
 * to the job.
 */
public class Job implements Comparable<Job> {
	public final String name;
	public final SortedSet<Event> events;
	public final Event lastEvent;
	private final int hashCode;

	private Job(String name, Collection<Event> events) {
		this.name = name;
		SortedSet<Event> sortedEvents = new TreeSet<>(events);
		for (Iterator<Event> i = sortedEvents.iterator(); i.hasNext();)
			if (i.next().isNull()) i.remove();
		this.events = Collections.unmodifiableSortedSet(sortedEvents);
		lastEvent = this.events.isEmpty() ? Event.NULL : this.events.last();
		hashCode = HashCoder.hash(name, events);
	}

	public static Job create(String name) {
		return new Job(name, Collections.<Event>emptyList());
	}

	public static Job create(String name, Event... events) {
		return new Job(name, Arrays.asList(events));
	}

	public Job createUpdate(Event... events) {
		Collection<Event> newEvents = new HashSet<>(this.events);
		Collections.addAll(newEvents, events);
		return new Job(name, newEvents);
	}

	@Override
	public int compareTo(Job job) {
		if (job == null) return 1;
		return Comparators.STRING.compare(name, job.name);
	}

	public static Event aggregate(Collection<Job> jobs) {
		Event.Type state = Event.Type.fixed;
		for (Job job : jobs)
			if (job.lastEvent.type == Event.Type.broken) {
				state = Event.Type.broken;
				break;
			}
		Event.Builder builder = Event.builder(state);
		for (Job job : jobs)
			if (job.lastEvent.type == state && !job.lastEvent.isNull()) builder
				.responsible(job.lastEvent.responsible);
		return builder.build();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Job)) return false;
		Job other = (Job) obj;
		if (!EqualsUtil.equals(name, other.name)) return false;
		if (!EqualsUtil.equals(events, other.events)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, lastEvent.type, events).toString();
	}

}
