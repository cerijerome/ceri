package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;

public class ColorSpacesTest {

	@Test
	public void testScale() {
		Assert.approxArray(ColorSpaces.scale(0, 0, 0), 0, 0, 0);
		Assert.approxArray(ColorSpaces.scale(1.2, 0.6, 0.3), 1.0, 0.5, 0.25);
		Assert.approxArray(ColorSpaces.scale(1.2, 0.6, -0.8), 1.0, 0.7, 0.0);
	}

	@Test
	public void testLimit() {
		Assert.approxArray(ColorSpaces.limit(0, 0, 0), 0, 0, 0);
		Assert.approxArray(ColorSpaces.limit(1.2, 0.6, 0.3), 1.0, 0.6, 0.3);
		Assert.approxArray(ColorSpaces.limit(1.2, 0.6, -0.8), 1.0, 0.6, 0.0);
	}

	@Test
	public void testDToRgb() {
		Assert.equal(ColorSpaces.dToRgb(35), 0xfffff4a5);
		Assert.equal(ColorSpaces.dToRgb(50), 0xfffffcdd);
		Assert.equal(ColorSpaces.dToRgb(65), 0xffffffff);
		Assert.equal(ColorSpaces.dToRgb(93), 0xffedffff);
	}

	@Test
	public void testDToSrgb() {
		Assert.approxArray(ColorSpaces.dToSrgb(35), 1.210, 0.955, 0.648);
		Assert.approxArray(ColorSpaces.dToSrgb(50), 1.074, 0.989, 0.865);
		Assert.approxArray(ColorSpaces.dToSrgb(65), 1.000, 1.000, 0.999);
		Assert.approxArray(ColorSpaces.dToSrgb(93), 0.929, 1.005, 1.138);
	}

	@Test
	public void testDToXyz() {
		Assert.approxArray(ColorSpaces.dToXyz(35), 1.028, 1.0, 0.496);
		Assert.approxArray(ColorSpaces.dToXyz(50), 0.964, 1.0, 0.824);
		Assert.approxArray(ColorSpaces.dToXyz(65), 0.950, 1.0, 1.088);
		Assert.approxArray(ColorSpaces.dToXyz(93), 0.953, 1.0, 1.413);
	}

	@Test
	public void testCctToSrgb() {
		Assert.approxArray(ColorSpaces.cctToSrgb(1500), 1.660, 0.702, -0.784);
		Assert.approxArray(ColorSpaces.cctToSrgb(5000), 1.088, 0.983, 0.887);
		Assert.approxArray(ColorSpaces.cctToSrgb(6500), 1.019, 0.993, 1.015);
		Assert.approxArray(ColorSpaces.cctToSrgb(7500), 0.989, 0.996, 1.074);
	}

	@Test
	public void testCctToXyz() {
		Assert.approxArray(ColorSpaces.cctToXyz(1500), 1.473, 1.0, 0.058);
		Assert.approxArray(ColorSpaces.cctToXyz(5000), 0.981, 1.0, 0.863);
		Assert.approxArray(ColorSpaces.cctToXyz(6500), 0.969, 1.0, 1.121);
		Assert.approxArray(ColorSpaces.cctToXyz(7500), 0.968, 1.0, 1.255);
	}

	@Test
	public void testSrgbToHsb() {
		Assert.approxArray(ColorSpaces.srgbToHsb(0.01, 0, 0.01), 0.833, 1.0, 0.01);
	}

	@Test
	public void testHsbToSrgb() {
		Assert.approxArray(ColorSpaces.hsbToSrgb(0.5, 0.0, 0.3), 0.3, 0.3, 0.3);
	}
}
