package ceri.home.device.dvd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.util.PrimitiveUtil;

public class DvdProperties extends BaseProperties {
	private static final String SPEED = "speed";

	private static enum ScanType {
		FAST,
		SLOW
	}

	private final Map<Dvd.Direction, List<Integer>> fastSpeeds;
	private final Map<Dvd.Direction, List<Integer>> slowSpeeds;

	public DvdProperties(Properties properties, String prefix) {
		super(properties, prefix);
		fastSpeeds = Collections.unmodifiableMap(getSpeedMap(ScanType.FAST));
		slowSpeeds = Collections.unmodifiableMap(getSpeedMap(ScanType.SLOW));
	}

	public Map<Dvd.Direction, List<Integer>> getFastSpeeds() {
		return fastSpeeds;
	}

	public Map<Dvd.Direction, List<Integer>> getSlowSpeeds() {
		return slowSpeeds;
	}

	private int speed(ScanType scanType, Dvd.Direction direction, int index) {
		String value =
			value(scanType.name().toLowerCase(), direction.name().toLowerCase(), String
				.valueOf(index), SPEED);
		return PrimitiveUtil.valueOf(value, 0);
	}

	private Map<Dvd.Direction, List<Integer>> getSpeedMap(ScanType scanType) {
		Map<Dvd.Direction, List<Integer>> map = new HashMap<>();
		for (Dvd.Direction direction : Dvd.Direction.values()) {
			List<Integer> speeds = Collections.unmodifiableList(getSpeeds(scanType, direction));
			map.put(direction, speeds);
		}
		return map;
	}

	private List<Integer> getSpeeds(ScanType scanType, Dvd.Direction direction) {
		List<Integer> speeds = new ArrayList<>();
		for (int i = 1;; i++) {
			int speed = speed(scanType, direction, i);
			if (speed == 0) break;
			speeds.add(speed);
		}
		return speeds;
	}

}
