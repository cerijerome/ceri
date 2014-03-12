package ceri.ci.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.Builds;
import ceri.common.log.LogUtil;

public class WebAlerter {
	private static final Logger logger = LogManager.getLogger();
	private volatile Builds builds;

	public WebAlerter() {
		clear();
	}

	public void update(Builds builds) {
		logger.debug("update: {}", LogUtil.compact(builds));
		this.builds = new Builds(builds);
	}

	public void clear() {
		logger.debug("clear");
		builds = new Builds();
	}

	public Builds builds() {
		return builds;
	}

}
