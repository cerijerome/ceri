package ceri.common.geo;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

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
		return HashCoder.hash(latitude, longitude);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LatLng)) return false;
		LatLng other = (LatLng) obj;
		return EqualsUtil.equals(latitude, other.latitude) &&
			EqualsUtil.equals(longitude, other.longitude);
	}

	@Override
	public String toString() {
		return "(" + latitude + "," + longitude + ")";
	}

}
