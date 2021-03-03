package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.data.ByteUtil.ubyteAt;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.awt.Color;

public class ColorTestUtil {

	private ColorTestUtil() {}

	public static void assertColor(Color color, Color c, int alpha) {
		assertEquals(color, new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
	}

	public static void assertColor(Color color, int rgb) {
		assertColor(color, ubyteAt(rgb, 2), ubyteAt(rgb, 1), ubyteAt(rgb, 0));
	}

	public static void assertColor(Color color, int rgb, int a) {
		assertColor(color, ubyteAt(rgb, 2), ubyteAt(rgb, 1), ubyteAt(rgb, 0), a);
	}

	public static void assertColor(Color color, int r, int g, int b) {
		assertColor(color, r, g, b, MAX_VALUE);
	}

	public static void assertColor(Color color, int r, int g, int b, int a) {
		assertEquals(color, new Color(r, g, b, a));
	}

	public static void assertColorx(Colorx colorx, Colorx cx) {
		assertColorx(colorx, MAX_VALUE, cx);
	}

	public static void assertColorx(Colorx colorx, int alpha, Colorx cx) {
		assertColor(colorx.color(), cx.color(), alpha);
		assertXComponent(colorx, cx.xs());
	}

	public static void assertColorx(Colorx colorx, Color color, int... xs) {
		assertEquals(colorx.color(), color);
		assertXComponent(colorx, xs);
	}

	public static void assertColorx(Colorx colorx, int r, int g, int b, int... xs) {
		assertColorx(colorx, MAX_VALUE, r, g, b, xs);
	}

	public static void assertColorx(Colorx colorx, int a, int r, int g, int b, int... xs) {
		assertColor(colorx.color(), a, r, g, b);
		assertXComponent(colorx, xs);
	}

	private static void assertXComponent(Colorx colorx, int... xs) {
		assertArray(colorx.xs(), xs);
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
}
