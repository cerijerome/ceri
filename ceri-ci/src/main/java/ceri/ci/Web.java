package ceri.ci;

import java.io.File;
import java.util.Properties;
import ceri.ci.web.WebAlerter;
import ceri.ci.web.WebAlerterProperties;
import ceri.common.io.IoUtil;

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
			File dir = IoUtil.getPackageDir(WebAlerter.class);
			alerter = new WebAlerter(dir);
		}
	}

}
