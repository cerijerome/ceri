package ceri.common.color;

import java.awt.Color;
import ceri.common.test.Assert;

public class ColorAssert {
	public static final int white = Coloring.white.argb;

	private ColorAssert() {}

	public static void argbDiff(int argb, int expectedArgb, int diff) {
		for (var c : Component.ARGB)
			componentDiff(c, argb, expectedArgb, diff);
	}

	public static void componentDiff(Component component, int argb, int expected, int diff) {
		int value = component.get(argb);
		int expectedValue = component.get(expected);
		int expectedMin = Math.max(0, expectedValue - diff);
		int expectedMax = Math.min(Colors.MAX_VALUE, expectedValue + diff);
		Assert.range(value, expectedMin, expectedMax,
			"Component %s is out of range: #%08x / #%08x \u00b1%d", component, argb, expected,
			diff);
	}

	public static void xargbDiff(long xargb, long expectedXargb, int diff) {
		for (var c : Component.XARGB)
			componentDiff(c, xargb, expectedXargb, diff);
	}

	public static void componentDiff(Component component, long xargb, long expected, int diff) {
		int value = component.get(xargb);
		int expectedValue = component.get(expected);
		int expectedMin = Math.max(0, expectedValue - diff);
		int expectedMax = Math.min(Colors.MAX_VALUE, expectedValue + diff);
		Assert.range(value, expectedMin, expectedMax,
			"Component %s is out of range: #%016x / #%016x \u00b1%d", component, xargb, expected,
			diff);
	}

	public static void color(Color color, Color c) {
		Assert.equal(argb(color), argb(c));
	}

	public static void color(Color color, int argb) {
		Assert.equal(argb(color), argb);
	}

	public static void colorx(Colorx colorx, Colorx cx) {
		Assert.equal(xargb(colorx), xargb(cx));
	}

	public static void colorx(Colorx colorx, long xargb) {
		Assert.equal(xargb(colorx), xargb);
	}

	public static void rgb(Rgb color, double r, double g, double b) {
		rgb(color, 1.0, r, g, b);
	}

	public static void rgb(Rgb color, double a, double r, double g, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.r(), r, "r");
		Assert.approx(color.g(), g, "g");
		Assert.approx(color.b(), b, "b");
	}

	public static void hsb(Color color, double h, double s, double b) {
		hsb(color, 1.0, h, s, b);
	}

	public static void hsb(Color color, double a, double h, double s, double b) {
		hsb(Hsb.from(color), a, h, s, b);
	}

	public static void hsb(Hsb color, double h, double s, double b) {
		hsb(color, 1.0, h, s, b);
	}

	public static void hsb(Hsb color, double a, double h, double s, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.h(), h, "h");
		Assert.approx(color.s(), s, "s");
		Assert.approx(color.b(), b, "b");
	}

	public static void xyb(Xyb color, double x, double y, double b) {
		xyb(color, 1.0, x, y, b);
	}

	public static void xyb(Xyb color, double a, double x, double y, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.x(), x, "x");
		Assert.approx(color.y(), y, "y");
		Assert.approx(color.b(), b, "b");
	}

	public static void xyz(Xyz color, double x, double y, double z) {
		xyz(color, 1.0, x, y, z);
	}

	public static void xyz(Xyz color, double a, double x, double y, double z) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.x(), x, "x");
		Assert.approx(color.y(), y, "y");
		Assert.approx(color.z(), z, "z");
	}

	public static void luv(Luv color, double l, double u, double v) {
		luv(color, 1.0, l, u, v);
	}

	public static void luv(Luv color, double a, double l, double u, double v) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.l(), l, "l");
		Assert.approx(color.u(), u, "u");
		Assert.approx(color.v(), v, "v");
	}

	private static Integer argb(Color color) {
		return color == null ? null : color.getRGB();
	}

	private static Long xargb(Colorx colorx) {
		return colorx == null ? null : colorx.xargb();
	}
}
