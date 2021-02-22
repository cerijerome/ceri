package ceri.common.color;

import static java.lang.Math.pow;
import ceri.common.math.MathUtil;
import ceri.common.math.Matrix;

/**
 * Utilities to convert between color spaces. https://en.wikipedia.org/wiki/Color_space
 */
public class ColorSpaces {
	// https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	private static final Matrix XYZ_TO_RGB = Matrix.of(new double[][] { //
		{ 3.24096994, -1.53738318, -0.49861076 }, //
		{ -0.96924364, 1.8759675, 0.04155506 }, //
		{ 0.05563008, -0.20397696, 1.05697151 } });
	private static final Matrix RGB_TO_XYZ = XYZ_TO_RGB.invert();
	private static final double GAMMA_AL = 12.92;
	private static final double GAMMA_AH = 0.055;
	private static final double GAMMA_P = 2.4;
	private static final double GAMMA_LOW = 0.0031308;
	private static final double GAMMA_LOW_EXP = GAMMA_LOW * GAMMA_AL;
	// https://en.wikipedia.org/wiki/Standard_illuminant#Illuminant_series_D
	private static final Matrix CCT_TO_XYB_D_X = Matrix.of(new double[][] { //
		{ 0.244063, 0.09911, 2.9678, -4.607 }, // <= K_MIN_D_X
		{ 0.23704, 0.24748, 1.9018, -2.0064 } });
	private static final Matrix CCT_TO_XYB_D_Y = Matrix.of(new double[][] { //
		{ -0.275, 2.87, -3 } });
	// https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	private static final Matrix CCT_TO_XYB_X = Matrix.of(new double[][] { //
		{ 0.179910, 0.8776956, -0.2343589, -0.2661239 }, // <= K_MIN_X
		{ 0.240390, 0.2226347, 2.1070379, -3.0258469 } });
	private static final Matrix CCT_TO_XYB_Y = Matrix.of(new double[][] { //
		{ -0.20219683, 2.18555832, -1.34811020, -1.1063814 }, // <= K_MIN_Y0
		{ -0.16748867, 2.09137015, -1.37418593, -0.9549476 }, // <= K_MIN_Y1
		{ -0.37001483, 3.75112997, -5.8733867, 3.0817580 } });
	private static final int K_MIN = 1300;
	private static final int K_MAX = 25000;
	private static final int K_MIN_D_X = 7000;
	private static final int K_MIN_X = 4000;
	private static final int K_MIN_Y0 = 2222;
	private static final int K_MIN_Y1 = 4000;

	private ColorSpaces() {}

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
		double x = polynomial(CCT_TO_XYB_D_X, k <= K_MIN_D_X ? 0 : 1, 1000.0 / k);
		double y = polynomial(CCT_TO_XYB_D_Y, 0, x);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert color temp K at max brightness to CIE xy D65/2° doubles 0-1. Valid for 1667K-25000K.
	 * https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	 */
	public static double[] cctToXyb(int k) {
		k = MathUtil.limit(k, K_MIN, K_MAX);
		double x = polynomial(CCT_TO_XYB_X, k <= K_MIN_X ? 0 : 1, 1000.0 / k);
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
	 * https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	 */
	public static double[] rgbToXyz(int rgb) {
		return RGB_TO_XYZ.multiply(srgbToLinear(rgbRatios(rgb))).columnValues(0);
	}

	/**
	 * Convert CIE XYZ D65/2° doubles 0-1 to sRGB int.
	 * https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	 */
	public static int xyzToRgb(double... xyz) {
		return rgbValues(linearToSrgb(XYZ_TO_RGB.multiply(Matrix.vector(xyz))));
	}

	/**
	 * Convert CIE XYZ to CIE xyY. All values 0-1.
	 */
	public static double[] xyzToXyb(double... xyz) {
		double sum = xyz[0] + xyz[1] + xyz[2];
		return new double[] { xyz[0] / sum, xyz[1] / sum, xyz[1] };
	}

	/**
	 * Convert CIE xyY to CIE XYZ. All values 0-1.
	 */
	public static double[] xybToXyz(double... xyy) {
		double m = xyy[2] / xyy[1];
		return new double[] { xyy[0] * m, xyy[2], (1 - xyy[0] - xyy[1]) * m };
	}

	/* support methods */

	private static Matrix rgbRatios(int rgb) {
		return Matrix.vector(ColorUtil.ratio(ColorUtil.r(rgb)), ColorUtil.ratio(ColorUtil.g(rgb)),
			ColorUtil.ratio(ColorUtil.b(rgb)));
	}

	private static int rgbValues(Matrix vector) {
		return ColorUtil.argb(0, ColorUtil.value(vector.value(0, 0)),
			ColorUtil.value(vector.value(1, 0)), ColorUtil.value(vector.value(2, 0)));
	}

	/**
	 * Gamma correction to get sRGB from linear RGB
	 */
	private static Matrix linearToSrgb(Matrix srgb) {
		return srgb.apply(ColorSpaces::gammaCompress);
	}

	/**
	 * Reverse gamma correction to get linear RGB from sRGB
	 */
	private static Matrix srgbToLinear(Matrix srgb) {
		return srgb.apply(ColorSpaces::gammaExpand);
	}

	private static double gammaCompress(double u) {
		return u <= GAMMA_LOW ? GAMMA_AL * u : (1 + GAMMA_AH) * pow(u, 1 / GAMMA_P) - GAMMA_AH;
	}

	private static double gammaExpand(double u) {
		return u <= GAMMA_LOW_EXP ? u / GAMMA_AL : pow((u + GAMMA_AH) / (1 + GAMMA_AH), GAMMA_P);
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
