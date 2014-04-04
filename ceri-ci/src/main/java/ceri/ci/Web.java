package ceri.ci;

import java.util.Properties;
import ceri.ci.web.WebAlerter;
import ceri.ci.web.WebAlerterProperties;

/**
 * Creates web alerter.
 */
public class Web {
	public final WebAlerter alerter;

	public Web(Properties properties) {
		WebAlerterProperties webProperties = new WebAlerterProperties(properties, "web");
		if (!webProperties.enabled()) {
			alerter = null;
		} else {
			alerter = new WebAlerter();
		}
	}

}
