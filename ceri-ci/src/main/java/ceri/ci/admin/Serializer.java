package ceri.ci.admin;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import ceri.ci.build.Build;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Builds;
import ceri.ci.build.Job;
import ceri.common.util.BasicUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Serializer {
	private static final Type buildEventCollectionType = new TypeToken<Collection<BuildEvent>>() {}
	.getType();
	private final Gson gson;

	public Serializer() {
		this(false);
	}
	
	public Serializer(boolean pretty) {
		GsonBuilder builder = new GsonBuilder();
		if (pretty) builder.setPrettyPrinting();
		gson = builder.create();
	}
	
	public Collection<BuildEvent> tobuildEvents(String json) {
		if (BasicUtil.isEmpty(json)) return Collections.emptyList();
		return gson.fromJson(json, buildEventCollectionType);
	}

	public String fromBuildEvents(BuildEvent...buildEvents) {
		return fromBuildEvents(Arrays.asList(buildEvents));
	}
	
	public String fromBuildEvents(Collection<BuildEvent> buildEvents) {
		return gson.toJson(buildEvents, buildEventCollectionType);
	}

	public String fromBuilds(Builds builds) {
		return gson.toJson(builds);
	}
	
	public String fromBuild(Build build) {
		return gson.toJson(build);
	}
	
	public String fromJob(Job job) {
		return gson.toJson(job);
	}
	
}
