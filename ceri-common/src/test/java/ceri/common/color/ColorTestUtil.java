package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRange;
import java.awt.Color;
import ceri.common.text.AnsiEscape;

public class ColorTestUtil {
	public static final String sgrReset = AnsiEscape.csi.sgr().reset().toString();
	public static final int white = Colors.white.argb;

	private ColorTestUtil() {}

	public static void assertArgbDiff(int argb, int expectedArgb, int diff) {
		for (var c : Component.ARGB)
			assertComponentDiff(c, argb, expectedArgb, diff);
	}

	public static void assertComponentDiff(Component component, int argb, int expected, int diff) {
		int value = component.get(argb);
		int expectedValue = component.get(expected);
		int expectedMin = Math.max(0, expectedValue - diff);
		int expectedMax = Math.min(MAX_VALUE, expectedValue + diff);
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
		int expectedMax = Math.min(MAX_VALUE, expectedValue + diff);
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

	public static void assertRgb(RgbColor color, double r, double g, double b) {
		assertRgb(color, 1.0, r, g, b);
	}

	public static void assertRgb(RgbColor color, double a, double r, double g, double b) {
		assertApprox(color.a, a, "a");
		assertApprox(color.r, r, "r");
		assertApprox(color.g, g, "g");
		assertApprox(color.b, b, "b");
	}

	public static void assertHsb(Color color, double h, double s, double b) {
		assertHsb(color, 1.0, h, s, b);
	}

	public static void assertHsb(Color color, double a, double h, double s, double b) {
		assertHsb(HsbColor.from(color), a, h, s, b);
	}

	public static void assertHsb(HsbColor color, double h, double s, double b) {
		assertHsb(color, 1.0, h, s, b);
	}

	public static void assertHsb(HsbColor color, double a, double h, double s, double b) {
		assertApprox(color.a, a, "a");
		assertApprox(color.h, h, "h");
		assertApprox(color.s, s, "s");
		assertApprox(color.b, b, "b");
	}

	public static void assertXyb(XybColor color, double x, double y, double b) {
		assertXyb(color, 1.0, x, y, b);
	}

	public static void assertXyb(XybColor color, double a, double x, double y, double b) {
		assertApprox(color.a, a, "a");
		assertApprox(color.x, x, "x");
		assertApprox(color.y, y, "y");
		assertApprox(color.b, b, "b");
	}

	public static void assertXyz(XyzColor color, double x, double y, double z) {
		assertXyz(color, 1.0, x, y, z);
	}

	public static void assertXyz(XyzColor color, double a, double x, double y, double z) {
		assertApprox(color.a, a, "a");
		assertApprox(color.x, x, "x");
		assertApprox(color.y, y, "y");
		assertApprox(color.z, z, "z");
	}

	public static void assertLuv(LuvColor color, double l, double u, double v) {
		assertLuv(color, 1.0, l, u, v);
	}

	public static void assertLuv(LuvColor color, double a, double l, double u, double v) {
		assertApprox(color.a, a, "a");
		assertApprox(color.l, l, "l");
		assertApprox(color.u, u, "u");
		assertApprox(color.v, v, "v");
	}

	public static String sgrs(int... argbs) {
		return sgrs("  ", white, argbs);
	}

	public static String sgrs(String space, int bg, int... argbs) {
		var b = new StringBuilder();
		for (int argb : argbs)
			b.append(sgr(argb, bg)).append(space);
		return b.append(sgrReset).toString();
	}

	public static String sgr(int argb) {
		return sgr(argb, white);
	}

	public static String sgr(int argb, int bg) {
		return AnsiEscape.csi.sgr().bgColor24(ColorUtil.blendArgbs(argb, bg | Component.a.intMask))
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
		return colorx == null ? null : colorx.xargb;
	}
}
