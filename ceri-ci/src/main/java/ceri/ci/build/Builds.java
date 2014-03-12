package ceri.ci.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

public class Builds {
	private final Map<String, Build> mutableBuilds = new TreeMap<>();
	public final Collection<Build> builds = Collections.unmodifiableCollection(mutableBuilds
		.values());

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
	public int hashCode() {
		return HashCoder.hash(mutableBuilds);
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
		return ToStringHelper.createByClass(this).childrens(builds).toString();
	}
	
	private void add(Build build) {
		mutableBuilds.put(build.name, build);
	}

}
