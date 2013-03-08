package ceri.geo;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Geometric utilities.
 */
public class GeoUtil {

	private GeoUtil() {}

	/**
	 * Resize dimension by multiplying width and height by percentages, without
	 * maintaining aspect ratio.
	 */
	public static Dimension resizePercent(Dimension dimension, int widthPercent, int heightPercent) {
		int w = dimension.width;
		int h = dimension.height;
		w = (int) (((long) w * widthPercent) / 100);
		h = (int) (((long) h * heightPercent) / 100);
		return new Dimension(w, h);
	}

	/**
	 * Resize dimension to fit at least given dimensions while maintaining
	 * aspect ratio.
	 */
	public static Dimension resizeToMin(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if ((long) w * height <= (long) width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return new Dimension(w, h);
	}

	/**
	 * Resize dimension to fit within given dimensions while maintaining aspect
	 * ratio.
	 */
	public static Dimension resizeToMax(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if ((long) w * height >= (long) width * h) {
			h = width * h / w;
			w = width;
		} else {
			w = w * height / h;
			h = height;
		}
		return new Dimension(w, h);
	}

	/**
	 * Crop dimension to given dimensions. If dimension is smaller than the
	 * desired crop it will remain unchanged.
	 */
	public static Dimension crop(Dimension dimension, int width, int height) {
		int w = dimension.width;
		int h = dimension.height;
		if (width >= w) width = w;
		if (height >= h) height = h;
		return new Dimension(width, height);
	}

	/**
	 * Crop to given dimensions, with specified crop window alignment. If the
	 * original size is smaller than the desired crop in one dimension that
	 * dimension is not cropped.
	 */
	public static Rectangle crop(Dimension dimension, int width, int height, AlignX alignX,
		AlignY alignY) {
		int w = dimension.width;
		int h = dimension.height;
		Dimension cropDimension = crop(new Dimension(w, h), width, height);
		// If no change required, just return the image
		int x = (int) (alignX.offsetMultiplier * (w - cropDimension.width));
		int y = (int) (alignY.offsetMultiplier * (h - cropDimension.height));
		return new Rectangle(x, y, cropDimension.width, cropDimension.height);
	}

	/**
	 * Adjust given rectangle so that it fits within given dimensions. If the
	 * rectangle is completely outside, it returns a zero size rectangle.
	 */
	public static Rectangle overlap(Rectangle rectangle, Dimension dimension) {
		rectangle = new Rectangle(rectangle);
		if (rectangle.x >= dimension.width || rectangle.y >= dimension.height) return new Rectangle();
		if (rectangle.x + rectangle.width > dimension.width) rectangle.width =
			dimension.width - rectangle.x;
		if (rectangle.y + rectangle.height > dimension.height) rectangle.height =
			dimension.height - rectangle.y;
		return rectangle;
	}

}
