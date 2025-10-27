package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.color.Xyb.CENTER;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class XybBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Xyb.of(1.0, 0.7, 0.5, 0.8);
		var eq0 = Xyb.of(1.0, 0.7, 0.5, 0.8);
		var eq1 = Xyb.of(0.7, 0.5, 0.8);
		var ne0 = Xyb.of(0.9, 0.7, 0.5, 0.8);
		var ne1 = Xyb.of(1.0, 0.6, 0.5, 0.8);
		var ne2 = Xyb.of(1.0, 0.7, 0.6, 0.8);
		var ne3 = Xyb.of(1.0, 0.7, 0.5, 0.7);
		TestUtil.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		assertXyb(Xyb.from(color(0)), 0, 0, 0, 0);
		assertXyb(Xyb.fromRgb(0x804020), 1.0, 0.505, 0.384, 0.084);
	}

	@Test
	public void shouldCreateWithFullBrightness() {
		assertXyb(Xyb.full(0.5, 0.4), 1.0, 0.5, 0.4, 1.0);
	}

	@Test
	public void shouldProvideXybValues() {
		Assert.approxArray(Xyb.from(0).xybValues(), 0, 0, 0);
		Assert.approxArray(Xyb.from(0x804020).xybValues(), 0.505, 0.384, 0.084);
	}

	@Test
	public void shouldConvertToArgb() {
		Assert.equal(Xyb.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		assertColor(Xyb.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToRgb() {
		assertRgb(Xyb.from(0x12345678).rgb(), 0.071, 0.204, 0.337, 0.471);
	}

	@Test
	public void shouldConvertToXyz() {
		assertXyz(Xyb.from(0x12345678).xyz(), 0.071, 0.081, 0.087, 0.190);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		Assert.equal(Xyb.from(0xff123456).hasAlpha(), false);
		Assert.equal(Xyb.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldDim() {
		assertXyb(Xyb.from(0x87654321).dim(0.0), 0.529, 0.452, 0.408, 0);
		assertXyb(Xyb.from(0x87654321).dim(0.5), 0.529, 0.452, 0.408, 0.034);
		assertXyb(Xyb.from(0x87654321).dim(1.5), 0.529, 0.452, 0.408, 0.103);
		Assert.equal(Xyb.from(0x87654321).dim(1.0).argb(), 0x87654321);
		assertXyb(Xyb.from(0x87654321).dim(20.0), 0.529, 0.452, 0.408, 1.378);
	}

	@Test
	public void shouldNormalize() {
		assertXyb(Xyb.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		assertXyb(Xyb.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		assertXyb(Xyb.of(0.5, 0.5, 1.5).normalize(), 0.5, 0.5, 1.0);
		assertXyb(Xyb.of(0, 2.0, 0.5).normalize(), 1.0, 0.2, 1, 0.5);
		assertXyb(Xyb.of(1.2, 2, -2, 1).normalize(), 1.0, 0.571, 0, 1);
		assertXyb(Xyb.of(CENTER.x(), CENTER.y() + 1, 1).normalize(), 0.333, 1.0, 1);
		assertXyb(Xyb.of(CENTER.x() + 1, CENTER.y(), 1).normalize(), 1.0, 0.333, 1);
	}

	@Test
	public void shouldLimitValues() {
		assertXyb(Xyb.of(0.9, 0, 1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertXyb(Xyb.of(0.9, 0, 1, -0.1).limit(), 0.9, 0, 1, 0);
		assertXyb(Xyb.of(0.9, 0, 1.1, 0.5).limit(), 0.9, 0, 1, 0.5);
		assertXyb(Xyb.of(0.9, 2, 1, 0.5).limit(), 0.9, 1, 1, 0.5);
		assertXyb(Xyb.of(1.2, 2, 1, 0.5).limit(), 1.0, 1, 1, 0.5);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		Xyb.of(0.9, 0, 1, 0.5).verify();
		Assert.thrown(() -> Xyb.of(1.1, 0, 1, 0.5).verify());
		Assert.thrown(() -> Xyb.of(0.9, 0, 1, 1.5).verify());
		Assert.thrown(() -> Xyb.of(0.9, -0.1, 1, 0.5).verify());
	}
}
