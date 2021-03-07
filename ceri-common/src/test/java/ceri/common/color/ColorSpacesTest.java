package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class ColorSpacesTest {

	@Test
	public void testDToRgb() {
		assertEquals(ColorSpaces.dToRgb(35), 0xfffff4a5);
		assertEquals(ColorSpaces.dToRgb(50), 0xfffffcdd);
		assertEquals(ColorSpaces.dToRgb(65), 0xffffffff);
		assertEquals(ColorSpaces.dToRgb(93), 0xffedffff);
	}

	@Test
	public void testDToSrgb() {
		assertApproxArray(ColorSpaces.dToSrgb(35), 1.210, 0.955, 0.648);
		assertApproxArray(ColorSpaces.dToSrgb(50), 1.074, 0.989, 0.865);
		assertApproxArray(ColorSpaces.dToSrgb(65), 1.000, 1.000, 0.999);
		assertApproxArray(ColorSpaces.dToSrgb(93), 0.929, 1.005, 1.138);
	}

	@Test
	public void testDToXyz() {
		assertApproxArray(ColorSpaces.dToXyz(35), 1.028, 1.0, 0.496);
		assertApproxArray(ColorSpaces.dToXyz(50), 0.964, 1.0, 0.824);
		assertApproxArray(ColorSpaces.dToXyz(65), 0.950, 1.0, 1.088);
		assertApproxArray(ColorSpaces.dToXyz(93), 0.953, 1.0, 1.413);
	}

	@Test
	public void testCctToSrgb() {
		assertApproxArray(ColorSpaces.cctToSrgb(1500), 1.660, 0.702, -0.784);
		assertApproxArray(ColorSpaces.cctToSrgb(5000), 1.088, 0.983, 0.887);
		assertApproxArray(ColorSpaces.cctToSrgb(6500), 1.019, 0.993, 1.015);
		assertApproxArray(ColorSpaces.cctToSrgb(7500), 0.989, 0.996, 1.074);
	}

	@Test
	public void testCctToXyz() {
		assertApproxArray(ColorSpaces.cctToXyz(1500), 1.473, 1.0, 0.058);
		assertApproxArray(ColorSpaces.cctToXyz(5000), 0.981, 1.0, 0.863);
		assertApproxArray(ColorSpaces.cctToXyz(6500), 0.969, 1.0, 1.121);
		assertApproxArray(ColorSpaces.cctToXyz(7500), 0.968, 1.0, 1.255);
	}

	@Test
	public void testSrgbToHsb() {
		assertApproxArray(ColorSpaces.srgbToHsb(0.01, 0, 0.01), 0.833, 1.0, 0.01);
	}

	@Test
	public void testHsbToSrgb() {
		assertApproxArray(ColorSpaces.hsbToSrgb(0.5, 0.0, 0.3), 0.3, 0.3, 0.3);
	}

	@Test
	public void testNormalizeSrgb() {
		assertApproxArray(ColorSpaces.normalizeSrgb(0, 0, 0), 0, 0, 0);
		assertApproxArray(ColorSpaces.normalizeSrgb(1.2, 0.6, 0.3), 1.0, 0.5, 0.25);
		assertApproxArray(ColorSpaces.normalizeSrgb(1.2, 0.6, -0.8), 1.0, 0.7, 0.0);
	}

}
