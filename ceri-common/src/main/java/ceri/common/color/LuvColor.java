package ceri.common.color;

import static ceri.common.color.ColorUtil.a;
import static ceri.common.color.ColorUtil.ratio;
import static ceri.common.color.ColorUtil.value;
import java.awt.Color;
import java.util.Objects;

/**
 * Represents a CIE LUV color with alpha. Approximate values a and L* 0-1, u* and v* -1 to +1.
 */
public class LuvColor {
	public static final double MAX_ALPHA = 1.0;
	public final double a;
	public final double l; // L*
	public final double u; // u*
	public final double v; // v*

	/**
	 * A reference used to convert CIE LUV values.
	 */
	public static class Ref {
		public static final Ref CIE_D65 = XyzColor.CIE_D65.luvRef();
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
		public LuvColor luv(int argb) {
			double[] luv = ColorSpaces.rgbToLuv(yn, un, vn, argb);
			return LuvColor.of(ratio(a(argb)), luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert sRGB color to CIE LUV, using this reference. Alpha is maintained.
		 */
		public LuvColor luv(Color color) {
			return luv(color.getRGB());
		}

		/**
		 * Convert sRGB to CIE LUV, using this reference. Alpha is maintained.
		 */
		public LuvColor luv(RgbColor rgb) {
			double[] luv = ColorSpaces.srgbToLuv(yn, un, vn, rgb.r, rgb.g, rgb.b);
			return LuvColor.of(rgb.a, luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE XYZ to CIE LUV, using this reference. Alpha is maintained.
		 */
		public LuvColor luv(XyzColor xyz) {
			double[] luv = ColorSpaces.xyzToLuv(yn, un, vn, xyz.x, xyz.y, xyz.z);
			return LuvColor.of(xyz.a, luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE xyY to CIE LUV, using this reference. Alpha is maintained.
		 */
		public LuvColor luv(XybColor xyb) {
			double[] luv = ColorSpaces.xybToLuv(yn, un, vn, xyb.x, xyb.y, xyb.b);
			return LuvColor.of(xyb.a, luv[0], luv[1], luv[2]);
		}

		/**
		 * Convert CIE LUV to sRGB int, using this reference. Alpha is maintained.
		 */
		public int argb(LuvColor luv) {
			return ColorUtil.alphaArgb(value(luv.a),
				ColorSpaces.luvToRgb(yn, un, vn, luv.l, luv.u, luv.v));
		}

		/**
		 * Convert CIE LUV to sRGB color, using this reference. Alpha is maintained.
		 */
		public Color color(LuvColor luv) {
			return ColorUtil.color(argb(luv));
		}

		/**
		 * Convert CIE LUV to sRGB, using this reference. Alpha is maintained.
		 */
		public RgbColor rgb(LuvColor luv) {
			double[] rgb = ColorSpaces.luvToSrgb(yn, un, vn, luv.l, luv.u, luv.v);
			return RgbColor.of(luv.a, rgb[0], rgb[1], rgb[2]);
		}

		/**
		 * Convert CIE LUV to CIE XYZ, using this reference. Alpha is maintained.
		 */
		public XyzColor xyz(LuvColor luv) {
			double[] xyz = ColorSpaces.luvToXyz(yn, un, vn, luv.l, luv.u, luv.v);
			return XyzColor.of(luv.a, xyz[0], xyz[1], xyz[2]);
		}

		/**
		 * Convert CIE LUV to CIE xyY, using this reference. Alpha is maintained.
		 */
		public XybColor xyb(LuvColor luv) {
			double[] xyb = ColorSpaces.luvToXyb(yn, un, vn, luv.l, luv.u, luv.v);
			return XybColor.of(luv.a, xyb[0], xyb[1], xyb[2]);
		}
	}

	/**
	 * Construct opaque instance from CIE L*u*v*.
	 */
	public static LuvColor of(double l, double u, double v) {
		return new LuvColor(MAX_ALPHA, l, u, v);
	}

	/**
	 * Construct from alpha + CIE L*u*v*.
	 */
	public static LuvColor of(double a, double l, double u, double v) {
		return new LuvColor(a, l, u, v);
	}

	private LuvColor(double a, double l, double u, double v) {
		this.a = a;
		this.l = l;
		this.u = u;
		this.v = v;
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
		return a < MAX_ALPHA;
	}

	@Override
	public int hashCode() {
		return Objects.hash(a, l, u, v);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LuvColor)) return false;
		LuvColor other = (LuvColor) obj;
		if (!Objects.equals(a, other.a)) return false;
		if (!Objects.equals(l, other.l)) return false;
		if (!Objects.equals(u, other.u)) return false;
		if (!Objects.equals(v, other.v)) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,l=%.5f,u=%.5f,v=%.5f)", a, l, u, v);
	}
}
