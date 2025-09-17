package ceri.common.color;

import java.awt.Color;

/**
 * Represents a CIE XYZ color with alpha. All values approximately 0-1.
 */
public record XyzColor(double a, double x, double y, double z) {

	public static final XyzColor CIE_A = of(1.09850, 1.0, 0.35585);
	public static final XyzColor CIE_B = of(0.99072, 1.0, 0.85223);
	public static final XyzColor CIE_C = of(0.98074, 1.0, 1.18232);
	public static final XyzColor CIE_D50 = of(0.96422, 1.0, 0.82521);
	public static final XyzColor CIE_D55 = of(0.95682, 1.0, 0.92149);
	public static final XyzColor CIE_D65 = of(0.95047, 1.0, 1.08883);
	public static final XyzColor CIE_D75 = of(0.94972, 1.0, 1.22638);
	public static final XyzColor CIE_E = of(1.0, 1.0, 1.0);
	public static final XyzColor CIE_F2 = of(0.99186, 1.0, 0.67393);
	public static final XyzColor CIE_F7 = of(0.95041, 1.0, 1.08747);
	public static final XyzColor CIE_F11 = of(1.00962, 1.0, 0.64350);

	/**
	 * Construct from sRGB color. Alpha is maintained.
	 */
	public static XyzColor from(Color color) {
		return from(color.getRGB());
	}

	/**
	 * Construct an opaque instance from sRGB int value.
	 */
	public static XyzColor fromRgb(int rgb) {
		double[] xyz = ColorSpaces.rgbToXyz(rgb);
		return of(xyz[0], xyz[1], xyz[2]);
	}

	/**
	 * Construct from sRGB int value. Alpha is maintained.
	 */
	public static XyzColor from(int argb) {
		double[] xyz = ColorSpaces.rgbToXyz(argb);
		return of(Colors.ratio(Colors.a(argb)), xyz[0], xyz[1], xyz[2]);
	}

	/**
	 * Construct opaque instance from CIE XYZ 0-1 values.
	 */
	public static XyzColor of(double x, double y, double z) {
		return new XyzColor(Colors.MAX_RATIO, x, y, z);
	}

	/**
	 * Construct from alpha + CIE XYZ 0-1 values.
	 */
	public static XyzColor of(double a, double x, double y, double z) {
		return new XyzColor(a, x, y, z);
	}

	/**
	 * Provide XYZ 0-1 values. Alpha is dropped.
	 */
	public double[] xyzValues() {
		return new double[] { x, y, z };
	}

	/**
	 * Convert to sRGB int. Alpha is maintained.
	 */
	public int argb() {
		return Component.a.set(ColorSpaces.xyzToRgb(x, y, z), Colors.value(a));
	}

	/**
	 * Convert to sRGB color. Alpha is maintained.
	 */
	public Color color() {
		return Colors.color(argb());
	}

	/**
	 * Convert to sRGB. Alpha is maintained.
	 */
	public RgbColor rgb() {
		double[] rgb = rgbValues();
		return RgbColor.of(a, rgb[0], rgb[1], rgb[2]);
	}

	/**
	 * Convert to sRGB 0-1 values. Alpha is dropped.
	 */
	public double[] rgbValues() {
		return ColorSpaces.xyzToSrgb(x, y, z);
	}

	/**
	 * Convert to CIE xyY. Alpha is maintained.
	 */
	public XybColor xyb() {
		double[] xyb = xybValues();
		return XybColor.of(a, xyb[0], xyb[1], xyb[2]);
	}

	/**
	 * Convert to CIE xyY 0-1 values. Alpha is dropped.
	 */
	public double[] xybValues() {
		return ColorSpaces.xyzToXyb(x, y, z);
	}

	/**
	 * Creates a CIE LUV reference of this color.
	 */
	public LuvColor.Ref luvRef() {
		return LuvColor.Ref.from(x, y, z);
	}

	/**
	 * Returns true if not opaque.
	 */
	public boolean hasAlpha() {
		return a < Colors.MAX_RATIO;
	}

	/**
	 * Normalizes values by first converting to CIE xyY.
	 */
	public XyzColor normalize() {
		var xyb = xyb();
		var normalXyb = xyb.normalize();
		return xyb == normalXyb ? this : normalXyb.xyz();
	}

	@Override
	public String toString() {
		return String.format("(a=%.5f,x=%.5f,y=%.5f,z=%.5f)", a, x, y, z);
	}
}
