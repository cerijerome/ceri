package ceri.common.color;

import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

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
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void shouldCreateFromColor() {
		ColorAssert.rgb(Rgb.from(Colors.color(0)), 0, 0, 0, 0);
		ColorAssert.rgb(Rgb.fromRgb(0x804020), 1.0, 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldProvideRgbValues() {
		Assert.approxArray(Rgb.from(0).rgbValues(), 0, 0, 0);
		Assert.approxArray(Rgb.from(0x804020).rgbValues(), 0.502, 0.251, 0.125);
	}

	@Test
	public void shouldConvertToArgb() {
		Assert.equal(Rgb.from(0x12345678).argb(), 0x12345678);
	}

	@Test
	public void shouldConvertToColor() {
		ColorAssert.color(Rgb.from(0x12345678).color(), 0x12345678);
	}

	@Test
	public void shouldConvertToHsb() {
		ColorAssert.hsb(Rgb.from(0x12345678).hsb(), 0.071, 0.583, 0.567, 0.471);
	}

	@Test
	public void shouldConvertToXyz() {
		ColorAssert.xyz(Rgb.from(0x12345678).xyz(), 0.071, 0.081, 0.087, 0.190);
	}

	@Test
	public void shouldConvertToXyb() {
		ColorAssert.xyb(Rgb.from(0x12345678).xyb(), 0.071, 0.227, 0.243, 0.087);
	}

	@Test
	public void shouldApplyAlpha() {
		Assert.equal(Rgb.from(0xff654321).applyAlpha().argb(), 0xff654321);
		ColorAssert.rgb(Rgb.from(0x87654321).applyAlpha(), 1.0, 0.210, 0.139, 0.069);
	}

	@Test
	public void shouldDeterminePresenceOfAlpha() {
		Assert.equal(Rgb.from(0xff123456).hasAlpha(), false);
		Assert.equal(Rgb.from(0xef123456).hasAlpha(), true);
	}

	@Test
	public void shouldDim() {
		ColorAssert.rgb(Rgb.from(0x87654321).dim(0.0), 0.529, 0, 0, 0);
		ColorAssert.rgb(Rgb.from(0x87654321).dim(0.5), 0.529, 0.198, 0.131, 0.065);
		ColorAssert.rgb(Rgb.from(0x87654321).dim(1.5), 0.529, 0.594, 0.394, 0.194);
		Assert.equal(Rgb.from(0x87654321).dim(1.0).argb(), 0x87654321);
		// not normalized or limited
		ColorAssert.rgb(Rgb.from(0x87654321).dim(5.0), 0.529, 1.980, 1.314, 0.647);
	}

	@Test
	public void shouldNormalize() {
		ColorAssert.rgb(Rgb.of(0, 1, 0.5).normalize(), 0, 1, 0.5);
		ColorAssert.rgb(Rgb.of(1.1, 0, 1, 0.5).normalize(), 1.0, 0, 1, 0.5);
		ColorAssert.rgb(Rgb.of(0, 2.0, 0.5).normalize(), 1.0, 0, 1, 0.25);
		ColorAssert.rgb(Rgb.of(1.2, 2, -2, 1).normalize(), 1.0, 1.0, 0, 0.75);
	}

	@Test
	public void shouldLimitValues() {
		ColorAssert.rgb(Rgb.of(0.9, 0, 1, 0.5).limit(), 0.9, 0, 1, 0.5);
		ColorAssert.rgb(Rgb.of(0.9, 0, 1, -0.1).limit(), 0.9, 0, 1, 0);
		ColorAssert.rgb(Rgb.of(0.9, 0, 1.1, 0.5).limit(), 0.9, 0, 1, 0.5);
		ColorAssert.rgb(Rgb.of(0.9, 2, 1, 0.5).limit(), 0.9, 1, 1, 0.5);
		ColorAssert.rgb(Rgb.of(1.2, 2, 1, 0.5).limit(), 1.0, 1, 1, 0.5);
	}

	@Test
	public void shouldVerifyValuesAreInRange() {
		Rgb.of(0.9, 0, 1, 0.5).verify();
		Assert.thrown(() -> Rgb.of(1.1, 0, 1, 0.5).verify());
		Assert.thrown(() -> Rgb.of(0.9, 0, 1, 1.5).verify());
		Assert.thrown(() -> Rgb.of(0.9, -0.1, 1, 0.5).verify());
	}
}
