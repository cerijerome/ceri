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
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class RgbColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		RgbColor t = RgbColor.of(0.5, 0.1, 0.2);
		RgbColor eq0 = RgbColor.of(0.5, 0.1, 0.2);
		RgbColor eq1 = RgbColor.of(1.0, 0.5, 0.1, 0.2);
		RgbColor ne0 = RgbColor.of(0.9, 0.5, 0.1, 0.2);
		RgbColor ne1 = RgbColor.of(1.0, 0.4, 0.1, 0.2);
		RgbColor ne2 = RgbColor.of(1.0, 0.5, 0.2, 0.2);
		RgbColor ne3 = RgbColor.of(1.0, 0.5, 0.1, 0.1);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertRgb(RgbColor.from(color(0)), 0, 0, 0, 0);
		assertRgb(RgbColor.fromRgb(0x804020), 1.0, 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldProvideRgbValues() {
		assertApproxArray(RgbColor.from(0).rgbValues(), 0, 0, 0);
		assertApproxArray(RgbColor.from(0x804020).rgbValues(), 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(RgbColor.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(RgbColor.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToHsb() {
		assertHsb(RgbColor.from(0x12345678).hsb(), 0.071, 0.583, 0.567, 0.471);
	}

	@Test
	public void shouldConvertToXyz() {
		assertXyz(RgbColor.from(0x12345678).xyz(), 0.071, 0.081, 0.087, 0.190);
	}

	@Test
	public void shouldConvertToXyb() {
		assertXyb(RgbColor.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldApplyAlpha() {
		assertEquals(RgbColor.from(0xff654321).applyAlpha().argb(), 0xff654321);
		assertRgb(RgbColor.from(0x87654321).applyAlpha(), 1.0, 0.210, 0.139, 0.069);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(RgbColor.from(0xff123456).hasAlpha(), false);
		assertEquals(RgbColor.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldDim() {
		assertRgb(RgbColor.from(0x87654321).dim(0.0), 0.529, 0, 0, 0);
		assertRgb(RgbColor.from(0x87654321).dim(0.5), 0.529, 0.198, 0.131, 0.065);
		assertRgb(RgbColor.from(0x87654321).dim(1.5), 0.529, 0.594, 0.394, 0.194);
		assertEquals(RgbColor.from(0x87654321).dim(1.0).argb(), 0x87654321);
		// not normalized or limited
		assertRgb(RgbColor.from(0x87654321).dim(5.0), 0.529, 1.980, 1.314, 0.647);
	}

	@Test
	public void shouldNormalize() {
		assertRgb(RgbColor.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertRgb(RgbColor.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertRgb(RgbColor.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		assertRgb(RgbColor.of(1.2, 2, -2, 1).normalize(), 1.0, 1.0, 0, 0.75);
	}

	@Test
	public void shouldLimitValues() {
		assertRgb(RgbColor.of(0.9, 0, 1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertRgb(RgbColor.of(0.9, 0, 1, -0.1).limit(), 0.9, 0, 1, 0);
		assertRgb(RgbColor.of(0.9, 0, 1.1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertRgb(RgbColor.of(0.9, 2, 1, 0.5).limit(), 0.9, 1, 1, 0.5);
		assertRgb(RgbColor.of(1.2, 2, 1, 0.5).limit(), 1.0, 1, 1, 0.5);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		RgbColor.of(0.9, 0, 1, 0.5).verify();
		assertThrown(() -> RgbColor.of(1.1, 0, 1, 0.5).verify());
		assertThrown(() -> RgbColor.of(0.9, 0, 1, 1.5).verify());
		assertThrown(() -> RgbColor.of(0.9, -0.1, 1, 0.5).verify());
	}

}
