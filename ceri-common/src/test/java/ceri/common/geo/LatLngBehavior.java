package ceri.common.geo;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class LatLngBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		LatLng l = LatLng.of(77.123456, -23.987654);
		LatLng eq0 = LatLng.of(77.123456, -23.987654);
		LatLng ne0 = LatLng.of(77.12345, -23.987654);
		LatLng ne1 = LatLng.of(77.123456, -23.98765);
		exerciseEquals(l, eq0);
		assertAllNotEqual(l, ne0, ne1);
	}

}
