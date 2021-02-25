package ceri.common.color;

import static java.lang.Math.pow;
import ceri.common.math.MathUtil;
import ceri.common.math.Matrix;

/**
 * Utility methods to convert between color spaces. https://en.wikipedia.org/wiki/Color_space
 */
public class ColorSpaces {
	// https://en.wikipedia.org/wiki/CIELUV#The_forward_transformation
	private static final Matrix XYZ_TO_DUV =
		Matrix.of(new double[][] { { 1, 15, 3 }, { 4, 0, 0 }, { 0, 9, 0 } });
	private static final Matrix YUV_TO_DXZ =
		Matrix.of(new double[][] { { 0, 0, 4 }, { 0, 9, 0 }, { 12, -3, -20 } });
	private static final Gamma L_GAMMA = new Gamma(2.16 / 24389, 243.89 / 27, 0.16, 3);
	private static final double UVLM = 13; // u*v* L* multiplier
	public static final Luv D65_LUV = XyzColor.CIE_D65.luv();
	// https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	// http://www.brucelindbloom.com/index.html?LContinuity.html (more accurate?)
	private static final Matrix LRGB_TO_XYZ = Matrix.of(new double[][] { //
		{ 0.4124564, 0.3575761, 0.1804375 }, //
		{ 0.2126729, 0.7151522, 0.0721750 }, //
		{ 0.0193339, 0.1191920, 0.9503041 } });
	private static final Matrix XYZ_TO_LRGB = LRGB_TO_XYZ.invert();
	private static final Gamma SRGB_GAMMA = new Gamma(0.0031308, 12.92, 0.055, 2.4);
	// https://en.wikipedia.org/wiki/Standard_illuminant#Illuminant_series_D
	private static final Matrix CCT_TO_XYB_D_X = Matrix.of(new double[][] { //
		{ 0.244063, 0.09911, 2.9678, -4.6070 }, // <= K_MIN_D_X
		{ 0.237040, 0.24748, 1.9018, -2.0064 } });
	private static final Matrix CCT_TO_XYB_D_Y = Matrix.of(new double[][] { //
		{ -0.275, 2.870, -3.000 } });
	// https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	private static final Matrix CCT_TO_XYB_X = Matrix.of(new double[][] { //
		{ 0.179910, 0.8776956, -0.2343589, -0.2661239 }, // <= K_MIN_X
		{ 0.240390, 0.2226347, 2.1070379, -3.0258469 } });
	private static final Matrix CCT_TO_XYB_Y = Matrix.of(new double[][] { //
		{ -0.20219683, 2.18555832, -1.34811020, -1.1063814 }, // <= K_MIN_Y0
		{ -0.16748867, 2.09137015, -1.37418593, -0.9549476 }, // <= K_MIN_Y1
		{ -0.37001483, 3.75112997, -5.87338670, 3.0817580 } });
	private static final int K_MIN = 1300;
	private static final int K_MAX = 25000;
	private static final int K_MIN_D_X = 7000;
	private static final int K_MIN_X = 4000;
	private static final int K_MIN_Y0 = 2222;
	private static final int K_MIN_Y1 = 4000;
	private static final double RKK = 1000.0; // to calculate reciprocal kilo-kelvins

	private ColorSpaces() {}

	/**
	 * Provides split compress/expand functions, used for sRGB gamma correction, and to calculate
	 * CIELUV L* from Y/Yn. The low function is a simple multiplication, and the high function is a
	 * reciprocal power with multiplier and offset.
	 */
	public static class Gamma {
		private final double low; // the low/high boundary for compress
		private final double lowExp; // the low/high boundary for expand
		private final double aLow; // the low multiplier
		private final double aHigh; // the high multiplier/offset
		private final double p; // the high reciprocal power

		private Gamma(double low, double aLow, double aHigh, double p) {
			this.low = low;
			this.aLow = aLow;
			this.aHigh = aHigh;
			this.p = p;
			lowExp = compress(low);
		}

		/**
		 * Execute the split gamma compress function on the value.
		 */
		public double compress(double u) {
			return u <= low ? aLow * u : (1 + aHigh) * pow(u, 1 / p) - aHigh;
		}

		/**
		 * Execute the split gamma expand function on the value.
		 */
		public double expand(double u) {
			return u <= lowExp ? u / aLow : pow((u + aHigh) / (1 + aHigh), p);
		}
	}

	/**
	 * Provides a mapping betwen CIE XYZ and CIE L*u*v*, using reference Yn, un', and vn'.
	 */
	public static class Luv {
		private final double yn; // Yn
		private final double un; // un'
		private final double vn; // vn'

		public static Luv fromXyz(double... xyz) {
			double[] yuv = xyzToYuv(xyz);
			return new Luv(xyz[1], yuv[1], yuv[2]);
		}

		private Luv(double yn, double un, double vn) {
			this.yn = yn;
			this.un = un;
			this.vn = vn;
		}

		/**
		 * Map CIE Y 0-1 double to CIE L* 0-1 double, using this reference.
		 */
		public double yToL(double y) {
			return ColorSpaces.yToL(yn, y); // L* from Y, Yn
		}

		/**
		 * Map sRGB int to CIE L* 0-1 double, using this reference.
		 */
		public double rgbToL(int rgb) {
			return yToL(ColorSpaces.rgbToY(rgb)); // L* from sRGB
		}

		/**
		 * Map sRGB int to CIE L*u*v* 0-1 doubles, using this reference.
		 */
		public double[] rgbToLuv(int rgb) {
			return xyzToLuv(ColorSpaces.rgbToXyz(rgb));
		}

		/**
		 * Map CIE L*u*v* 0-1 doubles to sRGB int using this reference.
		 */
		public int luvToRgb(double... luv) {
			return ColorSpaces.xyzToRgb(luvToXyz(luv));
		}

		/**
		 * Map CIE xyY 0-1 doubles to CIE L*u*v* 0-1 doubles using this reference.
		 */
		public double[] xybToLuv(double... xyb) {
			return xyzToLuv(ColorSpaces.xybToXyz(xyb));
		}

		/**
		 * Map CIE L*u*v* 0-1 doubles to CIE xyY 0-1 doubles using this reference.
		 */
		public double[] luvToXyb(double... luv) {
			return ColorSpaces.xyzToXyb(luvToXyz(luv));
		}

		/**
		 * Map CIE XYZ 0-1 doubles to CIE L*u*v* 0-1 doubles using this reference.
		 */
		public double[] xyzToLuv(double... xyz) {
			double[] yuv = xyzToYuv(xyz); // Y, u', v'
			// re-using double[] Yu'v' for L*u*v*
			yuv[0] = yToL(yuv[0]); // L* from Y, Yn
			yuv[1] = UVLM * yuv[0] * (yuv[1] - un); // u* from L*, u', un'
			yuv[2] = UVLM * yuv[0] * (yuv[2] - vn); // v* from L*, v', vn'
			return yuv; // actually L*u*v*
		}

		/**
		 * Map CIE L*u*v* 0-1 doubles to CIE XYZ 0-1 doubles using this reference.
		 */
		public double[] luvToXyz(double... luv) { // L*u*v*
			double y = lToY(yn, luv[0]); // Y from L*, Yn
			double u = luv[1] / (UVLM * luv[0]) + un; // u' from L*, u*, un'
			double v = luv[2] / (UVLM * luv[0]) + vn; // v' from L*, v*, vn'
			return yuvToXyz(y, u, v);
		}
	}

	/**
	 * Convert color temp K at max brightness to sRGB int. Valid for 4000K-25000K.
	 */
	public static int cctToRgb(int k) {
		return xybToRgb(cctToXyb(k));
	}

	/**
	 * Convert CIE daylight illuminant (Dxx) at max brightness to CIE xy.
	 */
	public static double[] dToXyb(int d) {
		int k = MathUtil.limit(d * 100, K_MIN, K_MAX);
		double x = polynomial(CCT_TO_XYB_D_X, k <= K_MIN_D_X ? 0 : 1, RKK / k);
		double y = polynomial(CCT_TO_XYB_D_Y, 0, x);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert color temp K at max brightness to CIE xy D65/2° doubles 0-1. Valid for 1667K-25000K.
	 * https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	 */
	public static double[] cctToXyb(int k) {
		k = MathUtil.limit(k, K_MIN, K_MAX);
		double x = polynomial(CCT_TO_XYB_X, k <= K_MIN_X ? 0 : 1, RKK / k);
		double y = polynomial(CCT_TO_XYB_Y, k <= K_MIN_Y0 ? 0 : k <= K_MIN_Y1 ? 1 : 2, x);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert sRGB int to CIE xyY D65/2° doubles 0-1.
	 */
	public static double[] rgbToXyb(int rgb) {
		return xyzToXyb(rgbToXyz(rgb));
	}

	/**
	 * Convert CIE xyY D65/2° doubles 0-1 to sRGB int.
	 */
	public static int xybToRgb(double... xyy) {
		return xyzToRgb(xybToXyz(xyy));
	}

	/**
	 * Convert sRGB int to CIE XYZ D65/2° doubles 0-1.
	 */
	public static double[] rgbToXyz(int rgb) {
		return LRGB_TO_XYZ.multiply(srgbToLrgb(rgbRatios(rgb))).columnValues(0);
	}

	/**
	 * Convert CIE XYZ D65/2° doubles 0-1 to sRGB int.
	 * https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	 */
	public static int xyzToRgb(double... xyz) {
		return rgbValues(lrgbToSrgb(XYZ_TO_LRGB.multiply(Matrix.vector(xyz))));
	}

	/**
	 * Convert CIE XYZ to CIE xyY. All values 0-1+.
	 */
	public static double[] xyzToXyb(double... xyz) {
		double sum = xyz[0] + xyz[1] + xyz[2];
		if (sum <= 0) return new double[] { 0, 0, 0 };
		return new double[] { xyz[0] / sum, xyz[1] / sum, xyz[1] };
	}

	/**
	 * Convert CIE xyY to CIE XYZ. All values 0-1+.
	 */
	public static double[] xybToXyz(double... xyb) {
		if (xyb[1] <= 0) return new double[] { 0, 0, 0 };
		double m = xyb[2] / xyb[1];
		return new double[] { xyb[0] * m, xyb[2], (1 - xyb[0] - xyb[1]) * m };
	}

	/**
	 * Convert sRGB int to CIE L (no UV) D65/2°.
	 */
	public static double rgbToL(int rgb) {
		return D65_LUV.rgbToL(rgb);
	}

	/**
	 * Convert sRGB int to CIE LUV D65/2°.
	 */
	public static double[] rgbToLuv(int rgb) {
		return D65_LUV.rgbToLuv(rgb);
	}

	/**
	 * Convert CIE LUV D65/2° to sRGB.
	 */
	public static int luvToRgb(double... luv) {
		return D65_LUV.luvToRgb(luv);
	}

	/* support methods */

	private static Matrix rgbRatios(int rgb) {
		return Matrix.vector(ColorUtil.ratio(ColorUtil.r(rgb)), ColorUtil.ratio(ColorUtil.g(rgb)),
			ColorUtil.ratio(ColorUtil.b(rgb)));
	}

	private static int rgbValues(Matrix vector) {
		return ColorUtil.argb(ColorUtil.value(vector.value(0, 0)),
			ColorUtil.value(vector.value(1, 0)), ColorUtil.value(vector.value(2, 0)));
	}

	/**
	 * Gamma correction to get sRGB from linear RGB
	 */
	private static Matrix lrgbToSrgb(Matrix srgb) {
		return srgb.apply(SRGB_GAMMA::compress);
	}

	/**
	 * Reverse gamma correction to get linear RGB from sRGB
	 */
	private static Matrix srgbToLrgb(Matrix srgb) {
		return srgb.apply(SRGB_GAMMA::expand);
	}

	private static double rgbToY(int rgb) {
		return LRGB_TO_XYZ.row(1).multiply(srgbToLrgb(rgbRatios(rgb))).value(0, 0);
	}

	private static double yToL(double yn, double y) {
		return L_GAMMA.compress(y / yn);
	}

	private static double lToY(double yn, double l) {
		return yn * L_GAMMA.expand(l);
	}

	/**
	 * Combines Y with calculated (u', v') chromaticity coordinates from XYZ.
	 */
	private static double[] xyzToYuv(double... xyz) {
		Matrix duv = XYZ_TO_DUV.multiply(Matrix.vector(xyz));
		double m = 1 / duv.value(0, 0);
		return new double[] { xyz[1], duv.value(1, 0) * m, duv.value(2, 0) * m };
	}

	/**
	 * Calculates XYZ from Y and (u', v') chromaticity coordinates.
	 */
	private static double[] yuvToXyz(double... yuv) {
		Matrix dxz = YUV_TO_DXZ.multiply(Matrix.vector(1, yuv[1], yuv[2]));
		double m = yuv[0] / dxz.value(0, 0);
		return new double[] { dxz.value(1, 0) * m, yuv[0], dxz.value(2, 0) * m };
	}

	private static double polynomial(Matrix m, int row, double x) {
		double sum = m.value(row, 0);
		double a = 1;
		for (int c = 1; c < m.columns; c++) {
			a *= x;
			sum += m.value(row, c) * a;
		}
		return sum;
	}
}
