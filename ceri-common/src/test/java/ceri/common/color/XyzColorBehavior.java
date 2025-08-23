package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class XyzColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XyzColor t = XyzColor.of(1.0, 0.5, 0.8, 0.6);
		XyzColor eq0 = XyzColor.of(1.0, 0.5, 0.8, 0.6);
		XyzColor eq1 = XyzColor.of(0.5, 0.8, 0.6);
		XyzColor ne0 = XyzColor.of(0.9, 0.5, 0.8, 0.6);
		XyzColor ne1 = XyzColor.of(1.0, 0.6, 0.8, 0.6);
		XyzColor ne2 = XyzColor.of(1.0, 0.5, 0.9, 0.6);
		XyzColor ne3 = XyzColor.of(1.0, 0.5, 0.8, 0.7);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertXyz(XyzColor.from(color(0)), 0, 0, 0, 0);
		assertXyz(XyzColor.fromRgb(0x804020), 1.0, 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldProvideXyzValues() {
		assertApproxArray(XyzColor.from(0).xyzValues(), 0, 0, 0);
		assertApproxArray(XyzColor.from(0x804020).xyzValues(), 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(XyzColor.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(XyzColor.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(XyzColor.from(0x12345678).rgb(), 0.071, 0.204, 0.337, 0.471);
	}

	@Test
	public void shouldConvertToXyb() {
		assertXyb(XyzColor.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(XyzColor.from(0xff123456).hasAlpha(), false);
		assertEquals(XyzColor.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldNormalize() {
		assertXyz(XyzColor.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertXyz(XyzColor.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertXyz(XyzColor.of(0.5, 0.5, 1.5).normalize(), 0.5, 0.5, 1.5);
		assertXyz(XyzColor.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		assertXyz(XyzColor.of(1.2, 2, -2, 1).normalize(), 1.0, 0, 0, 0);
	}
}
