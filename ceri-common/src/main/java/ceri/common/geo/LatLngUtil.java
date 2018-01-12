package ceri.common.geo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatLngUtil {
	private static final Pattern PARSE_REGEX = Pattern.compile( //
		"\\(?\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*,\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*\\)?");

	private LatLngUtil() {}

	public static LatLng parse(String value) {
		if (value == null) return null;
		Matcher m = PARSE_REGEX.matcher(value);
		if (!m.find()) return null;
		return LatLng.of(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
	}

	public static Double distance(LatLng lhs, LatLng rhs) {
		if (lhs == null || rhs == null) return null;
		double latitude = lhs.latitude - rhs.latitude;
		double longitude = lhs.longitude - rhs.longitude;
		return Math.sqrt((latitude * latitude) + (longitude * longitude));
	}

}
