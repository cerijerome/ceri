package ceri.ci.web;

import java.util.Objects;
import ceri.common.text.ToString;

public class Actor implements Comparable<Actor> {
	public final String name;
	public final String build;
	public final String job;

	public Actor(String name, String build, String job) {
		this.name = name;
		this.build = build;
		this.job = job;
	}

	public String getName() {
		return name;
	}

	public String getBuild() {
		return build;
	}

	public String getJob() {
		return job;
	}

	@Override
	public int compareTo(Actor actor) {
		return ActorComparators.DEFAULT.compare(this, actor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, build, job);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Actor)) return false;
		Actor other = (Actor) obj;
		return Objects.equals(name, other.name) && Objects.equals(build, other.build) &&
			Objects.equals(job, other.job);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, name, build, job);
	}

}
