package ceri.ci.admin;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import ceri.ci.build.BuildEvent;

public class Params {
	public static final String ACTION = "action";
	public static final String EVENTS = "events";
	private static final Pattern PATH_SPLIT = Pattern.compile("/+([^/\\?]+)");
	private final HttpServletRequest request;
	private final Serializer serializer;

	public static class BuildJob {
		public final String build;
		public final String job;

		BuildJob(String build, String job) {
			this.build = build;
			this.job = job;
		}
	}

	public Params(HttpServletRequest request, Serializer serializer) {
		this.request = request;
		this.serializer = serializer;
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
		return serializer.toBuildEvents(json);
	}

}
