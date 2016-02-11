package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateMax;

/**
 * Geometric utilities.
 */
public class GeometryUtil {

	private GeometryUtil() {}

	/**
	 * Resize dimension to fit at least given dimensions while maintaining aspect ratio.
	 */
	public static Dimension2d resizeToMin(Dimension2d size, Dimension2d min) {
		double w = size.w;
		double h = size.h;
		if (w == 0 && h == 0) return size;
		if (w == 0 || Double.isInfinite(w)) w = min.w;
		if (h == 0 || Double.isInfinite(h)) h = min.h;
		if (h > 0) w = Math.max(min.w, min.h * w / h);
		if (w > 0) h = Math.max(min.h, min.w * h / w);
		return new Dimension2d(w, h);
	}

	/**
	 * Resize dimension to fit within given dimensions while maintaining aspect ratio.
	 */
	public static Dimension2d resizeToMax(Dimension2d size, Dimension2d max) {
		double w = size.w;
		double h = size.h;
		if (w > 0 && w * max.h >= max.w * h) {
			h = max.w * h / w;
			w = max.w;
		} else if (h > 0) {
			w = w * max.h / h;
			h = max.h;
		}
		return new Dimension2d(w, h);
	}

	/**
	 * Crop dimension to given dimensions. If dimension is smaller than the desired crop it will
	 * remain unchanged.
	 */
	public static Dimension2d crop(Dimension2d size, Dimension2d crop) {
		double w = crop.w;
		double h = crop.h;
		if (w >= size.w) w = size.w;
		if (h >= size.h) h = size.h;
		return new Dimension2d(w, h);
	}

	/**
	 * Crop to given dimensions, with specified crop window alignment. If the original size is
	 * smaller than the desired crop in one dimension that dimension is not cropped.
	 */
	public static Rectangle crop(Dimension2d size, Dimension2d crop, Ratio2d ratio) {
		crop = crop(size, crop);
		validateMax(ratio.x, 1, "X ratio");
		validateMax(ratio.y, 1, "Y ratio");
		double x = ratio.x * (size.w - crop.w);
		double y = ratio.y * (size.h - crop.h);
		return new Rectangle(x, y, crop.w, crop.h);
	}

	/**
	 * Finds the overlapping Adjust given rectangle so that it fits within given dimensions. If the
	 * rectangle is completely outside, it returns a zero size rectangle.
	 */
	public static Rectangle overlap(Rectangle r0, Rectangle r1) {
		double x = Math.max(r0.x, r1.x);
		double xw = Math.min(r0.x + r0.w, r1.x + r1.w);
		double y = Math.max(r0.y, r1.y);
		double yh = Math.min(r0.y + r0.h, r1.y + r1.h);
		if (y > yh || x > xw) return Rectangle.NULL;
		return new Rectangle(x, y, xw - x, yh - y);
	}

}
