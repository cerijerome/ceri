package ceri.common.color;

import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import java.awt.Color;

public class ColorTestUtil {

	private ColorTestUtil() {}

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
		assertRgb(color, r, g, b, 1.0);
	}

	public static void assertRgb(RgbColor color, double r, double g, double b, double a) {
		assertApprox(color.r, r);
		assertApprox(color.g, g);
		assertApprox(color.b, b);
		assertApprox(color.a, a);
	}

	public static void assertHsb(Color color, double h, double s, double b) {
		assertHsb(color, h, s, b, 1.0);
	}

	public static void assertHsb(Color color, double a, double h, double s, double b) {
		assertHsb(HsbColor.from(color), a, h, s, b);
	}

	public static void assertHsb(HsbColor color, double h, double s, double b) {
		assertHsb(color, 1.0, h, s, b);
	}

	public static void assertHsb(HsbColor color, double a, double h, double s, double b) {
		assertApprox(color.a, a);
		assertApprox(color.h, h);
		assertApprox(color.s, s);
		assertApprox(color.b, b);
	}

	public static void assertXyb(XybColor color, double x, double y, double b) {
		assertXyb(color, 1.0, x, y, b);
	}

	public static void assertXyb(XybColor color, double a, double h, double s, double b) {
		assertApprox(color.a, a);
		assertApprox(color.x, h);
		assertApprox(color.y, s);
		assertApprox(color.b, b);
	}

	public static void assertXyz(XyzColor color, double x, double y, double z) {
		assertXyz(color, 1.0, x, y, z);
	}

	public static void assertXyz(XyzColor color, double a, double x, double y, double z) {
		assertApprox(color.a, a);
		assertApprox(color.x, x);
		assertApprox(color.y, y);
		assertApprox(color.z, z);
	}

	private static Integer argb(Color color) {
		return color == null ? null : color.getRGB();
	}

	private static Long xargb(Colorx colorx) {
		return colorx == null ? null : colorx.xargb;
	}
}
