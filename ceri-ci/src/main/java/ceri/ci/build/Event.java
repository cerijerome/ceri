package ceri.ci.build;

import java.util.Collection;
import java.util.Date;
import ceri.common.collection.ImmutableUtil;
import ceri.common.comparator.Comparators;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Immutable fixed or broken event.
 */
public class Event implements Comparable<Event> {
	public final long timeStamp;
	public final Type type;
	public final Collection<String> names;
	private int hashCode;

	public static enum Type {
		fixed,
		broken;
	}

	Event(Type type, long timeStamp, Collection<String> names) {
		this.timeStamp = timeStamp;
		this.type = type;
		this.names = ImmutableUtil.copyAsSet(names);
		hashCode = HashCoder.hash(type, timeStamp, this.names);
	}

	public Event(Event event) {
		this(event.type, event.timeStamp, event.names);
	}
	
	public static Event fixed(Collection<String> names) {
		return new Event(Type.fixed, System.currentTimeMillis(), names);
	}

	public static Event broken(Collection<String> names) {
		return new Event(Type.broken, System.currentTimeMillis(), names);
	}

	@Override
	public int compareTo(Event event) {
		return Comparators.LONG.compare(timeStamp, event.timeStamp);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Event)) return false;
		Event other = (Event) obj;
		return type == other.type && timeStamp == other.timeStamp && names.equals(other.names);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, type, timeStamp, new Date(timeStamp), names)
			.toString();
	}

}
