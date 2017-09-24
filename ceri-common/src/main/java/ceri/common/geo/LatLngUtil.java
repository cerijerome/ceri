package ceri.common.geo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.math.MathUtil;

public class LatLngUtil {
	private static final Pattern PARSE_REGEX = Pattern.compile( //
		"\\(?\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*,\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*\\)?");

	private LatLngUtil() {}

	public static LatLng parse(String value) {
		if (value == null) return null;
		Matcher m = PARSE_REGEX.matcher(value);
		if (!m.find()) return null;
		return new LatLng(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
	}

	public static Double distance(LatLng lhs, LatLng rhs) {
		if (lhs == null || rhs == null) return null;
		double latitude = lhs.latitude - rhs.latitude;
		double longitude = lhs.longitude - rhs.longitude;
		return Math.sqrt((latitude * latitude) + (longitude * longitude));
	}

	public static LatLng circleShift(LatLng latLng, int sliceIndex, int slices, double radius) {
		double latitudeRatio = Math.cos(Math.toRadians(latLng.latitude));
		double radians = (Math.PI * 2 * sliceIndex) / slices;
		double latitude = latLng.latitude + (radius * Math.cos(radians) * latitudeRatio);
		double longitude = latLng.longitude + (radius * Math.sin(radians));
		return new LatLng(MathUtil.simpleRound(latitude, 10), MathUtil.simpleRound(longitude, 10));
	}

}
