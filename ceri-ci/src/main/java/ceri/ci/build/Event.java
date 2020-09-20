package ceri.ci.build;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import ceri.common.collection.ImmutableUtil;
import ceri.common.comparator.Comparators;
import ceri.common.text.ToString;

/**
 * Immutable successful or failed build event.
 */
public class Event implements Comparable<Event> {
	public final long timeStamp;
	public final Type type;
	public final Collection<String> names;

	public enum Type {
		success,
		failure;
	}

	public Event(Type type, Long timeStamp, String... names) {
		this(type, timeStamp, Arrays.asList(names));
	}

	public Event(Type type, Long timeStamp, Collection<String> names) {
		this.timeStamp = timeStamp == null ? System.currentTimeMillis() : timeStamp;
		this.type = type;
		this.names = ImmutableUtil.copyAsSet(names);
	}

	public Event(Event event) {
		this(event.type, event.timeStamp, event.names);
	}

	public static Event success(String... names) {
		return new Event(Type.success, null, names);
	}

	public static Event failure(String... names) {
		return new Event(Type.failure, null, names);
	}

	@Override
	public int compareTo(Event event) {
		return Comparators.LONG.compare(timeStamp, event.timeStamp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, timeStamp, names);
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
		return ToString.forClass(this, type, timeStamp, new Date(timeStamp), names);
	}

}
