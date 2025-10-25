package ceri.common.color;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertRange;
import java.awt.Color;
import ceri.common.test.Assert;
import ceri.common.text.AnsiEscape;

public class ColorTestUtil {
	public static final int white = Coloring.white.argb;

	private ColorTestUtil() {}

	public static void assertArgbDiff(int argb, int expectedArgb, int diff) {
		for (var c : Component.ARGB)
			assertComponentDiff(c, argb, expectedArgb, diff);
	}

	public static void assertComponentDiff(Component component, int argb, int expected, int diff) {
		int value = component.get(argb);
		int expectedValue = component.get(expected);
		int expectedMin = Math.max(0, expectedValue - diff);
		int expectedMax = Math.min(Colors.MAX_VALUE, expectedValue + diff);
		assertRange(value, expectedMin, expectedMax,
			"Component %s is out of range: #%08x / #%08x \u00b1%d", component, argb, expected,
			diff);
	}

	public static void assertXargbDiff(long xargb, long expectedXargb, int diff) {
		for (var c : Component.XARGB)
			assertComponentDiff(c, xargb, expectedXargb, diff);
	}

	public static void assertComponentDiff(Component component, long xargb, long expected,
		int diff) {
		int value = component.get(xargb);
		int expectedValue = component.get(expected);
		int expectedMin = Math.max(0, expectedValue - diff);
		int expectedMax = Math.min(Colors.MAX_VALUE, expectedValue + diff);
		assertRange(value, expectedMin, expectedMax,
			"Component %s is out of range: #%016x / #%016x \u00b1%d", component, xargb, expected,
			diff);
	}

	public static void assertColor(Color color, Color c) {
		assertEquals(argb(color), argb(c));
	}

	public static void assertColor(Color color, int argb) {
		assertEquals(argb(color), argb);
	}

	public static void assertColorx(Colorx colorx, Colorx cx) {
		assertEquals(xargb(colorx), xargb(cx));
	}

	public static void assertColorx(Colorx colorx, long xargb) {
		assertEquals(xargb(colorx), xargb);
	}

	public static void assertRgb(Rgb color, double r, double g, double b) {
		assertRgb(color, 1.0, r, g, b);
	}

	public static void assertRgb(Rgb color, double a, double r, double g, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.r(), r, "r");
		Assert.approx(color.g(), g, "g");
		Assert.approx(color.b(), b, "b");
	}

	public static void assertHsb(Color color, double h, double s, double b) {
		assertHsb(color, 1.0, h, s, b);
	}

	public static void assertHsb(Color color, double a, double h, double s, double b) {
		assertHsb(Hsb.from(color), a, h, s, b);
	}

	public static void assertHsb(Hsb color, double h, double s, double b) {
		assertHsb(color, 1.0, h, s, b);
	}

	public static void assertHsb(Hsb color, double a, double h, double s, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.h(), h, "h");
		Assert.approx(color.s(), s, "s");
		Assert.approx(color.b(), b, "b");
	}

	public static void assertXyb(Xyb color, double x, double y, double b) {
		assertXyb(color, 1.0, x, y, b);
	}

	public static void assertXyb(Xyb color, double a, double x, double y, double b) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.x(), x, "x");
		Assert.approx(color.y(), y, "y");
		Assert.approx(color.b(), b, "b");
	}

	public static void assertXyz(Xyz color, double x, double y, double z) {
		assertXyz(color, 1.0, x, y, z);
	}

	public static void assertXyz(Xyz color, double a, double x, double y, double z) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.x(), x, "x");
		Assert.approx(color.y(), y, "y");
		Assert.approx(color.z(), z, "z");
	}

	public static void assertLuv(Luv color, double l, double u, double v) {
		assertLuv(color, 1.0, l, u, v);
	}

	public static void assertLuv(Luv color, double a, double l, double u, double v) {
		Assert.approx(color.a(), a, "a");
		Assert.approx(color.l(), l, "l");
		Assert.approx(color.u(), u, "u");
		Assert.approx(color.v(), v, "v");
	}

	public static String sgrs(int... argbs) {
		return sgrs("  ", white, argbs);
	}

	public static String sgrs(String space, int bg, int... argbs) {
		var b = new StringBuilder();
		for (int argb : argbs)
			b.append(sgr(argb, bg)).append(space);
		return b.append(AnsiEscape.Sgr.reset).toString();
	}

	public static String sgr(int argb) {
		return sgr(argb, white);
	}

	public static String sgr(int argb, int bg) {
		return AnsiEscape.csi.sgr().bgColor24(Colors.blendArgbs(argb, bg | Component.a.intMask))
			.toString();
	}

	public static String sgr(Color color) {
		return sgr(color, Color.white);
	}

	public static String sgr(Color color, Color bg) {
		return sgr(color.getRGB(), bg.getRGB());
	}

	private static Integer argb(Color color) {
		return color == null ? null : color.getRGB();
	}

	private static Long xargb(Colorx colorx) {
		return colorx == null ? null : colorx.xargb();
	}
}
