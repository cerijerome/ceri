package ceri.ci.alert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import ceri.common.collection.ImmutableUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Event {
	public final long time;
	public final Collection<String> responsible;
	private final int hashCode;
	
	public static class Builder {
		long time = System.currentTimeMillis();
		final Collection<String> responsible = new HashSet<>();
		
		Builder() {
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
			return new Event(this);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	Event(Builder builder) {
		time = builder.time;
		responsible = ImmutableUtil.copyAsSet(builder.responsible);
		hashCode = HashCoder.hash(time, responsible);
	}
	
	public boolean after(Event event) {
		return time > event.time;
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
		if (time != other.time) return false;
		if (!EqualsUtil.equals(responsible, other.responsible)) return false;
		return true;
	}
	
}
