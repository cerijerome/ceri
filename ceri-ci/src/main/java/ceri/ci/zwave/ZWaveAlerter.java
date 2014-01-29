package ceri.ci.zwave;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import ceri.common.collection.ImmutableUtil;
import ceri.zwave.veralite.VeraLite;

public class ZWaveAlerter {
	private final Map<String, Integer> devices;
	private final Set<Integer> activeDevices = new HashSet<>();
	private final ZWaveController zwave;

	public static ZWaveAlerter create(Properties properties, String prefix) {
		ZWaveAlerterProperties zwProperties = new ZWaveAlerterProperties(properties, prefix);
		String host = zwProperties.host();
		VeraLite veraLite = new VeraLite(host);
		// TODO: check connectivity
		Builder builder = builder(new ZWaveController(veraLite));
		for (String name : zwProperties.names()) {
			Integer device = zwProperties.device(name);
			builder.device(name, device);
		}
		return builder.build();
	}

	public static class Builder {
		final Map<String, Integer> devices = new HashMap<>();
		final ZWaveController zwave;

		Builder(ZWaveController zwave) {
			if (zwave == null) throw new NullPointerException("zwave controller cannot be null");
			this.zwave = zwave;
		}

		public Builder device(String name, int device) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			if (device <= 0) throw new IllegalArgumentException("Not a valid device for " + name +
				": " + device);
			devices.put(name, device);
			return this;
		}

		public ZWaveAlerter build() {
			return new ZWaveAlerter(this);
		}
	}

	public static Builder builder(ZWaveController zwave) {
		return new Builder(zwave);
	}

	ZWaveAlerter(Builder builder) {
		zwave = builder.zwave;
		devices = ImmutableUtil.copyAsMap(builder.devices);
	}

	public void alert(Collection<String> keys) {
		Collection<Integer> devices = keysToDevices(keys);
		for (Integer device : new HashSet<>(activeDevices)) {
			if (!devices.contains(device)) deviceOff(device);
		}
		for (Integer device : devices) {
			if (!activeDevices.contains(device)) deviceOn(device);
		}
	}

	public void clear() {
		for (int device : devices.values())
			deviceOff(device);
	}

	private Collection<Integer> keysToDevices(Collection<String> keys) {
		Collection<Integer> devices = new HashSet<>();
		for (String key : keys) {
			Integer device = this.devices.get(key);
			if (device != null) devices.add(device);
		}
		return devices;
	}

	private void deviceOn(int device) {
		try {
			zwave.on(device);
			activeDevices.add(device);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deviceOff(int device) {
		try {
			zwave.off(device);
			activeDevices.remove(device);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
