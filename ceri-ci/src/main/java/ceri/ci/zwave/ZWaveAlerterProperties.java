package ceri.ci.zwave;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;

public class ZWaveAlerterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String HOST_KEY = "host";
	private static final String DEVICE_KEY = "device";

	public ZWaveAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public String host() {
		return value(HOST_KEY);
	}

	public Integer device(String name) {
		return intValue(DEVICE_KEY, name);
	}

	public Collection<String> names() {
		String prefix = key(DEVICE_KEY) + Key.SEPARATOR;
		int offset = prefix.length();
		Collection<String> names = new LinkedHashSet<>();
		for (String key : keys()) {
			if (!key.startsWith(prefix)) continue;
			String name = key.substring(offset);
			names.add(name);
		}
		return names;
	}

}
