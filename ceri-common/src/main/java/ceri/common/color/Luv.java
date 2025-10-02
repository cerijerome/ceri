package ceri.common.color;

import java.awt.Color;

/**
 * Represents a CIE LUV color with alpha. Approximate values a and L* 0-1, u* and v* -1 to +1.
 */
public record Luv(double a, double l, double u, double v) {

	/**
	 * A reference used to convert CIE LUV values.
	 */
	public static class Ref {
		public static final Ref CIE_D65 = Xyz.CIE_D65.luvRef();
		private final double yn; // Yn
		private final double un; // un'
		private final double vn; // vn'

		/**
		 * Construct from CIE XYZ 0-1 values.
		 */
		public static Ref from(double x, double y, double z) {
			double[] yuv = ColorSpaces.xyzToYuv(x, y, z);
			return new Ref(yuv[0], yuv[1], yuv[2]);
		}

		private Ref(double yn, double un, double vn) {
			this.yn = yn;
			this.un = un;
			this.vn = vn;
		}

		/**
		 * Convert CIE Y 0-1 value to CIE L* 0-1 value, using this reference.
		 */
		public double l(double y) {
			return ColorSpaces.yToL(yn, y); // L* from Y, Yn
		}

		/**
		 * Convert sRGB int value to CIE L* 0-1 value, using this reference. Alpha is ignored.
		 */
		public double l(int rgb) {
			return ColorSpaces.rgbToL(yn, rgb);
		}

		/**
		 * Convert sRGB int to CIE LUV, using this reference. Alpha is maintained.
		 */
		public Luv luv(int argb) {
			double[] luv = ColorSpaces.rgbToLuv(yn, un, vn, argb);
			return Luv.of(Colors.ratio(Colors.a(argb)), luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert sRGB color to CIE LUV, using this reference. Alpha is maintained.
		 */
		public Luv luv(Color color) {
			return luv(color.getRGB());
		}

		/**
		 * Convert sRGB to CIE LUV, using this reference. Alpha is maintained.
		 */
		public Luv luv(Rgb rgb) {
			double[] luv = ColorSpaces.srgbToLuv(yn, un, vn, rgb.r(), rgb.g(), rgb.b());
			return Luv.of(rgb.a(), luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE XYZ to CIE LUV, using this reference. Alpha is maintained.
		 */
		public Luv luv(Xyz xyz) {
			double[] luv = ColorSpaces.xyzToLuv(yn, un, vn, xyz.x(), xyz.y(), xyz.z());
			return Luv.of(xyz.a(), luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE xyY to CIE LUV, using this reference. Alpha is maintained.
		 */
		public Luv luv(Xyb xyb) {
			double[] luv = ColorSpaces.xybToLuv(yn, un, vn, xyb.x(), xyb.y(), xyb.b());
			return Luv.of(xyb.a(), luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE LUV to sRGB int, using this reference. Alpha is maintained.
		 */
		public int argb(Luv luv) {
			return Component.a.set(ColorSpaces.luvToRgb(yn, un, vn, luv.l, luv.u, luv.v),
				Colors.value(luv.a));
		}

		/**
		 * Convert CIE LUV to sRGB color, using this reference. Alpha is maintained.
		 */
		public Color color(Luv luv) {
			return Colors.color(argb(luv));
		}

		/**
		 * Convert CIE LUV to sRGB, using this reference. Alpha is maintained.
		 */
		public Rgb rgb(Luv luv) {
			double[] rgb = ColorSpaces.luvToSrgb(yn, un, vn, luv.l, luv.u, luv.v);
			return Rgb.of(luv.a, rgb[0], rgb[1], rgb[2]);
		}

		/**
		 * Convert CIE LUV to CIE XYZ, using this reference. Alpha is maintained.
		 */
		public Xyz xyz(Luv luv) {
			double[] xyz = ColorSpaces.luvToXyz(yn, un, vn, luv.l, luv.u, luv.v);
			return Xyz.of(luv.a, xyz[0], xyz[1], xyz[2]);
		}

		/**
		 * Convert CIE LUV to CIE xyY, using this reference. Alpha is maintained.
		 */
		public Xyb xyb(Luv luv) {
			double[] xyb = ColorSpaces.luvToXyb(yn, un, vn, luv.l, luv.u, luv.v);
			return Xyb.of(luv.a, xyb[0], xyb[1], xyb[2]);
		}
	}

	/**
	 * Construct opaque instance from CIE L*u*v*.
	 */
	public static Luv of(double l, double u, double v) {
		return new Luv(Colors.MAX_RATIO, l, u, v);
	}

	/**
	 * Construct from alpha + CIE L*u*v*.
	 */
	public static Luv of(double a, double l, double u, double v) {
		return new Luv(a, l, u, v);
	}

	/**
	 * Provide L*u*v* values. Alpha is dropped.
	 */
	public double[] luvValues() {
		return new double[] { l, u, v };
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < Colors.MAX_RATIO;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,l=%.5f,u=%.5f,v=%.5f)", a, l, u, v);
	}
}
