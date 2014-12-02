package ceri.ci.zwave;

import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;

/**
 * Properties to configure the zwave alerter.
 */
public class ZWaveProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String HOST_KEY = "host";
	private static final String DEVICE_KEY = "device";
	private static final String GROUP_KEY = "group";
	private static final String DEVICES_KEY = "devices";
	private static final String CALL_KEY = "call";
	private static final int CALL_DELAY_MS_DEF = 300;
	private static final String DELAY_MS_KEY = "delay.ms";
	private static final String RANDOMIZE_KEY = "randomize";
	private static final int RANDOMIZE_DELAY_MS_DEF = 100;
	private static final int RANDOMIZE_PERIOD_MS_DEF = 0;
	private static final String PERIOD_MS_KEY = "period.ms";
	private static final String TEST_MODE_KEY = "test.mode";

	public ZWaveProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public boolean testMode() {
		return booleanValue(false, TEST_MODE_KEY);
	}

	public String host() {
		return value(HOST_KEY);
	}

	public int callDelayMs() {
		return intValue(CALL_DELAY_MS_DEF, CALL_KEY, DELAY_MS_KEY);
	}
	
	public int randomizeDelayMs() {
		return intValue(RANDOMIZE_DELAY_MS_DEF, RANDOMIZE_KEY, DELAY_MS_KEY);
	}
	
	public long randomizePeriodMs() {
		return longValue(RANDOMIZE_PERIOD_MS_DEF, RANDOMIZE_KEY, PERIOD_MS_KEY);
	}
	
	public Integer device(String name) {
		return intValue(DEVICE_KEY, name);
	}

	public Collection<Integer> groupDevices() {
		Collection<String> values = stringValues(GROUP_KEY, DEVICES_KEY);
		if (values == null) return null;
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
