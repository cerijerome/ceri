package ceri.ci.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ceri.ci.build.Build;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import ceri.ci.build.Job;

public class WebModel {
	private final WebParams params;
	private final Map<String, BuildModel> builds;

	public static class ItemModel {
		private final String job;
		private final String name;

		public ItemModel(String job, String name) {
			this.job = job;
			this.name = name;
		}

		public String getJob() {
			return job;
		}

		public String getName() {
			return name;
		}
	}

	public static class BuildModel {
		private final String name;
		private final List<ItemModel> villains;
		private final List<ItemModel> heroes;

		public BuildModel(String name, List<ItemModel> villains, List<ItemModel> heroes) {
			this.name = name;
			this.villains = villains;
			this.heroes = heroes;
		}

		public String getName() {
			return name;
		}

		public List<ItemModel> getVillains() {
			return villains;
		}

		public List<ItemModel> getHeroes() {
			return heroes;
		}
	}

	public WebModel(WebParams params, Builds builds) {
		this.params = params;
		Map<String, BuildModel> buildModels = new LinkedHashMap<>();
		for (Build build : builds.builds)
			buildModels.put(build.name, build(build));
		this.builds = Collections.unmodifiableMap(buildModels);
	}

	public WebParams getParams() {
		return params;
	}

	public Map<String, BuildModel> getBuilds() {
		return builds;
	}

	private BuildModel build(Build build) {
		return new BuildModel(build.name, items(build, Event.Type.failure), items(build,
			Event.Type.success));
	}

	private List<ItemModel> items(Build build, Event.Type type) {
		List<ItemModel> items = new ArrayList<>();
		for (Job job : build.jobs) {
			Event event = BuildUtil.latestEvent(job);
			if (event == null || event.type != type) continue;
			for (String name : event.names)
				items.add(new ItemModel(job.name, name));
		}
		return items;
	}

}
