package ceri.common.color;

import static ceri.common.color.ColorUtil.MAX_VALUE;
import static ceri.common.color.ColorUtil.rgb;
import static ceri.common.math.MathUtil.intRoundExact;
import static ceri.common.math.MathUtil.limit;
import static ceri.common.math.MathUtil.polynomial;
import java.util.Arrays;
import ceri.common.geom.Point2d;
import ceri.common.math.MathUtil;
import ceri.common.math.Matrix;

public class ColorSpaceUtil {

	private ColorSpaceUtil() {}

	/**
	 * Convert color temp K at max brightness to sRGB int. Valid for 4000K-25000K.
	 */
	public static int cctToRgb(int k) {
		return xybToRgb(cctToXyb(k));
	}

	/**
	 * Convert color temp K at max brightness to CIE xy D65/2° doubles 0-1. Valid for 4000K-25000K.
	 * https://en.wikipedia.org/wiki/Standard_illuminant#Illuminant_series_D
	 */
	public static double[] cctToXybD(int k) {
		k = MathUtil.limit(k, 1300, 25000);
		double m = 1000.0 / k;
		double x = k <= 7000 ? polynomial(m, 0.244063, 0.09911, 2.9678, -4.607) :
			polynomial(m, 0.23704, 0.24748, 1.9018, -2.0064);
		double y = polynomial(x, -0.275, 2.87, -3);
		return new double[] { x, y, 1 };
	}

	/**
	 * Convert color temp K at max brightness to CIE xy D65/2° doubles 0-1. Valid for 1667K-25000K.
	 * https://en.wikipedia.org/wiki/Planckian_locus#Approximation
	 */
	public static double[] cctToXyb(int k) {
		k = MathUtil.limit(k, 1300, 25000);
		double m = 1000.0 / k; // change to m?
		double x = k <= 4000 ? polynomial(m, 0.179910, 0.8776956, -0.2343589, -0.2661239) :
			polynomial(m, 0.240390, 0.2226347, 2.1070379, -3.0258469);
		double y = //
			k <= 2222 ? polynomial(x, -0.20219683, 2.18555832, -1.34811020, -1.1063814) :
				k <= 4000 ? polynomial(x, -0.16748867, 2.09137015, -1.37418593, -0.9549476) :
					polynomial(x, -0.37001483, 3.75112997, -5.8733867, 3.0817580);
		return new double[] { x, y , 1 };
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
		// Reverse gamma correction to get linear RGB from sRGB
		double r = srgbToLinear(ColorUtil.r(rgb));
		double g = srgbToLinear(ColorUtil.g(rgb));
		double b = srgbToLinear(ColorUtil.b(rgb));
		// Calculate XYZ from linear RGB
		double x = r * 0.41239080 + g * 0.35758434 + b * 0.18048079;
		double y = r * 0.21263901 + g * 0.71516868 + b * 0.07219232;
		double z = r * 0.01933082 + g * 0.11919478 + b * 0.95053215;
		return new double[] { x, y, z };
	}

	/**
	 * Convert CIE XYZ D65/2° doubles 0-1 to sRGB int.
	 * https://en.wikipedia.org/wiki/SRGB#Specification_of_the_transformation
	 */
	public static int xyzToRgb(double... xyz) {
		// Calculate linear RGB from XYZ
		double r = xyz[0] * 3.24096994 + xyz[1] * -1.53738318 + xyz[2] * -0.49861076;
		double g = xyz[0] * -0.96924364 + xyz[1] * 1.8759675 + xyz[2] * 0.04155506;
		double b = xyz[0] * 0.05563008 + xyz[1] * -0.20397696 + xyz[2] * 1.05697151;
		// Apply gamma correction to get sRGB from linear RGB
		return ColorUtil.argb(0, linearToSrgb(r), linearToSrgb(g), linearToSrgb(b));
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

	private static double dot(double[] row, double... col) {
		double sum = 0;
		int n = Math.min(row.length, col.length);
		for (int i = 0; i < n; i++)
			sum += row[i] * col[i];
		return sum;
	}

	private static double[] multiply(double[][] matrix, double... col) {
		double[] row = new double[matrix.length];
		for (int i = 0; i < row.length; i++)
			row[i] = dot(matrix[i], col);
		return row;
	}

	private static double srgbToLinear(int v) {
		double u = ColorUtil.ratio(v);
		return (u > 0.04045 ? Math.pow((u + 0.055) / 1.055, 2.4) : u / 12.92);
	}

	private static int linearToSrgb(double u) {
		double v = u > 0.0031308 ? 1.055 * Math.pow(u, 1 / 2.4) - 0.055 : u * 12.92;
		return ColorUtil.value(v);
	}

}
