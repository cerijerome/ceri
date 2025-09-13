package ceri.common.color;

import static ceri.common.color.ColorTestUtil.assertColor;
import static ceri.common.color.ColorTestUtil.assertLuv;
import static ceri.common.color.ColorTestUtil.assertRgb;
import static ceri.common.color.ColorTestUtil.assertXyb;
import static ceri.common.color.ColorTestUtil.assertXyz;
import static ceri.common.color.Colors.color;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertApproxArray;
import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class LuvColorBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		LuvColor t = LuvColor.of(1.0, 0.5, 0.4, 0.3);
		LuvColor eq0 = LuvColor.of(1.0, 0.5, 0.4, 0.3);
		LuvColor eq1 = LuvColor.of(0.5, 0.4, 0.3);
		LuvColor ne0 = LuvColor.of(0.9, 0.5, 0.4, 0.3);
		LuvColor ne1 = LuvColor.of(1.0, 0.4, 0.4, 0.3);
		LuvColor ne2 = LuvColor.of(1.0, 0.5, 0.5, 0.3);
		LuvColor ne3 = LuvColor.of(1.0, 0.5, 0.4, 0.4);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldProvideLuvValues() {
		assertApproxArray(LuvColor.of(0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
		assertApproxArray(LuvColor.of(0.6, 0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		assertEquals(LuvColor.of(0.5, 0.4, 0.2).hasAlpha(), false);
		assertEquals(LuvColor.of(0.5, 1, 0.5, 0).hasAlpha(), true);
	}

	@Test
	public void shouldProvideLightnessFromRef() {
		assertApprox(LuvColor.Ref.CIE_D65.l(0), 0);
		assertApprox(LuvColor.Ref.CIE_D65.l(0x123456), 0.21);
		assertApprox(LuvColor.Ref.CIE_D65.l(0xffffff), 1.0);
		assertApprox(LuvColor.Ref.CIE_D65.l(0.0), 0.0);
		assertApprox(LuvColor.Ref.CIE_D65.l(0.5), 0.761);
		assertApprox(LuvColor.Ref.CIE_D65.l(1.0), 1.0);
	}

	@Test
	public void shouldCreateFromColorWithRef() {
		assertLuv(LuvColor.Ref.CIE_D65.luv(color(0)), 0, 0, 0, 0);
		assertLuv(LuvColor.Ref.CIE_D65.luv(color(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromRgbWithRef() {
		assertLuv(LuvColor.Ref.CIE_D65.luv(RgbColor.from(0)), 0, 0, 0, 0);
		assertLuv(LuvColor.Ref.CIE_D65.luv(RgbColor.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXyzWithRef() {
		assertLuv(LuvColor.Ref.CIE_D65.luv(XyzColor.from(0)), 0, 0, 0, 0);
		assertLuv(LuvColor.Ref.CIE_D65.luv(XyzColor.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXybWithRef() {
		assertLuv(LuvColor.Ref.CIE_D65.luv(XybColor.from(0)), 0, 0, 0, 0);
		assertLuv(LuvColor.Ref.CIE_D65.luv(XybColor.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldConvertToArgbWithRef() {
		assertEquals(LuvColor.Ref.CIE_D65.argb(LuvColor.of(0, 0, 0)), 0xff000000);
		assertEquals(LuvColor.Ref.CIE_D65.argb(LuvColor.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToColorWithRef() {
		assertColor(LuvColor.Ref.CIE_D65.color(LuvColor.of(0, 0, 0)), 0xff000000);
		assertColor(LuvColor.Ref.CIE_D65.color(LuvColor.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToRgbWithRef() {
		assertRgb(LuvColor.Ref.CIE_D65.rgb(LuvColor.of(0, 0, 0)), 0, 0, 0);
		assertRgb(LuvColor.Ref.CIE_D65.rgb(LuvColor.of(0.21, -0.108, -0.277)), 0.07, 0.203, 0.337);
	}

	@Test
	public void shouldConvertToXyzWithRef() {
		assertXyz(LuvColor.Ref.CIE_D65.xyz(LuvColor.of(0, 0, 0)), 0, 0, 0);
		assertXyz(LuvColor.Ref.CIE_D65.xyz(LuvColor.of(0.21, -0.108, -0.277)), 0.032, 0.032, 0.093);
	}

	@Test
	public void shouldConvertToXybWithRef() {
		assertXyb(LuvColor.Ref.CIE_D65.xyb(LuvColor.of(0, 0, 0)), 0, 0, 0);
		assertXyb(LuvColor.Ref.CIE_D65.xyb(LuvColor.of(0.21, -0.108, -0.277)), 0.201, 0.207, 0.032);
	}

}
