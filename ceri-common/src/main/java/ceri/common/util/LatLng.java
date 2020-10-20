package ceri.common.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatLng {
	private static final Pattern PARSE_REGEX = Pattern.compile( //
		"\\(?\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*,\\s*\\+?(\\-?\\d+\\.?\\d*)\\s*\\)?");
	public final double latitude;
	public final double longitude;

	public static LatLng parse(String value) {
		if (value == null) return null;
		Matcher m = PARSE_REGEX.matcher(value);
		if (!m.find()) return null;
		return LatLng.of(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
	}

	public static Double distance(LatLng lhs, LatLng rhs) {
		if (lhs == null || rhs == null) return null;
		return lhs.distanceTo(rhs);
	}

	public static Double distanceSquared(LatLng lhs, LatLng rhs) {
		if (lhs == null || rhs == null) return null;
		return lhs.distanceSquaredTo(rhs);
	}

	public static LatLng of(double lat, double lng) {
		return new LatLng(lat, lng);
	}

	public double distanceTo(LatLng latLng) {
		return Math.sqrt(distanceSquaredTo(latLng));
	}

	public double distanceSquaredTo(LatLng latLng) {
		Objects.requireNonNull(latLng);
		double latitude = this.latitude - latLng.latitude;
		double longitude = this.longitude - latLng.longitude;
		return (latitude * latitude) + (longitude * longitude);
	}

	private LatLng(double lat, double lng) {
		this.latitude = lat;
		this.longitude = lng;
	}

	@Override
	public int hashCode() {
		return Objects.hash(latitude, longitude);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LatLng)) return false;
		LatLng other = (LatLng) obj;
		if (!Objects.equals(latitude, other.latitude)) return false;
		if (!Objects.equals(longitude, other.longitude)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + latitude + "," + longitude + ")";
	}

}
