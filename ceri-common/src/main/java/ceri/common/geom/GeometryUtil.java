package ceri.common.geom;

import static ceri.common.validation.ValidationUtil.validateMax;
import ceri.common.math.MathUtil;

/**
 * Geometric utilities.
 */
public class GeometryUtil {

	private GeometryUtil() {}

	/**
	 * Calculates the angle of a given gradient from -pi/2 to +pi/2
	 */
	public static double angleFromGradient(double m) {
		if (Double.isNaN(m)) return Double.NaN;
		if (m == 0) return 0;
		if (m == Double.POSITIVE_INFINITY) return MathUtil.PI_BY_2;
		if (m == Double.NEGATIVE_INFINITY) return -MathUtil.PI_BY_2;
		return Math.atan(m);
	}

	/**
	 * Resize dimension to fit at least given dimensions while maintaining aspect ratio.
	 */
	public static Dimension2d resizeToMin(Dimension2d size, Dimension2d min) {
		if (size.isNull()) return size;
		if (size.w == 0) return new Dimension2d(0, min.h);
		if (size.h == 0) return new Dimension2d(min.w, 0);
		double w = Math.max(min.w, min.h * size.aspectRatio());
		double h = Math.max(min.h, min.w / size.aspectRatio());
		return new Dimension2d(w, h);
	}

	/**
	 * Resize dimension to fit within given dimensions while maintaining aspect ratio.
	 */
	public static Dimension2d resizeToMax(Dimension2d size, Dimension2d max) {
		if (size.isNull()) return size;
		if (size.w == 0) return new Dimension2d(0, max.h);
		if (size.h == 0) return new Dimension2d(max.w, 0);
		double w = Math.min(max.w, max.h * size.aspectRatio());
		double h = Math.min(max.h, max.w / size.aspectRatio());
		return new Dimension2d(w, h);
	}

	/**
	 * Crop dimension to given dimensions. If dimension is smaller than the desired crop it will
	 * remain unchanged.
	 */
	public static Dimension2d crop(Dimension2d size, Dimension2d crop) {
		return new Dimension2d(Math.min(size.w, crop.w), Math.min(size.h, crop.h));
	}

	/**
	 * Crop to given dimensions, with spacing alignment. If the original size is smaller than the
	 * desired crop in one dimension that dimension is not cropped.
	 */
	public static Rectangle2d crop(Dimension2d size, Dimension2d crop, Ratio2d ratio) {
		crop = crop(size, crop);
		validateMax(ratio.x, 1, "X spacing ratio");
		validateMax(ratio.y, 1, "Y spacing ratio");
		double x = ratio.x * (size.w - crop.w);
		double y = ratio.y * (size.h - crop.h);
		return new Rectangle2d(x, y, crop.w, crop.h);
	}

	/**
	 * Finds the overlapping Adjust given rectangle so that it fits within given dimensions. If the
	 * rectangle is completely outside, it returns a zero size rectangle.
	 */
	public static Rectangle2d overlap(Rectangle2d r0, Rectangle2d r1) {
		double x = Math.max(r0.x, r1.x);
		double xw = Math.min(r0.x + r0.w, r1.x + r1.w);
		double y = Math.max(r0.y, r1.y);
		double yh = Math.min(r0.y + r0.h, r1.y + r1.h);
		if (y > yh || x > xw) return Rectangle2d.NULL;
		return new Rectangle2d(x, y, xw - x, yh - y);
	}

}
