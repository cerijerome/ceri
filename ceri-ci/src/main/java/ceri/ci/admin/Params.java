package ceri.ci.admin;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Params {
	public static final String ACTION = "action";
	public static final String EVENTS = "events";
	private static final Pattern PATH_SPLIT = Pattern.compile("/+([^/\\?]+)");
	private static final Type buildEventCollectionType = new TypeToken<Collection<BuildEvent>>() {}
		.getType();
	private static final Gson gson = new GsonBuilder().create();
	private final HttpServletRequest request;

	public static void main(String[] args) {
		Event e0 = Event.failure("a", "b", "c");
		Event e1 = Event.success();
		Event e2 = Event.failure("a");
		BuildEvent b0 = new BuildEvent("b0", "j0", e0);
		BuildEvent b1 = new BuildEvent("b0", "j1", e1);
		BuildEvent b2 = new BuildEvent("b1", "j0", e2);
		Collection<BuildEvent> bes = Arrays.asList(b0, b1, b2);
		String json = gson.toJson(bes);
		System.out.println(json);
		Collection<BuildEvent> bes2 = gson.fromJson(json, buildEventCollectionType);
		for (BuildEvent be : bes2)
			System.out.println(be);
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(e0);
		builds.build("b0").job("j1").event(e1);
		builds.build("b1").job("j0").event(e2);
		json = gson.toJson(builds, Builds.class);
		System.out.println(json);
	}

	public static class BuildJob {
		public final String build;
		public final String job;

		BuildJob(String build, String job) {
			this.build = build;
			this.job = job;
		}
	}

	public Params(HttpServletRequest request) {
		this.request = request;
	}

	public Action action() {
		for (Action action : Action.values())
			if (request.getParameter(action.name()) != null) return action;
		return Action.view;
	}

	public BuildJob buildJob() {
		String build = null;
		String job = null;
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) pathInfo = "";
		Matcher m = PATH_SPLIT.matcher(pathInfo);
		if (m.find()) build = m.group(1);
		if (m.find()) job = m.group(1);
		return new BuildJob(build, job);
	}

	public Collection<BuildEvent> buildEvents() {
		String json = request.getParameter(EVENTS);
		if (json == null) return Collections.emptyList();
		return gson.fromJson(json, buildEventCollectionType);
	}

}
