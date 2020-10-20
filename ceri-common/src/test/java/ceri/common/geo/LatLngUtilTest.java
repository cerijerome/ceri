package ceri.common.geo;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import org.junit.Test;

public class LatLngUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LatLngUtil.class);
	}

	@Test
	public void testDistance() {
		assertNull(LatLngUtil.distance(null, LatLng.of(0, 0)));
		assertNull(LatLngUtil.distance(LatLng.of(0, 0), null));
		assertEquals(LatLngUtil.distance(LatLng.of(0, 0), LatLng.of(0, 0)), 0.0);
		assertEquals(LatLngUtil.distance(LatLng.of(-30, -40), LatLng.of(60, 80)), 150.0);
	}

	@Test
	public void testParse() {
		assertNull(LatLngUtil.parse(null));
		assertNull(LatLngUtil.parse(""));
		assertNull(LatLngUtil.parse("10.0"));
		assertLatLng(LatLngUtil.parse("9.999,-7.777"), 9.999, -7.777);
		assertLatLng(LatLngUtil.parse("0.9  ,  0.7"), 0.9, 0.7);
		assertNull(LatLngUtil.parse(".9 , .7"));
	}

	private void assertLatLng(LatLng latLng, double lat, double lng) {
		assertEquals(latLng, LatLng.of(lat, lng));
	}

}
