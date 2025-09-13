package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertHsb;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class HsbColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		HsbColor t = HsbColor.of(1.0, 0.5, 0.8, 0.6);
		HsbColor eq0 = HsbColor.of(1.0, 0.5, 0.8, 0.6);
		HsbColor eq1 = HsbColor.of(0.5, 0.8, 0.6);
		HsbColor ne0 = HsbColor.of(0.9, 0.5, 0.8, 0.6);
		HsbColor ne1 = HsbColor.of(1.0, 0.6, 0.8, 0.6);
		HsbColor ne2 = HsbColor.of(1.0, 0.5, 0.9, 0.6);
		HsbColor ne3 = HsbColor.of(1.0, 0.5, 0.8, 0.8);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromRgb() {
		assertHsb(HsbColor.fromRgb(0), 0, 0, 0);
		assertHsb(HsbColor.fromRgb(0x804020), 0.056, 0.750, 0.502);
	}

	@Test
	public void shouldCreateWithMaxSaturationAndBrightness() {
		assertHsb(HsbColor.max(0.5), 0.5, 1, 1);
	}

	@Test
	public void shouldProvideHsbValues() {
		assertApproxArray(HsbColor.from(0).hsbValues(), 0, 0, 0);
		assertApproxArray(HsbColor.from(0x804020).hsbValues(), 0.056, 0.750, 0.502);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(HsbColor.from(0x804020).argb(), 0xff804020);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(HsbColor.from(0x804020).color(), 0xff804020);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(HsbColor.from(0xff804020).rgb(), 1.0, 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldDetermineIfBlack() {
		assertEquals(HsbColor.of(0.0, 0.0, 0.0).isBlack(), true);
		assertEquals(HsbColor.of(0.5, 0.5, 0.0).isBlack(), true);
		assertEquals(HsbColor.of(0.5, 0.0, 0.5).isBlack(), false);
		assertEquals(HsbColor.of(1.0, 1.0, 1.0).isBlack(), false);
	}

	@Test
	public void shouldDetermineIfWhiteBlack() {
		assertEquals(HsbColor.of(0.0, 0.0, 0.0).isWhite(), false);
		assertEquals(HsbColor.of(0.5, 0.5, 1.0).isWhite(), false);
		assertEquals(HsbColor.of(0.5, 0.0, 0.5).isWhite(), false);
		assertEquals(HsbColor.of(0.5, 0.0, 1.0).isWhite(), true);
		assertEquals(HsbColor.of(0.0, 0.0, 1.0).isWhite(), true);
	}

	@Test
	public void shouldApplyAlpha() {
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).applyAlpha(), 0.8, 0.6, 0.4);
		assertHsb(HsbColor.of(0.5, 0.8, 0.6, 0.4).applyAlpha(), 0.8, 0.6, 0.2);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(HsbColor.from(0xff123456).hasAlpha(), false);
		assertEquals(HsbColor.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldShiftHue() {
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).shiftHue(0.0), 0.8, 0.6, 0.4);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).shiftHue(0.5), 0.3, 0.6, 0.4);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).shiftHue(1.0), 0.8, 0.6, 0.4);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).shiftHue(1.4), 0.2, 0.6, 0.4);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).shiftHue(-1.2), 0.6, 0.6, 0.4);
	}

	@Test
	public void shouldDim() {
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).dim(0.0), 0.8, 0.6, 0);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).dim(0.5), 0.8, 0.6, 0.2);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).dim(1.5), 0.8, 0.6, 0.6);
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).dim(1.0), 0.8, 0.6, 0.4);
		// not normalized or limited
		assertHsb(HsbColor.of(0.8, 0.6, 0.4).dim(5.0), 0.8, 0.6, 1.0);
	}

	@Test
	public void shouldNormalize() {
		assertHsb(HsbColor.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertHsb(HsbColor.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertHsb(HsbColor.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertHsb(HsbColor.of(0, 1.0, -0.5).normalize(), 1.0, 0, 1, 0.0);
		assertHsb(HsbColor.of(1.0, 2, -2, 1).normalize(), 1.0, 1.0, 0, 1.0);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		HsbColor.of(0.9, 0, 1, 0.5).verify();
		assertThrown(() -> HsbColor.of(1.1, 0, 1, 0.5).verify());
		assertThrown(() -> HsbColor.of(0.9, 0, 1, 1.5).verify());
		assertThrown(() -> HsbColor.of(0.9, -0.1, 1, 0.5).verify());
	}

}
