package ceri.ci.alert;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class AlertersProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String X10_KEY = "x10";
	private static final String ZWAVE_KEY = "zwave";
	private static final String WEB_KEY = "web";
	private static final String AUDIO_KEY = "audio";

	public AlertersProperties(Properties properties) {
		this(properties, null);
	}

	public AlertersProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public boolean x10Enabled() {
		return booleanValue(false, X10_KEY, ENABLED_KEY);
	}

	public boolean zwaveEnabled() {
		return booleanValue(false, ZWAVE_KEY, ENABLED_KEY);
	}

	public boolean webEnabled() {
		return booleanValue(false, WEB_KEY, ENABLED_KEY);
	}

	public boolean audioEnabled() {
		return booleanValue(false, AUDIO_KEY, ENABLED_KEY);
	}

}
