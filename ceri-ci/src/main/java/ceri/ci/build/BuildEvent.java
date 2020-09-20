package ceri.ci.build;

import java.util.Objects;
import ceri.common.text.ToString;

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
		builds.build(build).job(job).events(event);
	}

	@Override
	public int hashCode() {
		return Objects.hash(build, job, event);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BuildEvent)) return false;
		BuildEvent other = (BuildEvent) obj;
		return Objects.equals(build, other.build) && Objects.equals(job, other.job) &&
			Objects.equals(event, other.event);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, build, job, event);
	}

}
