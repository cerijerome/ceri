package ceri.ci.zwave;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;

public class ZWaveAlerterProperties extends BaseProperties {
	private static final String HOST_KEY = "host";
	private static final String DEVICE_KEY = "device";

	public ZWaveAlerterProperties(Properties properties) {
		this(properties, null);
	}

	public ZWaveAlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public String host() {
		return value(HOST_KEY);
	}

	public Integer device(String name) {
		String value = value(DEVICE_KEY, name);
		if (value == null) return null;
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Invalid device for " + key(DEVICE_KEY, name) + ": " +
				value);
		}
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
