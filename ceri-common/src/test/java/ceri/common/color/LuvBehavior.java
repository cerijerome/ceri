package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldProvideLuvValues() {
		Assert.approxArray(Luv.of(0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
		Assert.approxArray(Luv.of(0.6, 0.8, 0.6, 0.4).luvValues(), 0.8, 0.6, 0.4);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		Assert.equal(Luv.of(0.5, 0.4, 0.2).hasAlpha(), false);
		Assert.equal(Luv.of(0.5, 1, 0.5, 0).hasAlpha(), true);
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
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Colors.color(0)), 0, 0, 0, 0);
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Colors.color(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromRgbWithRef() {
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Rgb.from(0)), 0, 0, 0, 0);
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Rgb.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXyzWithRef() {
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Xyz.from(0)), 0, 0, 0, 0);
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Xyz.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldCreateFromXybWithRef() {
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Xyb.from(0)), 0, 0, 0, 0);
		ColorAssert.luv(Luv.Ref.CIE_D65.luv(Xyb.from(0xff123456)), 0.21, -0.108, -0.277);
	}

	@Test
	public void shouldConvertToArgbWithRef() {
		Assert.equal(Luv.Ref.CIE_D65.argb(Luv.of(0, 0, 0)), 0xff000000);
		Assert.equal(Luv.Ref.CIE_D65.argb(Luv.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToColorWithRef() {
		ColorAssert.color(Luv.Ref.CIE_D65.color(Luv.of(0, 0, 0)), 0xff000000);
		ColorAssert.color(Luv.Ref.CIE_D65.color(Luv.of(0.21, -0.108, -0.277)), 0xff123456);
	}

	@Test
	public void shouldConvertToRgbWithRef() {
		ColorAssert.rgb(Luv.Ref.CIE_D65.rgb(Luv.of(0, 0, 0)), 0, 0, 0);
		ColorAssert.rgb(Luv.Ref.CIE_D65.rgb(Luv.of(0.21, -0.108, -0.277)), 0.07, 0.203, 0.337);
	}

	@Test
	public void shouldConvertToXyzWithRef() {
		ColorAssert.xyz(Luv.Ref.CIE_D65.xyz(Luv.of(0, 0, 0)), 0, 0, 0);
		ColorAssert.xyz(Luv.Ref.CIE_D65.xyz(Luv.of(0.21, -0.108, -0.277)), 0.032, 0.032, 0.093);
	}

	@Test
	public void shouldConvertToXybWithRef() {
		ColorAssert.xyb(Luv.Ref.CIE_D65.xyb(Luv.of(0, 0, 0)), 0, 0, 0);
		ColorAssert.xyb(Luv.Ref.CIE_D65.xyb(Luv.of(0.21, -0.108, -0.277)), 0.201, 0.207, 0.032);
	}
}
