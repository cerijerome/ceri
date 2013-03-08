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
		validateDimension(dimension);
		if (widthPercent < 0 || heightPercent < 0) throw new IllegalArgumentException(
			"Percentages must be >= 0: " + widthPercent + ", " + heightPercent);
		long w = dimension.width;
		long h = dimension.height;
		w = w * widthPercent / 100;
		h = h * heightPercent / 100;
		return new Dimension((int) w, (int) h);
	}

	/**
	 * Resize dimension to fit at least given dimensions while maintaining
	 * aspect ratio.
	 */
	public static Dimension resizeToMin(Dimension dimension, int width, int height) {
		validateDimension(dimension);
		validateWidthHeight(width, height);
		long w = dimension.width;
		long h = dimension.height;
		if (w > 0 && w * height <= width * h) {
			h = width * h / w;
			w = width;
		} else if (h > 0) {
			w = w * height / h;
			h = height;
		}
		return new Dimension((int) w, (int) h);
	}

	/**
	 * Resize dimension to fit within given dimensions while maintaining aspect
	 * ratio.
	 */
	public static Dimension resizeToMax(Dimension dimension, int width, int height) {
		validateDimension(dimension);
		validateWidthHeight(width, height);
		long w = dimension.width;
		long h = dimension.height;
		if (w > 0 && w * height >= width * h) {
			h = width * h / w;
			w = width;
		} else if (h > 0) {
			w = w * height / h;
			h = height;
		}
		return new Dimension((int) w, (int) h);
	}

	/**
	 * Crop dimension to given dimensions. If dimension is smaller than the
	 * desired crop it will remain unchanged.
	 */
	public static Dimension crop(Dimension dimension, int width, int height) {
		validateDimension(dimension);
		validateWidthHeight(width, height);
		int w = dimension.width;
		int h = dimension.height;
		if (width >= w) width = w;
		if (height >= h) height = h;
		return new Dimension(width, height);
	}

	/**
	 * Crop to given dimensions, with specified crop window alignment. If the
	 * original size is smaller than the desired crop in one dimension that
	 * dimension is not cropped. If alignX is null it defaults to Left
	 * alignment. If alignY is null it defaults to Top alignment.
	 */
	public static Rectangle crop(Dimension dimension, int width, int height, AlignX alignX,
		AlignY alignY) {
		validateDimension(dimension);
		validateWidthHeight(width, height);
		if (alignX == null) alignX = AlignX.Left;
		if (alignY == null) alignY = AlignY.Top;
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
		if (rectangle.x < 0 || rectangle.y < 0 || rectangle.width < 0 || rectangle.height < 0) 
			throw new IllegalArgumentException(
			"Rectangle values must be >= 0: " + rectangle.x + ", " + rectangle.y + ", " +
				rectangle.width + ", " + rectangle.height);
		validateDimension(dimension);
		if (rectangle.x >= dimension.width || rectangle.y >= dimension.height) return new Rectangle();
		if (rectangle.x + rectangle.width > dimension.width) rectangle.width =
			dimension.width - rectangle.x;
		if (rectangle.y + rectangle.height > dimension.height) rectangle.height =
			dimension.height - rectangle.y;
		return rectangle;
	}

	private static void validateDimension(Dimension dimension) {
		if (dimension.width < 0 || dimension.height < 0) throw new IllegalArgumentException(
			"Dimensions must be >= 0: " + dimension.width + ", " + dimension.height);
	}

	private static void validateWidthHeight(int width, int height) {
		if (width < 0 || height < 0) throw new IllegalArgumentException(
			"Width/height must be >= 0: " + width + ", " + height);
	}

}
