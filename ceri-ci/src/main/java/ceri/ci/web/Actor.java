package ceri.ci.web;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class Actor implements Comparable<Actor> {
	public final String name;
	public final String build;
	public final String job;
	private final int hashCode;

	public Actor(String name, String build, String job) {
		this.name = name;
		this.build = build;
		this.job = job;
		hashCode = HashCoder.hash(name, build, job);
	}

	@Override
	public int compareTo(Actor actor) {
		return ActorComparators.DEFAULT.compare(this, actor);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Actor)) return false;
		Actor other = (Actor) obj;
		return EqualsUtil.equals(name, other.name) && EqualsUtil.equals(build, other.build) &&
			EqualsUtil.equals(job, other.job);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, name, build, job).toString();
	}

}
