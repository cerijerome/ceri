package ceri.ci.zwave;

import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;

/**
 * Properties to configure the zwave alerter.
 */
public class ZWaveAlerterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String HOST_KEY = "host";
	private static final String DEVICE_KEY = "device";
	private static final String ALERT_KEY = "alert";
	private static final String TIME_MS_KEY = "time.ms";

	public ZWaveAlerterProperties(BaseProperties properties, String group) {
		super(properties, group);
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

	public long alertTimeMs() {
		return longValue(0, ALERT_KEY, TIME_MS_KEY);
	}
	
	public Collection<Integer> alertDevices() {
		Collection<String> values = stringValues(ALERT_KEY, DEVICE_KEY);
		return integers(values);
	}
	
	private Collection<Integer> integers(Collection<String> values) {
		Collection<Integer> devices = new LinkedHashSet<>();
		for (String value : values)
			devices.add(Integer.valueOf(value.trim()));
		return devices;
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
