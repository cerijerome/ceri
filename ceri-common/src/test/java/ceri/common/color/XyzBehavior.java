package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		ColorAssert.xyz(Xyz.from(Colors.color(0)), 0, 0, 0, 0);
		ColorAssert.xyz(Xyz.fromRgb(0x804020), 1.0, 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldProvideXyzValues() {
		Assert.approxArray(Xyz.from(0).xyzValues(), 0, 0, 0);
		Assert.approxArray(Xyz.from(0x804020).xyzValues(), 0.110, 0.084, 0.024);
	}

	@Test
	public void shouldConvertToArgb() {
		Assert.equal(Xyz.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		ColorAssert.color(Xyz.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToRgb() {
		ColorAssert.rgb(Xyz.from(0x12345678).rgb(), 0.071, 0.204, 0.337, 0.471);
	}

	@Test
	public void shouldConvertToXyb() {
		ColorAssert.xyb(Xyz.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		Assert.equal(Xyz.from(0xff123456).hasAlpha(), false);
		Assert.equal(Xyz.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldNormalize() {
		ColorAssert.xyz(Xyz.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		ColorAssert.xyz(Xyz.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		ColorAssert.xyz(Xyz.of(0.5, 0.5, 1.5).normalize(), 0.5, 0.5, 1.5);
		ColorAssert.xyz(Xyz.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		ColorAssert.xyz(Xyz.of(1.2, 2, -2, 1).normalize(), 1.0, 0, 0, 0);
	}
}
