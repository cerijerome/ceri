package ceri.ci.job;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import ceri.common.collection.ImmutableUtil;
import ceri.common.comparator.Comparators;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class Event implements Comparable<Event> {
	public static final Event NULL = builder(Type.fixed).time(0).build();
	public final long time;
	public final Type type;
	public final Collection<String> responsible;
	private final int hashCode;
	
	public static enum Type { broken, fixed }
	
	public static class Builder {
		final Type type;
		long time = System.currentTimeMillis();
		final Collection<String> responsible = new HashSet<>();
		
		Builder(Type type) {
			this.type = type;
		}
		
		public Builder time(long time) {
			this.time = time;
			return this;
		}
		
		public Builder responsible(String...responsible) {
			Collections.addAll(this.responsible, responsible);
			return this;
		}
		
		public Builder responsible(Collection<String> responsible) {
			this.responsible.addAll(responsible);
			return this;
		}
		
		public Event build() {
			Event event = new Event(this);
			if (event.equals(NULL)) return NULL;
			return event;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(NULL);
	}
	
	public static Builder builder(Type type) {
		return new Builder(type);
	}
	
	Event(Builder builder) {
		type = builder.type;
		time = builder.time;
		responsible = ImmutableUtil.copyAsSet(builder.responsible);
		hashCode = HashCoder.hash(time, responsible);
	}
	
	public boolean isNull() {
		return this == NULL;
	}
	
	public boolean after(Event event) {
		return time > event.time;
	}
	
	@Override
	public int compareTo(Event event) {
		if (event == null) return 1;
		return Comparators.LONG.compare(time,  event.time);
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
		if (type != other.type) return false;
		if (time != other.time) return false;
		if (!EqualsUtil.equals(responsible, other.responsible)) return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, type, responsible, time).toString();
	}
	
}
