package ceri.common.color;

import ceri.common.math.Maths;
import ceri.common.math.Matrix;

/**
 * Utility methods to convert between color spaces. All calculations based on CIE D65 and 2°
 * observer unless noted. https://en.wikipedia.org/wiki/Color_space
 */
public class ColorSpaces {
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
	// https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	// https://drafts.csswg.org/css-color-4/#color-conversion-code
	private static final Matrix LRGB_TO_XYZ = Matrix.of(new double[][] { //
		{ 0.412390799265959, 0.357584339383878, 0.180480788401834 },
		{ 0.212639005871510, 0.715168678767756, 0.072192315360734 },
		{ 0.019330818715592, 0.119194779794626, 0.950532152249661 } });
	private static final Matrix XYZ_TO_LRGB = LRGB_TO_XYZ.invert();
	private static final NonLinear SRGB_GAMMA = new NonLinear(0.0031308, 12.92, 0.055, 2.4);
	// https://en.wikipedia.org/wiki/CIELUV#The_forward_transformation
	private static final Matrix XYZ_TO_DUV =
		Matrix.of(new double[][] { { 1, 15, 3 }, { 4, 0, 0 }, { 0, 9, 0 } });
	private static final Matrix YUV_TO_DXZ =
		Matrix.of(new double[][] { { 0, 0, 4 }, { 0, 9, 0 }, { 12, -3, -20 } });
	private static final NonLinear L_FN = new NonLinear(2.16 / 24389, 243.89 / 27, 0.16, 3);
	private static final double UVLM = 13; // u*v* L* multiplier

	private ColorSpaces() {}

	/**
	 * Provides split compress/expand non-linear functions. Used for sRGB gamma correction, and to
	 * calculate CIELUV L* from Y/Yn. The low function is a simple multiplication, and the high
	 * function is a reciprocal power with multiplier and offset.
	 */
	private static class NonLinear {
		private final double low; // the low/high boundary for compress
		private final double lowExp; // the low/high boundary for expand
		private final double aLow; // the low multiplier
		private final double aHigh; // the high multiplier/offset
		private final double p; // the high reciprocal power

		private NonLinear(double low, double aLow, double aHigh, double p) {
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
			return u <= low ? aLow * u : (1 + aHigh) * Math.pow(u, 1 / p) - aHigh;
		}

		/**
		 * Execute the split gamma expand function on the value.
		 */
		public double expand(double u) {
			return u <= lowExp ? u / aLow : Math.pow((u + aHigh) / (1 + aHigh), p);
		}
	}

	/**
	 * Limit values to 0-1, modifying values in place.
	 */
	public static double[] limit(double... values) {
		for (int i = 0; i < values.length; i++)
			values[i] = Maths.limit(values[i], 0, 1);
		return values;
	}

	/**
	 * Scale values to 0-1, modifying values in place.
	 */
	public static double[] scale(double... values) {
		double min = Math.min(Maths.min(values), 0);
		double d = Math.max(Maths.max(values), 1) - min;
		if (min < 0 || d > 1) for (int i = 0; i < values.length; i++)
			values[i] = (values[i] - min) / d;
		return values;
	}

	/**
	 * Convert CIE daylight illuminant (Ddd) at max brightness to sRGB int value.
	 */
	public static int dToRgb(int d) {
		return xybToRgb(dToXyb(d));
	}

	/**
	 * Convert CIE daylight illuminant (Ddd) at max brightness to sRGB 0-1 values.
	 */
	public static double[] dToSrgb(int d) {
		return xybToSrgb(dToXyb(d));
	}

	/**
	 * Convert CIE daylight illuminant (Ddd) at max brightness to CIE XYZ 0-1 values.
	 */
	public static double[] dToXyz(int d) {
		return xybToXyz(dToXyb(d));
	}

	/**
	 * Convert CIE daylight illuminant (Dxx) at max brightness to CIE xyY 0-1 values.
	 */
	public static double[] dToXyb(int d) {
		int k = Maths.limit(d * 100, K_MIN, K_MAX);
		double x = polynomial(CCT_TO_XYB_D_X, k <= K_MIN_D_X ? 0 : 1, RKK / k);
		double y = polynomial(CCT_TO_XYB_D_Y, 0, x);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert color temp K at max brightness to sRGB int value. Valid for 1667K-25000K.
	 */
	public static int cctToRgb(int k) {
		return xybToRgb(cctToXyb(k));
	}

	/**
	 * Convert color temp K at max brightness to sRGB 0-1 values. Valid for 1667K-25000K.
	 */
	public static double[] cctToSrgb(int k) {
		return xybToSrgb(cctToXyb(k));
	}

	/**
	 * Convert color temp K at max brightness to CIE XYZ D65/2° 0-1 values. Valid for 1667K-25000K.
	 */
	public static double[] cctToXyz(int k) {
		return xybToXyz(cctToXyb(k));
	}

	/**
	 * Convert color temp K at max brightness to CIE xyY D65/2°. Valid for 1667K-25000K.
	 * https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	 */
	public static double[] cctToXyb(int k) {
		k = Maths.limit(k, K_MIN, K_MAX);
		double x = polynomial(CCT_TO_XYB_X, k <= K_MIN_X ? 0 : 1, RKK / k);
		double y = polynomial(CCT_TO_XYB_Y, k <= K_MIN_Y0 ? 0 : k <= K_MIN_Y1 ? 1 : 2, x);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert ARGB int value to HSB 0-1 values. Double version of Color.RGBtoHSB().
	 */
	public static double[] rgbToHsb(int rgb) {
		return srgbToHsb(srgb(rgb));
	}

	/**
	 * Convert RGB 0-1 values to HSB 0-1 values. Double version of Color.RGBtoHSB().
	 */
	public static double[] srgbToHsb(double... rgb) {
		double max = Maths.max(rgb);
		double diff = max - Maths.min(rgb);
		double b = max;
		double s = max == 0 ? 0 : diff / max;
		double h = s == 0 ? 0 : hue(max, diff, rgb);
		if (h < 0) h += 1;
		return new double[] { h, s, b };
	}

	/**
	 * Convert HSB 0-1 values to RGB int value. Double version of Color.HSBtoRGB().
	 */
	public static int hsbToRgb(double... hsb) {
		return rgb(hsbToSrgb(hsb));
	}

	/**
	 * Convert HSB 0-1 values to RGB 0-1 values. Double version of Color.HSBtoRGB().
	 */
	public static double[] hsbToSrgb(double... hsb) {
		if (hsb[1] == 0) return new double[] { hsb[2], hsb[2], hsb[2] };
		double h = (hsb[0] - Math.floor(hsb[0])) * 6.0f;
		double f = h - Math.floor(h);
		double p = hsb[2] * (1.0 - hsb[1]);
		double q = hsb[2] * (1.0 - hsb[1] * f);
		double t = hsb[2] * (1.0 - hsb[1] * (1.0 - f));
		return switch ((int) h) {
			case 0 -> new double[] { hsb[2], t, p };
			case 1 -> new double[] { q, hsb[2], p };
			case 2 -> new double[] { p, hsb[2], t };
			case 3 -> new double[] { p, q, hsb[2] };
			case 4 -> new double[] { t, p, hsb[2] };
			default -> new double[] { hsb[2], p, q };
		};
	}

	/**
	 * Convert sRGB 0-1 values to int value, limiting component values to 0-255.
	 */
	public static int rgb(double... srgb) {
		return Component.a.intMask | Component.r.intValue(srgb[0]) | Component.g.intValue(srgb[1])
			| Component.b.intValue(srgb[2]);
	}

	/**
	 * Convert sRGB 0-1 values to CIE XYZ 0-1 values.
	 */
	public static double[] rgbToXyz(int rgb) {
		return srgbToXyz(srgb(rgb));
	}

	/**
	 * Convert sRGB 0-1 values to CIE xyY 0-1 values.
	 */
	public static double[] rgbToXyb(int rgb) {
		return srgbToXyb(srgb(rgb));
	}

	/**
	 * Convert sRGB int value to CIE LUV L* 0-1 value.
	 */
	public static double rgbToL(double yn, int rgb) {
		return srgbToL(yn, srgb(rgb));
	}

	/**
	 * Convert sRGB int value to CIE L*u*v* 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] rgbToLuv(double yn, double un, double vn, int rgb) {
		return srgbToLuv(yn, un, vn, srgb(rgb));
	}

	/**
	 * Convert sRGB int value to 0-1 values.
	 */
	public static double[] srgb(int argb) {
		return new double[] { Component.r.ratio(argb), Component.g.ratio(argb),
			Component.b.ratio(argb) };
	}

	/**
	 * Convert sRGB 0-1 values to CIE XYZ 0-1 values.
	 */
	public static double[] srgbToXyz(double... rgb) {
		Matrix srgb = Matrix.vector(rgb);
		return LRGB_TO_XYZ.multiply(srgbToLrgb(srgb)).columnValues(0);
	}

	/**
	 * Convert sRGB 0-1 values to CIE xyY 0-1 values.
	 */
	public static double[] srgbToXyb(double... rgb) {
		return xyzToXyb(srgbToXyz(rgb));
	}

	/**
	 * Convert sRGB 0-1 values to CIE LUV L* 0-1 value.
	 */
	public static double srgbToL(double yn, double... rgb) {
		Matrix srgb = Matrix.vector(rgb);
		double y = LRGB_TO_XYZ.row(1).multiply(srgbToLrgb(srgb)).at(0, 0);
		return yToL(yn, y);
	}

	/**
	 * Convert sRGB 0-1 values to CIE L*u*v* 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] srgbToLuv(double yn, double un, double vn, double... rgb) {
		return xyzToLuv(yn, un, vn, srgbToXyz(rgb));
	}

	/**
	 * Convert CIE XYZ 0-1 values to sRGB int value.
	 */
	public static int xyzToRgb(double... xyz) {
		return rgb(lrgbToSrgb(XYZ_TO_LRGB.multiply(Matrix.vector(xyz))));
	}

	/**
	 * Convert CIE XYZ 0-1 values to sRGB 0-1 values.
	 */
	public static double[] xyzToSrgb(double... xyz) {
		return lrgbToSrgb(XYZ_TO_LRGB.multiply(Matrix.vector(xyz))).columnValues(0);
	}

	/**
	 * Convert CIE XYZ 0-1 values to CIE xyY 0-1 values.
	 */
	public static double[] xyzToXyb(double... xyz) {
		double sum = xyz[0] + xyz[1] + xyz[2];
		if (sum <= 0) return new double[] { 0, 0, 0 };
		return new double[] { xyz[0] / sum, xyz[1] / sum, xyz[1] };
	}

	/**
	 * Convert CIE XYZ 0-1 values to CIE LUV reference Yn, un', and vn' 0-1 values.
	 */
	public static double[] xyzToYuv(double... xyz) {
		Matrix duv = XYZ_TO_DUV.multiply(Matrix.vector(xyz));
		double d = duv.at(0, 0);
		double m = d == 0 ? 0 : 1 / duv.at(0, 0);
		return new double[] { xyz[1], duv.at(1, 0) * m, duv.at(2, 0) * m };
	}

	/**
	 * Convert CIE XYZ 0-1 values to CIE L*u*v* 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] xyzToLuv(double yn, double un, double vn, double... xyz) {
		double[] yuv = xyzToYuv(xyz); // Y, u', v'
		// re-using double[] Yu'v' for L*u*v*
		yuv[0] = yToL(yn, yuv[0]); // L* from Yn, Y
		yuv[1] = UVLM * yuv[0] * (yuv[1] - un); // u* from L*, u', un'
		yuv[2] = UVLM * yuv[0] * (yuv[2] - vn); // v* from L*, v', vn'
		return yuv; // actually L*u*v*
	}

	/**
	 * Convert CIE xyY 0-1 values to sRGB int value.
	 */
	public static int xybToRgb(double... xyb) {
		return xyzToRgb(xybToXyz(xyb));
	}

	/**
	 * Convert CIE xyY 0-1 values to sRGB 0-1 values.
	 */
	public static double[] xybToSrgb(double... xyb) {
		return xyzToSrgb(xybToXyz(xyb));
	}

	/**
	 * Convert CIE xyY 0-1 values to CIE XYZ 0-1 values.
	 */
	public static double[] xybToXyz(double... xyb) {
		if (xyb[1] <= 0) return new double[] { 0, 0, 0 };
		double m = xyb[2] / xyb[1];
		return new double[] { xyb[0] * m, xyb[2], (1 - xyb[0] - xyb[1]) * m };
	}

	/**
	 * Convert CIE xyY 0-1 values to CIE L*u*v* 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] xybToLuv(double yn, double un, double vn, double... xyz) {
		return xyzToLuv(yn, un, vn, xybToXyz(xyz));
	}

	/**
	 * Convert CIE Y value to CIE L*, using Yn reference.
	 */
	public static double yToL(double yn, double y) {
		return L_FN.compress(y / yn);
	}

	/**
	 * Convert CIE L* value to CIE Y, using Yn reference.
	 */
	public static double lToY(double yn, double l) {
		return yn * L_FN.expand(l);
	}

	/**
	 * Convert CIE L*u*v* 0-1 values to sRGB int value using reference Yn, un', and vn'.
	 */
	public static int luvToRgb(double yn, double un, double vn, double... luv) {
		return xyzToRgb(luvToXyz(yn, un, vn, luv));
	}

	/**
	 * Convert CIE L*u*v* 0-1 values to sRGB 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] luvToSrgb(double yn, double un, double vn, double... luv) {
		return xyzToSrgb(luvToXyz(yn, un, vn, luv));
	}

	/**
	 * Convert CIE L*u*v* 0-1 values to CIE XYZ 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] luvToXyz(double yn, double un, double vn, double... luv) {
		double y = lToY(yn, luv[0]); // Y from Yn, L*
		if (luv[0] == 0) return yuvToXyz(y, un, vn);
		double u = luv[1] / (UVLM * luv[0]) + un; // u' from L*, u*, un'
		double v = luv[2] / (UVLM * luv[0]) + vn; // v' from L*, v*, vn'
		return yuvToXyz(y, u, v);
	}

	/**
	 * Convert CIE L*u*v* 0-1 values to CIE xyY 0-1 values using reference Yn, un', and vn'.
	 */
	public static double[] luvToXyb(double yn, double un, double vn, double... luv) {
		return xyzToXyb(luvToXyz(yn, un, vn, luv));
	}

	/* support methods */

	private static double hue(double max, double diff, double... rgb) {
		double rc = (max - rgb[0]) / diff;
		double gc = (max - rgb[1]) / diff;
		double bc = (max - rgb[2]) / diff;
		if (rgb[0] == max) return (bc - gc) / 6;
		if (rgb[1] == max) return (2 + rc - bc) / 6;
		return (4 + gc - rc) / 6;
	}

	private static int rgb(Matrix srgb) {
		return rgb(srgb.at(0, 0), srgb.at(1, 0), srgb.at(2, 0));
	}

	private static Matrix lrgbToSrgb(Matrix srgb) {
		return srgb.apply(SRGB_GAMMA::compress);
	}

	private static Matrix srgbToLrgb(Matrix srgb) {
		return srgb.apply(SRGB_GAMMA::expand);
	}

	private static double[] yuvToXyz(double... yuv) {
		Matrix dxz = YUV_TO_DXZ.multiply(Matrix.vector(1, yuv[1], yuv[2]));
		double m = yuv[0] / dxz.at(0, 0);
		return new double[] { dxz.at(1, 0) * m, yuv[0], dxz.at(2, 0) * m };
	}

	private static double polynomial(Matrix m, int row, double x) {
		double sum = m.at(row, 0);
		double a = 1;
		for (int c = 1; c < m.columns; c++) {
			a *= x;
			sum += m.at(row, c) * a;
		}
		return sum;
	}
}
