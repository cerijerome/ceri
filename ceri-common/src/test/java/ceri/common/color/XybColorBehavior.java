package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.color.XybColor.CENTER;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class XybColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		XybColor t = XybColor.of(1.0, 0.7, 0.5, 0.8);
		XybColor eq0 = XybColor.of(1.0, 0.7, 0.5, 0.8);
		XybColor eq1 = XybColor.of(0.7, 0.5, 0.8);
		XybColor ne0 = XybColor.of(0.9, 0.7, 0.5, 0.8);
		XybColor ne1 = XybColor.of(1.0, 0.6, 0.5, 0.8);
		XybColor ne2 = XybColor.of(1.0, 0.7, 0.6, 0.8);
		XybColor ne3 = XybColor.of(1.0, 0.7, 0.5, 0.7);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertXyb(XybColor.from(color(0)), 0, 0, 0, 0);
		assertXyb(XybColor.fromRgb(0x804020), 1.0, 0.505, 0.384, 0.084);
	}

	@Test
	public void shouldCreateWithFullBrightness() {
		assertXyb(XybColor.full(0.5, 0.4), 1.0, 0.5, 0.4, 1.0);
	}

	@Test
	public void shouldProvideXybValues() {
		assertApproxArray(XybColor.from(0).xybValues(), 0, 0, 0);
		assertApproxArray(XybColor.from(0x804020).xybValues(), 0.505, 0.384, 0.084);
	}

	@Test
	public void shouldConvertToArgb() {
		assertEquals(XybColor.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(XybColor.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(XybColor.from(0x12345678).rgb(), 0.071, 0.204, 0.337, 0.471);
	}

	@Test
	public void shouldConvertToXyz() {
		assertXyz(XybColor.from(0x12345678).xyz(), 0.071, 0.081, 0.087, 0.190);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(XybColor.from(0xff123456).hasAlpha(), false);
		assertEquals(XybColor.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldDim() {
		assertXyb(XybColor.from(0x87654321).dim(0.0), 0.529, 0.452, 0.408, 0);
		assertXyb(XybColor.from(0x87654321).dim(0.5), 0.529, 0.452, 0.408, 0.034);
		assertXyb(XybColor.from(0x87654321).dim(1.5), 0.529, 0.452, 0.408, 0.103);
		assertEquals(XybColor.from(0x87654321).dim(1.0).argb(), 0x87654321);
		assertXyb(XybColor.from(0x87654321).dim(20.0), 0.529, 0.452, 0.408, 1.378);
	}

	@Test
	public void shouldNormalize() {
		assertXyb(XybColor.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertXyb(XybColor.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertXyb(XybColor.of(0.5, 0.5, 1.5).normalize(), 0.5, 0.5, 1.0);
		assertXyb(XybColor.of(0, 2.0, 0.5).normalize(), 1.0, 0.2, 1, 0.5);
		assertXyb(XybColor.of(1.2, 2, -2, 1).normalize(), 1.0, 0.571, 0, 1);
		assertXyb(XybColor.of(CENTER.x, CENTER.y + 1, 1).normalize(), 0.333, 1.0, 1);
		assertXyb(XybColor.of(CENTER.x + 1, CENTER.y, 1).normalize(), 1.0, 0.333, 1);
	}

	@Test
	public void shouldLimitValues() {
		assertXyb(XybColor.of(0.9, 0, 1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertXyb(XybColor.of(0.9, 0, 1, -0.1).limit(), 0.9, 0, 1, 0);
		assertXyb(XybColor.of(0.9, 0, 1.1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertXyb(XybColor.of(0.9, 2, 1, 0.5).limit(), 0.9, 1, 1, 0.5);
		assertXyb(XybColor.of(1.2, 2, 1, 0.5).limit(), 1.0, 1, 1, 0.5);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		XybColor.of(0.9, 0, 1, 0.5).verify();
		assertThrown(() -> XybColor.of(1.1, 0, 1, 0.5).verify());
		assertThrown(() -> XybColor.of(0.9, 0, 1, 1.5).verify());
		assertThrown(() -> XybColor.of(0.9, -0.1, 1, 0.5).verify());
	}
}
