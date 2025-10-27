package ceri.common.util;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class LatLngBehavior {

	@Test
	public void testDistance() {
		Assert.isNull(LatLng.distance(null, LatLng.of(0, 0)));
		Assert.isNull(LatLng.distance(LatLng.of(0, 0), null));
		Assert.equal(LatLng.distance(LatLng.of(0, 0), LatLng.of(0, 0)), 0.0);
		Assert.equal(LatLng.distance(LatLng.of(-30, -40), LatLng.of(60, 80)), 150.0);
	}

	@Test
	public void testDistanceSquared() {
		Assert.isNull(LatLng.distanceSquared(null, LatLng.of(0, 0)));
		Assert.isNull(LatLng.distanceSquared(LatLng.of(0, 0), null));
		Assert.equal(LatLng.distanceSquared(LatLng.of(0, 0), LatLng.of(0, 0)), 0.0);
		Assert.equal(LatLng.distanceSquared(LatLng.of(-30, -40), LatLng.of(60, 80)), 22500.0);
	}

	@Test
	public void testParse() {
		Assert.isNull(LatLng.parse(null));
		Assert.isNull(LatLng.parse(""));
		Assert.isNull(LatLng.parse("10.0"));
		assertLatLng(LatLng.parse("9.999,-7.777"), 9.999, -7.777);
		assertLatLng(LatLng.parse("0.9  ,  0.7"), 0.9, 0.7);
		Assert.isNull(LatLng.parse(".9 , .7"));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		LatLng l = LatLng.of(77.123456, -23.987654);
		LatLng eq0 = LatLng.of(77.123456, -23.987654);
		LatLng ne0 = LatLng.of(77.12345, -23.987654);
		LatLng ne1 = LatLng.of(77.123456, -23.98765);
		TestUtil.exerciseEquals(l, eq0);
		Assert.notEqualAll(l, ne0, ne1);
	}

	private void assertLatLng(LatLng latLng, double lat, double lng) {
		Assert.equal(latLng, LatLng.of(lat, lng));
	}

}
