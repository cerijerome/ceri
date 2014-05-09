package ceri.ci.zwave;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.common.Alerter;
import ceri.common.collection.ImmutableUtil;

/**
 * Alerter that turns on zwave devices for build failure events.
 */
public class ZWaveAlerter implements Alerter {
	private static final Logger logger = LogManager.getLogger();
	private final Map<String, Integer> devices;
	private final Set<Integer> activeDevices = new HashSet<>();
	private final ZWaveController zwave;

	public static class Builder {
		final Map<String, Integer> devices = new HashMap<>();
		final ZWaveController controller;
		long alertTimeMs = TimeUnit.SECONDS.toMillis(20);

		Builder(ZWaveController controller) {
			if (controller == null) throw new NullPointerException("Controller cannot be null");
			this.controller = controller;
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

	public static Builder builder(ZWaveController controller) {
		return new Builder(controller);
	}

	ZWaveAlerter(Builder builder) {
		zwave = builder.controller;
		devices = ImmutableUtil.copyAsMap(builder.devices);
	}

	@Override
	public void update(Builds builds) {
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(builds);
		alert(breakNames);
	}

	public void alert(String... keys) {
		alert(Arrays.asList(keys));
	}

	public void alert(Collection<String> keys) {
		logger.info("Alerting for {}", keys);
		Collection<Integer> devices = keysToDevices(keys);
		for (Integer device : new HashSet<>(activeDevices))
			if (!devices.contains(device)) deviceOff(device);
		for (Integer device : devices)
			if (!activeDevices.contains(device)) deviceOn(device);
	}

	@Override
	public void clear() {
		logger.info("Clearing state");
		for (int device : devices.values())
			deviceOff(device);
	}

	@Override
	public void remind() {
		// Do nothing
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
			logger.catching(e);
		}
	}

	private void deviceOff(int device) {
		try {
			zwave.off(device);
			activeDevices.remove(device);
		} catch (IOException e) {
			logger.catching(e);
		}
	}

}
