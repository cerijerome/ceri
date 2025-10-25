package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertLuv;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertApproxArray;
import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestUtil;

public class LuvBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		var t = Luv.of(1.0, 0.5, 0.4, 0.3);
		var eq0 = Luv.of(1.0, 0.5, 0.4, 0.3);
		var eq1 = Luv.of(0.5, 0.4, 0.3);
		var ne0 = Luv.of(0.9, 0.5, 0.4, 0.3);
		var ne1 = Luv.of(1.0, 0.4, 0.4, 0.3);
		var ne2 = Luv.of(1.0, 0.5, 0.5, 0.3);
		var ne3 = Luv.of(1.0, 0.5, 0.4, 0.4);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldProvideLuvValues() {
		assertApproxArray(Luv.of(0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
		assertApproxArray(Luv.of(0.6, 0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(Luv.of(0.5, 0.4, 0.2).hasAlpha(), false);
		assertEquals(Luv.of(0.5, 1, 0.5, 0).hasAlpha(), true);
	}

	@Test
	public void shouldProvideLightnessFromRef() {
		Assert.approx(Luv.Ref.CIE_D65.l(0), 0);
		Assert.approx(Luv.Ref.CIE_D65.l(0x123456), 0.21);
		Assert.approx(Luv.Ref.CIE_D65.l(0xffffff), 1.0);
		Assert.approx(Luv.Ref.CIE_D65.l(0.0), 0.0);
		Assert.approx(Luv.Ref.CIE_D65.l(0.5), 0.761);
		Assert.approx(Luv.Ref.CIE_D65.l(1.0), 1.0);
	}

	@Test
	public void shouldCreateFromColorWithRef() {
		assertLuv(Luv.Ref.CIE_D65.luv(color(0)), 0, 0, 0, 0);
		assertLuv(Luv.Ref.CIE_D65.luv(color(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromRgbWithRef() {
		assertLuv(Luv.Ref.CIE_D65.luv(Rgb.from(0)), 0, 0, 0, 0);
		assertLuv(Luv.Ref.CIE_D65.luv(Rgb.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXyzWithRef() {
		assertLuv(Luv.Ref.CIE_D65.luv(Xyz.from(0)), 0, 0, 0, 0);
		assertLuv(Luv.Ref.CIE_D65.luv(Xyz.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXybWithRef() {
		assertLuv(Luv.Ref.CIE_D65.luv(Xyb.from(0)), 0, 0, 0, 0);
		assertLuv(Luv.Ref.CIE_D65.luv(Xyb.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldConvertToArgbWithRef() {
		assertEquals(Luv.Ref.CIE_D65.argb(Luv.of(0, 0, 0)), 0xff000000);
		assertEquals(Luv.Ref.CIE_D65.argb(Luv.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToColorWithRef() {
		assertColor(Luv.Ref.CIE_D65.color(Luv.of(0, 0, 0)), 0xff000000);
		assertColor(Luv.Ref.CIE_D65.color(Luv.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToRgbWithRef() {
		assertRgb(Luv.Ref.CIE_D65.rgb(Luv.of(0, 0, 0)), 0, 0, 0);
		assertRgb(Luv.Ref.CIE_D65.rgb(Luv.of(0.21, -0.108, -0.277)), 0.07, 0.203, 0.337);
	}

	@Test
	public void shouldConvertToXyzWithRef() {
		assertXyz(Luv.Ref.CIE_D65.xyz(Luv.of(0, 0, 0)), 0, 0, 0);
		assertXyz(Luv.Ref.CIE_D65.xyz(Luv.of(0.21, -0.108, -0.277)), 0.032, 0.032, 0.093);
	}

	@Test
	public void shouldConvertToXybWithRef() {
		assertXyb(Luv.Ref.CIE_D65.xyb(Luv.of(0, 0, 0)), 0, 0, 0);
		assertXyb(Luv.Ref.CIE_D65.xyb(Luv.of(0.21, -0.108, -0.277)), 0.201, 0.207, 0.032);
	}
}
