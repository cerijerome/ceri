package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMinFp;

public class Shape3dUtil {

	private Shape3dUtil() {}

	public static <T extends Radial3d> InvertedRadial3d<T> invert(T radial) {
		return InvertedRadial3d.create(radial);
	}

	public static <T extends Radial3d> TruncatedRadial3d<T> truncate(T radial, double h0,
		double h) {
		return TruncatedRadial3d.create(radial, h0, h);
	}

	public static Radial3d conicalFrustum(double r0, double r1, double h) {
		validateMinFp(r0, 0);
		validateMinFp(r1, 0);
		validateMinFp(h, 0);
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
	 * Creates a truncated concave spheroid with given radius r, elliptical axes a and c , lower
	 * height offset h0 (-c to +c) and height h (0 to 2c).
	 */
	public static TruncatedRadial3d<ConcaveSpheroid3d> truncatedConcaveSpheroid(double r, double a,
		double c, double h0, double h) {
		ConcaveSpheroid3d spheroid = ConcaveSpheroid3d.create(r, a, c);
		return truncate(spheroid, h0, h);
	}

	/**
	 * Creates a truncated concave semi-spheroid with minimum radius r0, large radius 1, height h,
	 * with gradient m at r1. The truncated concave spheroid either starts (m < 0) or ends (m > 0)
	 * at the spheroid mid-point.
	 */
	public static TruncatedRadial3d<ConcaveSpheroid3d>
		truncatedConcaveSemiSpheroidFromGradient(double r0, double r1, double h, double m) {
		validateMinFp(r0, 0, "Minimum radius r0");
		if (r1 <= r0)
			throw new IllegalArgumentException("Large radius r1 must be > " + r0 + ": " + r1);
		if (m == 0) throw new IllegalArgumentException("Gradient m cannot be 0: " + m);
		if (m > 0) return truncatedConcaveSemiSpheroidFromPositiveGradient(r0, r1, h, m);
		return truncatedConcaveSemiSpheroidFromNegativeGradient(r0, r1, h, m);
	}

	private static TruncatedRadial3d<ConcaveSpheroid3d>
		truncatedConcaveSemiSpheroidFromPositiveGradient(double r0, double r1, double h, double m) {
		double rd = r1 - r0;
		double d = 2.0 * m * rd;
		validateMinFp(h, d, "Height");
		double a = rd * (h - (m * rd)) / (h - d);
		double c = a * Math.sqrt(m * h / (a - rd));
		double r = r0 + a;
		return truncatedConcaveSpheroid(r, a, c, c, h);
	}

	private static TruncatedRadial3d<ConcaveSpheroid3d>
		truncatedConcaveSemiSpheroidFromNegativeGradient(double r0, double r1, double h, double m) {
		double rd = r1 - r0;
		double d = 2.0 * -m * rd;
		validateMinFp(h, d, "Height");
		double a = rd * (h - (-m * rd)) / (h - d);
		double c = a * Math.sqrt(-m * h / (a - rd));
		double r = r0 + a;
		return truncatedConcaveSpheroid(r, a, c, c - h, h);
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

	/**
	 * Creates a truncated spheroid with lower radius r0, upper radius r1, of given height h, and
	 * gradient m at r1.
	 */
	public static TruncatedRadial3d<Spheroid3d> truncatedSpheroidFromGradient(double r0, double r1,
		double h, double m) {
		if (m > 0) return spheroidFromPositiveGradient(r0, r1, h, m);
		return spheroidFromNegativeGradient(r0, r1, h, m);
	}

	private static TruncatedRadial3d<Spheroid3d> spheroidFromPositiveGradient(double r0, double r1,
		double h, double m) {
		if (r0 >= r1) throw new IllegalArgumentException("r1 must be > " + r0 + ": " + r1);
		double f = m * ((r1 * r1) - (r0 * r0)) / (2 * r1);
		if (h >= f) throw new IllegalArgumentException("h must be < " + f + ": " + h);
		double h1 = h * h / ((f - h) * 2);
		double a = Math.sqrt(r1 * (h1 + (m * r1)) / m);
		double b = Math.sqrt(h1 * (h1 + (m * r1)));
		double h0 = r0 == 0 ? 0 : h + h1;
		return truncatedSpheroid(a, b, b - h0, h);
	}

	private static TruncatedRadial3d<Spheroid3d> spheroidFromNegativeGradient(double r0, double r1,
		double h, double m) {
		double f = -m * ((r0 * r0) - (r1 * r1)) / (2 * r1);
		double hmin = Math.max(f, 0);
		if (h <= hmin) throw new IllegalArgumentException("h must be > " + hmin + ": " + h);
		double h1 = h * h / ((h - f) * 2);
		double a = Math.sqrt(r1 * (h1 + (-m * r1)) / -m);
		double b = Math.sqrt(h1 * (h1 + (-m * r1)));
		double h0 = r0 == 0 ? b : h - h1;
		return truncatedSpheroid(a, b, b - h0, h);
	}

}
