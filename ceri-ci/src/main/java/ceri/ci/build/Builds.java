package ceri.ci.build;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import ceri.common.text.ToString;

public class Builds implements Iterable<Build>, BuildEventProcessor {
	private transient final Map<String, Build> mutableBuilds = new TreeMap<>();
	public final Collection<Build> builds =
		Collections.unmodifiableCollection(mutableBuilds.values());

	public Builds() {}

	public Builds(Builds builds) {
		for (Build build : builds.builds)
			add(new Build(build));
	}

	public Build build(String name) {
		Build build = mutableBuilds.get(name);
		if (build == null) {
			build = new Build(name);
			add(build);
		}
		return build;
	}

	public void clear() {
		for (Build build : builds)
			build.clear();
	}

	public void delete() {
		mutableBuilds.clear();
	}

	public void delete(String name) {
		mutableBuilds.remove(name);
	}

	public void purge() {
		for (Build build : builds)
			build.purge();
	}

	@Override
	public Iterator<Build> iterator() {
		return builds.iterator();
	}

	@Override
	public void process(BuildEvent... events) {
		process(Arrays.asList(events));
	}

	@Override
	public void process(Collection<BuildEvent> events) {
		for (BuildEvent event : events)
			event.applyTo(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mutableBuilds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Builds)) return false;
		Builds builds = (Builds) obj;
		return mutableBuilds.equals(builds.mutableBuilds);
	}

	@Override
	public String toString() {
		return ToString.ofClass(this).childrens(builds).toString();
	}

	private void add(Build build) {
		mutableBuilds.put(build.name, build);
	}

}
