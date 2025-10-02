package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class RgbBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Rgb.of(0.5, 0.1, 0.2);
		var eq0 = Rgb.of(0.5, 0.1, 0.2);
		var eq1 = Rgb.of(1.0, 0.5, 0.1, 0.2);
		var ne0 = Rgb.of(0.9, 0.5, 0.1, 0.2);
		var ne1 = Rgb.of(1.0, 0.4, 0.1, 0.2);
		var ne2 = Rgb.of(1.0, 0.5, 0.2, 0.2);
		var ne3 = Rgb.of(1.0, 0.5, 0.1, 0.1);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertRgb(Rgb.from(color(0)), 0, 0, 0, 0);
		assertRgb(Rgb.fromRgb(0x804020), 1.0, 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldProvideRgbValues() {
		assertApproxArray(Rgb.from(0).rgbValues(), 0, 0, 0);
		assertApproxArray(Rgb.from(0x804020).rgbValues(), 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(Rgb.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(Rgb.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToHsb() {
		assertHsb(Rgb.from(0x12345678).hsb(), 0.071, 0.583, 0.567, 0.471);
	}

	@Test
	public void shouldConvertToXyz() {
		assertXyz(Rgb.from(0x12345678).xyz(), 0.071, 0.081, 0.087, 0.190);
	}

	@Test
	public void shouldConvertToXyb() {
		assertXyb(Rgb.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldApplyAlpha() {
		assertEquals(Rgb.from(0xff654321).applyAlpha().argb(), 0xff654321);
		assertRgb(Rgb.from(0x87654321).applyAlpha(), 1.0, 0.210, 0.139, 0.069);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(Rgb.from(0xff123456).hasAlpha(), false);
		assertEquals(Rgb.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldDim() {
		assertRgb(Rgb.from(0x87654321).dim(0.0), 0.529, 0, 0, 0);
		assertRgb(Rgb.from(0x87654321).dim(0.5), 0.529, 0.198, 0.131, 0.065);
		assertRgb(Rgb.from(0x87654321).dim(1.5), 0.529, 0.594, 0.394, 0.194);
		assertEquals(Rgb.from(0x87654321).dim(1.0).argb(), 0x87654321);
		// not normalized or limited
		assertRgb(Rgb.from(0x87654321).dim(5.0), 0.529, 1.980, 1.314, 0.647);
	}

	@Test
	public void shouldNormalize() {
		assertRgb(Rgb.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertRgb(Rgb.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertRgb(Rgb.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		assertRgb(Rgb.of(1.2, 2, -2, 1).normalize(), 1.0, 1.0, 0, 0.75);
	}

	@Test
	public void shouldLimitValues() {
		assertRgb(Rgb.of(0.9, 0, 1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertRgb(Rgb.of(0.9, 0, 1, -0.1).limit(), 0.9, 0, 1, 0);
		assertRgb(Rgb.of(0.9, 0, 1.1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertRgb(Rgb.of(0.9, 2, 1, 0.5).limit(), 0.9, 1, 1, 0.5);
		assertRgb(Rgb.of(1.2, 2, 1, 0.5).limit(), 1.0, 1, 1, 0.5);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		Rgb.of(0.9, 0, 1, 0.5).verify();
		assertThrown(() -> Rgb.of(1.1, 0, 1, 0.5).verify());
		assertThrown(() -> Rgb.of(0.9, 0, 1, 1.5).verify());
		assertThrown(() -> Rgb.of(0.9, -0.1, 1, 0.5).verify());
	}
}
