package ceri.ci.web;

import ceri.common.property.BaseProperties;

/**
 * Properties to configure the web view for alerts.
 */
public class WebAlerterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";

	public WebAlerterProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

}
