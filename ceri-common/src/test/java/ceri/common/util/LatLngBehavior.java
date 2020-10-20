package ceri.common.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class LatLngBehavior {

	@Test
	public void testDistance() {
		assertNull(LatLng.distance(null, LatLng.of(0, 0)));
		assertNull(LatLng.distance(LatLng.of(0, 0), null));
		assertEquals(LatLng.distance(LatLng.of(0, 0), LatLng.of(0, 0)), 0.0);
		assertEquals(LatLng.distance(LatLng.of(-30, -40), LatLng.of(60, 80)), 150.0);
	}

	@Test
	public void testDistanceSquared() {
		assertNull(LatLng.distanceSquared(null, LatLng.of(0, 0)));
		assertNull(LatLng.distanceSquared(LatLng.of(0, 0), null));
		assertEquals(LatLng.distanceSquared(LatLng.of(0, 0), LatLng.of(0, 0)), 0.0);
		assertEquals(LatLng.distanceSquared(LatLng.of(-30, -40), LatLng.of(60, 80)), 22500.0);
	}

	@Test
	public void testParse() {
		assertNull(LatLng.parse(null));
		assertNull(LatLng.parse(""));
		assertNull(LatLng.parse("10.0"));
		assertLatLng(LatLng.parse("9.999,-7.777"), 9.999, -7.777);
		assertLatLng(LatLng.parse("0.9  ,  0.7"), 0.9, 0.7);
		assertNull(LatLng.parse(".9 , .7"));
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		LatLng l = LatLng.of(77.123456, -23.987654);
		LatLng eq0 = LatLng.of(77.123456, -23.987654);
		LatLng ne0 = LatLng.of(77.12345, -23.987654);
		LatLng ne1 = LatLng.of(77.123456, -23.98765);
		exerciseEquals(l, eq0);
		assertAllNotEqual(l, ne0, ne1);
	}

	private void assertLatLng(LatLng latLng, double lat, double lng) {
		assertEquals(latLng, LatLng.of(lat, lng));
	}

}
