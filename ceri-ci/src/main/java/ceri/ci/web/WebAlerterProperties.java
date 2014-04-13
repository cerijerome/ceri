package ceri.ci.web;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class WebAlerterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";

	public WebAlerterProperties(Properties properties, String...prefix) {
		super(properties, prefix);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

}
