package ceri.ci.build;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Builds {
	private final Map<String, Build> mutableBuilds = new TreeMap<>();
	public final Collection<Build> builds = Collections.unmodifiableCollection(mutableBuilds
		.values());

	public Builds() {}

	public Builds(Builds builds) {
		for (Build build : builds.builds)
			add(build);
	}

	public Build build(String name) {
		Build build = mutableBuilds.get(name);
		if (build == null) add(new Build(name));
		return build;
	}

	public void clear() {
		for (Build build : builds)
			build.clear();
	}

	public void purge() {
		for (Build build : builds)
			build.purge();
	}

	private void add(Build build) {
		mutableBuilds.put(build.name, build);
	}

}
