package ceri.ci.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import ceri.common.comparator.Comparators;
import ceri.common.util.ToStringHelper;

/**
 * Summarizes events for a job.
 */
public class Summary {
	public final boolean broken;
	public final Collection<Event> lastBreaks;
	public final Event lastBreakAggr;
	public final Event lastFix;

	private Summary(Event lastFix, List<Event> sortedLastBreaks) {
		this.lastFix = lastFix;
		this.lastBreaks = Collections.unmodifiableList(sortedLastBreaks);
		lastBreakAggr = aggregateBreaks(sortedLastBreaks);
		broken = broken(this.lastFix, lastBreakAggr);
	}
	
	private boolean broken(Event lastFix, Event lastBreak) {
		if (lastBreak == null) return false;
		if (lastFix == null) return true;
		return lastFix.timeStamp < lastBreak.timeStamp;
	}
	
	private Event aggregateBreaks(Collection<Event> events) {
		if (events.isEmpty()) return null;
		long timeStamp = Long.MAX_VALUE;
		Set<String> names = new LinkedHashSet<>();
		for (Event event : events) {
			if (event.timeStamp < timeStamp) timeStamp = event.timeStamp;
			names.addAll(event.names);
		}
		return new Event(Event.Type.broken, timeStamp, names);
	}
	
	public static Summary create(Collection<Event> events) {
		if (events.isEmpty()) return new Summary(null, Collections.<Event>emptyList());
		List<Event> sortedEvents = new ArrayList<>(events);
		Collections.sort(sortedEvents, Comparators.reverse(EventComparators.TIMESTAMP));
		List<Event> sortedLastBreaks = sortedLastBreaks(sortedEvents);
		Event lastFix = lastFix(sortedEvents);
		return new Summary(lastFix, sortedLastBreaks);
	}

	private static Event lastFix(Collection<Event> sortedEvents) {
		Event lastFix = null;
		for (Event event : sortedEvents) {
			if (event.type == Event.Type.fixed) {
				lastFix = event;
			} else if (lastFix != null) break;
		}
		return lastFix;
	}

	private static List<Event> sortedLastBreaks(Collection<Event> sortedEvents) {
		List<Event> sortedLastBreaks = new ArrayList<>();
		for (Event event : sortedEvents) {
			if (event.type == Event.Type.broken) {
				sortedLastBreaks.add(event);
			} else if (!sortedLastBreaks.isEmpty()) break;
		}
		Collections.sort(sortedLastBreaks);
		return sortedLastBreaks;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, broken).field("lastFix", lastFix)
			.field("lastBreakAggr", lastBreakAggr).field("lastBreaks", lastBreaks).toString();
	}
	
}