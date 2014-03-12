package ceri.ci.servlet;

import ceri.ci.build.Builds;

public class WebModel {
	public final WebParams params;
	public final Builds builds;

	public WebModel(WebParams params, Builds builds) {
		this.params = params;
		this.builds = builds;
	}

}
