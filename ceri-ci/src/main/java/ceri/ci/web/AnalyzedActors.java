package ceri.ci.web;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class AnalyzedActors {
	public static AnalyzedActors EMPTY = new AnalyzedActors();
	public final Collection<Actor> heroes;
	public final Collection<Actor> villains;
	private final int hashCode;

	private AnalyzedActors() {
		heroes = Collections.emptySet();
		villains = Collections.emptySet();
		hashCode = HashCoder.hash(heroes, villains);
	}

	public AnalyzedActors(Builds builds) {
		Set<Actor> heroes = new TreeSet<>();
		Set<Actor> villains = new TreeSet<>();
		builds = BuildUtil.summarize(builds);
		analyze(builds, heroes, villains);
		this.heroes = Collections.unmodifiableSet(heroes);
		this.villains = Collections.unmodifiableSet(villains);
		hashCode = HashCoder.hash(this.heroes, this.villains);
	}

	public Collection<Actor> getHeroes() {
		return heroes;
	}
	
	public Collection<Actor> getVillains() {
		return villains;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AnalyzedActors)) return false;
		AnalyzedActors other = (AnalyzedActors) obj;
		return heroes.equals(other.heroes) && villains.equals(other.villains);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, heroes, villains).toString();
	}
	
	private void analyze(Builds builds, Set<Actor> heroes, Set<Actor> villains) {
		for (Build build : builds) {
			for (Job job : build.jobs) {
				Event event = BuildUtil.latestEvent(job);
				if (event == null) continue;
				if (event.type == Event.Type.failure) {
					add(villains, build.name, job.name, event.names);
					continue;
				}
				Event lastBreak = BuildUtil.latest(Event.Type.failure, job.events);
				if (lastBreak == null) continue;
				add(heroes, build.name, job.name, event.names);
			}
		}
	}

	private void add(Collection<Actor> actors, String build, String job, Collection<String> names) {
		for (String name : names)
			actors.add(new Actor(name, build, job));
	}

}
