package ceri.ci.web;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class WebAlerterProperties extends BaseProperties {
	private static final String _KEY = "";

	public WebAlerterProperties(Properties properties) {
		this(properties, null);
	}

	public WebAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public String value() {
		return stringValue(null, _KEY);
	}

}
