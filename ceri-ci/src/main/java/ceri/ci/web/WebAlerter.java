package ceri.ci.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;

/**
 * Captures the heroes and villains of the builds. Can be used to display heroes and villains on a
 * web page.
 */
public class WebAlerter implements Alerter {
	private static final Logger logger = LogManager.getLogger();
	private volatile AnalyzedActors analyzedActors = AnalyzedActors.EMPTY;

	@Override
	public void update(Builds builds) {
		logger.debug("Updating actors");
		analyzedActors = new AnalyzedActors(builds);
	}

	@Override
	public void clear() {
		logger.debug("Clearing state");
		analyzedActors = AnalyzedActors.EMPTY;
	}

	@Override
	public void remind() {
		// Do nothing
	}

	public AnalyzedActors actors() {
		return analyzedActors;
	}

}
