package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMin;

public class Shape3dUtil {

	private Shape3dUtil() {}

	public static <T extends Radial3d> InvertedRadial3d<T> invert(T radial) {
		return InvertedRadial3d.create(radial);
	}

	public static <T extends Radial3d> TruncatedRadial3d<T> truncate(T radial, double h0, double h) {
		return TruncatedRadial3d.create(radial, h0, h);
	}

	public static Radial3d conicalFrustum(double r0, double r1, double h) {
		validateMin(r0, 0);
		validateMin(r1, 0);
		validateMin(h, 0);
		if (r0 == r1) return Cylinder3d.create(r0, h);
		if (r0 == 0) return Cone3d.create(r1, h);
		if (r1 == 0) return invert(Cone3d.create(r0, h));
		double r = Math.max(r0, r1);
		double H = h * r / Math.abs(r1 - r0);
		Radial3d shape = truncate(Cone3d.create(r, H), H - h, h);
		if (r0 > r1) shape = invert(shape);
		return shape;
	}

	/**
	 * Creates a truncated spheroid with lower radius r0, upper radius r1, of given height h, and
	 * gradient m at r1.
	 */
	public static TruncatedRadial3d<Spheroid3d> truncatedSpheroidFromGradient(double r0, double r1,
		double h, double m) {
		if (m > 0) return spheroidFromPositiveGradient(r0, r1, h, m);
		return spheroidFromNegativeGradient(r0, r1, h, m);
	}

	/**
	 * Creates a truncated spheroid with given radius r (a-axis and b-axis), c-axis c, lower height
	 * offset h0 (-c to +c) and height h (0 to 2c).
	 */
	public static TruncatedRadial3d<Spheroid3d> truncatedSpheroid(double r, double c, double h0,
		double h) {
		Spheroid3d spheroid = Spheroid3d.create(r, c);
		return truncate(spheroid, h0, h);
	}

	private static TruncatedRadial3d<Spheroid3d> spheroidFromPositiveGradient(double r0, double r1,
		double h, double m) {
		if (r0 >= r1) throw new IllegalArgumentException("r1 must be > " + r0 + ": " + r1);
		double f = m * ((r1 * r1) - (r0 * r0)) / (2 * r1);
		if (h >= f) throw new IllegalArgumentException("h must be < " + f + ": " + h);
		double h1 = h * h / ((f - h) * 2);
		double a = Math.sqrt(r1 * (h1 + (m * r1)) / m);
		double b = Math.sqrt(h1 * (h1 + (m * r1)));
		double h0 = r0 == 0 ? -b : -(h1 + h);
		return truncatedSpheroid(a, b, h0, h);
	}

	private static TruncatedRadial3d<Spheroid3d> spheroidFromNegativeGradient(double r0, double r1,
		double h, double m) {
		double f = -m * ((r0 * r0) - (r1 * r1)) / (2 * r1);
		double hmin = Math.max(f, 0);
		if (h <= hmin) throw new IllegalArgumentException("h must be > " + hmin + ": " + h);
		double h1 = h * h / ((h - f) * 2);
		double a = Math.sqrt(r1 * (h1 + (-m * r1)) / -m);
		double b = Math.sqrt(h1 * (h1 + (-m * r1)));
		double h0 = r0 == 0 ? -b : h1 - h;
		return truncatedSpheroid(a, b, h0, h);
	}

}
