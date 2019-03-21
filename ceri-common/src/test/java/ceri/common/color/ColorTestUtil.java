package ceri.common.color;

import static ceri.common.color.ColorUtil.CHANNEL_MAX;
import static ceri.common.data.ByteUtil.byteValueAt;
import static ceri.common.test.TestUtil.assertApprox;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.Color;

public class ColorTestUtil {

	private ColorTestUtil() {}

	public static void assertColor(Color color, Color c) {
		assertColor(color, c, CHANNEL_MAX);
	}

	public static void assertColor(Color color, Color c, int alpha) {
		assertThat(color, is(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha)));
	}

	public static void assertColor(Color color, int rgb) {
		assertColor(color, byteValueAt(rgb, 2), byteValueAt(rgb, 1), byteValueAt(rgb, 0));
	}

	public static void assertColor(Color color, int rgb, int a) {
		assertColor(color, byteValueAt(rgb, 2), byteValueAt(rgb, 1), byteValueAt(rgb, 0), a);
	}

	public static void assertColor(Color color, int r, int g, int b) {
		assertColor(color, r, g, b, CHANNEL_MAX);
	}

	public static void assertColor(Color color, int r, int g, int b, int a) {
		assertThat(color, is(new Color(r, g, b, a)));
	}

	public static void assertColorx(Colorx colorx, Colorx cx) {
		assertColorx(colorx, cx, CHANNEL_MAX);
	}

	public static void assertColorx(Colorx colorx, Colorx cx, int alpha) {
		assertColor(colorx.rgb, cx.rgb, alpha);
		assertXComponent(colorx, cx.x());
	}

	public static void assertColorx(Colorx colorx, int rgbx) {
		assertColorx(colorx, byteValueAt(rgbx, 3), byteValueAt(rgbx, 2), byteValueAt(rgbx, 1),
			byteValueAt(rgbx, 0));
	}

	public static void assertColorx(Colorx colorx, int rgbx, int a) {
		assertColorx(colorx, byteValueAt(rgbx, 3), byteValueAt(rgbx, 2), byteValueAt(rgbx, 1),
			byteValueAt(rgbx, 0), a);
	}

	public static void assertColorx(Colorx colorx, int r, int g, int b, int x) {
		assertColorx(colorx, r, g, b, x, CHANNEL_MAX);
	}

	public static void assertColorx(Colorx colorx, int r, int g, int b, int x, int a) {
		assertColor(colorx.rgb, r, g, b, a);
		assertXComponent(colorx, x);
	}

	private static void assertXComponent(Colorx colorx, int x) {
		assertThat("x-component", colorx.x(), is(x));
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

	public static void assertHsb(Color color, double h, double s, double b, double a) {
		assertHsb(HsbColor.from(color), h, s, b, a);
	}

	public static void assertHsb(HsbColor color, double h, double s, double b) {
		assertHsb(color, h, s, b, 1.0);
	}

	public static void assertHsb(HsbColor color, double h, double s, double b, double a) {
		assertApprox(color.h, h);
		assertApprox(color.s, s);
		assertApprox(color.b, b);
		assertApprox(color.a, a);
	}

	public static void assertXyb(XybColor color, double x, double y, double b) {
		assertXyb(color, x, y, b, 1.0);
	}

	public static void assertXyb(XybColor color, double h, double s, double b, double a) {
		assertApprox(color.x, h);
		assertApprox(color.y, s);
		assertApprox(color.b, b);
		assertApprox(color.a, a);
	}

	public static void assertXyz(XyzColor color, double x, double y, double z) {
		assertXyz(color, x, y, z, 1.0);
	}

	public static void assertXyz(XyzColor color, double x, double y, double z, double a) {
		assertApprox(color.x, x);
		assertApprox(color.y, y);
		assertApprox(color.z, z);
		assertApprox(color.a, a);
	}
}
