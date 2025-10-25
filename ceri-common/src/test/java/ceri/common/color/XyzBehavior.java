package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertApproxArray;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class XyzBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Xyz.of(1.0, 0.5, 0.8, 0.6);
		var eq0 = Xyz.of(1.0, 0.5, 0.8, 0.6);
		var eq1 = Xyz.of(0.5, 0.8, 0.6);
		var ne0 = Xyz.of(0.9, 0.5, 0.8, 0.6);
		var ne1 = Xyz.of(1.0, 0.6, 0.8, 0.6);
		var ne2 = Xyz.of(1.0, 0.5, 0.9, 0.6);
		var ne3 = Xyz.of(1.0, 0.5, 0.8, 0.7);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertXyz(Xyz.from(color(0)), 0, 0, 0, 0);
		assertXyz(Xyz.fromRgb(0x804020), 1.0, 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldProvideXyzValues() {
		assertApproxArray(Xyz.from(0).xyzValues(), 0, 0, 0);
		assertApproxArray(Xyz.from(0x804020).xyzValues(), 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(Xyz.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(Xyz.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(Xyz.from(0x12345678).rgb(), 0.071, 0.204, 0.337, 0.471);
	}

	@Test
	public void shouldConvertToXyb() {
		assertXyb(Xyz.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(Xyz.from(0xff123456).hasAlpha(), false);
		assertEquals(Xyz.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldNormalize() {
		assertXyz(Xyz.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertXyz(Xyz.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertXyz(Xyz.of(0.5, 0.5, 1.5).normalize(), 0.5, 0.5, 1.5);
		assertXyz(Xyz.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		assertXyz(Xyz.of(1.2, 2, -2, 1).normalize(), 1.0, 0, 0, 0);
	}
}
