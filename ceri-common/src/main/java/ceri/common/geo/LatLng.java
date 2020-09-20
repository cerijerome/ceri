package ceri.common.geo;

import java.util.Objects;

public class LatLng {
	public final double latitude;
	public final double longitude;

	public static LatLng of(double lat, double lng) {
		return new LatLng(lat, lng);
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
