package ceri.ci.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;

/**
 * Captures the build state for web page view.
 */
public class WebAlerter implements Alerter {
	private static final Logger logger = LogManager.getLogger();
	private volatile Builds builds;

	public WebAlerter() {
		clear();
	}

	@Override
	public void update(Builds builds) {
		logger.debug("Updating build state");
		this.builds = new Builds(builds);
	}

	@Override
	public void clear() {
		logger.debug("Clearing state");
		builds = new Builds();
	}

	@Override
	public void remind() {
		// Do nothing
	}
	
	public Builds builds() {
		return builds;
	}

}
