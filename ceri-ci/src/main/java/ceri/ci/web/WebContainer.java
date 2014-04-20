package ceri.ci.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;

/**
 * Creates web alerter.
 */
public class WebContainer {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "web";
	public final WebAlerter alerter;

	public WebContainer(BaseProperties properties) {
		WebAlerterProperties webProperties = new WebAlerterProperties(properties, GROUP);
		if (!webProperties.enabled()) {
			logger.info("Web alerter disabled");
			alerter = null;
		} else {
			logger.info("Creating web alerter");
			alerter = new WebAlerter();
		}
	}
	
}
