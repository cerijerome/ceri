package ceri.ci.build;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class BuildEvent {
	public final String build;
	public final String job;
	public final Event event;

	public BuildEvent(String build, String job, Event event) {
		this.build = build;
		this.job = job;
		this.event = event;
	}

	public void applyTo(Builds builds) {
		builds.build(build).job(job).event(event);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(build, job, event);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BuildEvent)) return false;
		BuildEvent other = (BuildEvent) obj;
		return EqualsUtil.equals(build, other.build) && EqualsUtil.equals(job, other.job)
			&& EqualsUtil.equals(event, other.event);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, build, job, event).toString();
	}
	
}
